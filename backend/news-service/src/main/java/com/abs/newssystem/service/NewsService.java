package com.abs.newssystem.service;

import com.abs.newssystem.Dto.CachedPageDto;
import com.abs.newssystem.configuration.RabbitConfig;
import com.abs.newssystem.model.News;
import com.abs.newssystem.repository.NewsRepository;
import com.abs.newssystem.repository.NewsSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final NewsRepository newsRepository;
    private final RabbitTemplate rabbitTemplate;

    private static final String DATE_REGEX = "^\\d{2}\\.\\d{2}\\.\\d{4}$";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Cacheable(value = "news_list", key = "{#search, #page, #size, #allParams}")
    public CachedPageDto<News> getAllNews(String search, int page, int size, Map<String, String> allParams) {
        Map<String, Double> filters = new HashMap<>();

        String[] allowedFields = {
                "themeScienceResearch", "themeAcademicProcess", "themeAcademicContests",
                "themeExtracurricular", "themeSport", "themeCultureArt",
                "themeCareerEmployment", "themeAdministrationOfficial",
                "themePartnershipCollaboration", "themeCivicPatriotic",
                "themeAdmissionCampaign",
                "personStudents", "personAcademics", "personStaffAdmin",
                "personApplicants", "personAlumni", "personGeneral"
        };

        for (String field : allowedFields) {
            if (allParams.containsKey(field)) {
                try {
                    Double val = Double.parseDouble(allParams.get(field));
                    filters.put(field, val);
                } catch (NumberFormatException e) {
                    // ..
                }
            }
        }

        Specification<News> spec = NewsSpecification.filterByScores(filters);

        if (search != null) {
            spec = spec.and(NewsSpecification.searchByText(search));
        }

        Page<News> news = newsRepository.findAll(
                spec,
                PageRequest.of(page, size, Sort.by("publishedDate").descending())
        );
        return new CachedPageDto<>(news);
    }

    @Cacheable(value = "single_news", key = "#id")
    public News getById(Long id) {
        return newsRepository.findById(id).orElse(null);
    }

    @CacheEvict(value = "news_list", allEntries = true)
    public News analyzeAndSave(String title, String content, String link, LocalDateTime date) {
        String cleanLink = (link == null || link.trim().isEmpty()) ? null : link.trim();

        if (cleanLink != null && newsRepository.existsByOriginalLink(cleanLink)) {
            log.warn("Пропуск: Новость с такой ссылкой уже есть.");
            return null;
        }

        if (newsRepository.existsByTitleAndContent(title, content)) {
            log.warn("Пропуск: Новость с таким заголовком и текстом уже есть.");
            return null;
        }

        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        news.setOriginalLink(link);
        news.setPublishedDate(date != null ? date : LocalDateTime.now());
        news.setIsAnalyzed(false);

        News saved = newsRepository.save(news);

        Map<String, Object> task = Map.of(
                "id", saved.getId(),
                "text", saved.getTitle() + ". " + saved.getContent()
        );
        rabbitTemplate.convertAndSend(RabbitConfig.TASKS_QUEUE, task);

        return saved;
    }

    @Caching(evict = {
            @CacheEvict(value = "news_list", allEntries = true),
            @CacheEvict(value = "single_news", key = "#id")
    })
    public News update(Long id, News details) {
        News news = newsRepository.findById(id).orElseThrow();
        news.setTitle(details.getTitle());
        news.setContent(details.getContent());
        news.setOriginalLink(details.getOriginalLink());
        return newsRepository.save(news);
    }

    @Caching(evict = {
            @CacheEvict(value = "news_list", allEntries = true),
            @CacheEvict(value = "single_news", key = "#id")
    })
    public void delete(Long id) {
        newsRepository.deleteById(id);
    }

    public static void updateWeights(News news, Map<String, Double> probs) {
        news.setThemeScienceResearch(probs.getOrDefault("theme_science_research", 0.0));
        news.setThemeAcademicProcess(probs.getOrDefault("theme_academic_process", 0.0));
        news.setThemeAcademicContests(probs.getOrDefault("theme_academic_contests", 0.0));
        news.setThemeExtracurricular(probs.getOrDefault("theme_extracurricular", 0.0));
        news.setThemeSport(probs.getOrDefault("theme_sport", 0.0));
        news.setThemeCultureArt(probs.getOrDefault("theme_culture_art", 0.0));
        news.setThemeCareerEmployment(probs.getOrDefault("theme_career_employment", 0.0));
        news.setThemeAdministrationOfficial(probs.getOrDefault("theme_administration_official", 0.0));
        news.setThemePartnershipCollaboration(probs.getOrDefault("theme_partnership_collaboration", 0.0));
        news.setThemeCivicPatriotic(probs.getOrDefault("theme_civic_patriotic", 0.0));
        news.setThemeAdmissionCampaign(probs.getOrDefault("theme_admission_campaign", 0.0));

        news.setPersonStudents(probs.getOrDefault("person_students", 0.0));
        news.setPersonAcademics(probs.getOrDefault("person_academics", 0.0));
        news.setPersonStaffAdmin(probs.getOrDefault("person_staff_admin", 0.0));
        news.setPersonApplicants(probs.getOrDefault("person_applicants", 0.0));
        news.setPersonAlumni(probs.getOrDefault("person_alumni", 0.0));
        news.setPersonGeneral(probs.getOrDefault("person_general", 0.0));
    }

    public void startExcelImport(MultipartFile file) {
        try {
            byte[] fileBytes = file.getBytes();
            // Просто запускаем асинхронный метод
            processExcelAsync(fileBytes);
        } catch (IOException e) {
            log.error("Ошибка при чтении файла Excel", e);
            throw new RuntimeException("Не удалось прочитать файл");
        }
    }

    @Async("importExecutor")
    public void processExcelAsync(byte[] fileBytes) {
        log.info("Начата фоновая обработка Excel файла...");

        int count = 0;

        try (InputStream is = new ByteArrayInputStream(fileBytes);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return;

            Map<String, Integer> colMap = new HashMap<>();
            for (Cell cell : headerRow) {
                colMap.put(cell.getStringCellValue().trim().toLowerCase(), cell.getColumnIndex());
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String title = getCellValue(row, colMap.getOrDefault("news_title", -1)).trim();
                String content = getCellValue(row, colMap.getOrDefault("news_text", -1)).trim();
                String link = getCellValue(row, colMap.getOrDefault("news_link", -1)).trim();
                String dateRaw = getCellValue(row, colMap.getOrDefault("news_date", -1)).trim();

                if (title.isEmpty() && content.isEmpty()) continue;

                LocalDateTime date = LocalDateTime.now();
                if (!dateRaw.isEmpty() && dateRaw.matches(DATE_REGEX)) {
                    try {
                        date = LocalDate.parse(dateRaw, DATE_FORMATTER).atStartOfDay();
                    } catch (DateTimeParseException e) {
                        log.warn("Невалидная дата {}, заменена на текущую", dateRaw);
                    }
                }

                try {
                    analyzeAndSave(title, content, link, date);
                    count++;
                } catch (Exception e) {
                    log.error("Ошибка при сохранении строки {}: {}", i, e.getMessage());
                }
            }
            log.info("Фоновая обработка завершена. Отправлено в очередь: {} новостей", count);

        } catch (Exception e) {
            log.error("Критическая ошибка при разборе Excel", e);
        }
    }

    private String getCellValue(Row row, Integer colIndex) {
        if (colIndex == null || colIndex < 0) return "";
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().format(DATE_FORMATTER);
                }
                yield String.valueOf((long) cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }
}
