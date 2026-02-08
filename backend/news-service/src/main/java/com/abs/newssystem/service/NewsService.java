package com.abs.newssystem.service;

import com.abs.newssystem.Dto.BulkUploadResponse;
import com.abs.newssystem.Dto.NewsRequestDto;
import com.abs.newssystem.Dto.PredictionResponseDto;
import com.abs.newssystem.model.News;
import com.abs.newssystem.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsService {

    private final NewsRepository newsRepository;
    private final RestTemplate restTemplate;

    @Value("${ML_SERVICE_URL:http://localhost:5000/predict}")
    private String mlServiceUrl;

    public News analyzeAndSave(String title, String content, String link, LocalDateTime date) {
        if (link != null && !link.isEmpty() && newsRepository.existsByOriginalLink(link)) {
            log.warn("Пропуск: Новость с такой ссылкой уже есть.");
            return null;
        }

        if (newsRepository.existsByTitleAndContent(title, content)) {
            log.warn("Пропуск: Новость с таким заголовком и текстом уже есть.");
            return null;
        }

        String fullText = (title != null ? title : "") + ". " + (content != null ? content : "");
        NewsRequestDto request = new NewsRequestDto(fullText);

        PredictionResponseDto response = restTemplate.postForObject(
                mlServiceUrl,
                request,
                PredictionResponseDto.class
        );

        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        news.setOriginalLink(link);
        news.setPublishedDate(date != null ? date : LocalDateTime.now());

        if (response != null && response.getProbabilities() != null) {
            Map<String, Double> probs = response.getProbabilities();

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

        return newsRepository.save(news);
    }

    public BulkUploadResponse processExcel(MultipartFile file) {
        int success = 0;
        int errors = 0;
        List<String> failedTitles = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);

            if (headerRow == null) throw new RuntimeException("Файл пуст");

            Map<String, Integer> colMap = new HashMap<>();
            for (Cell cell : headerRow) {
                colMap.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
            }

            if (!colMap.containsKey("News_Title") || !colMap.containsKey("News_Text")) {
                throw new RuntimeException("В файле отсутствуют необходимые колонки (News_Title или News_Text)");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String title = getCellValue(row, colMap.get("News_Title")).trim();
                String content = getCellValue(row, colMap.get("News_Text")).trim();

                if (title.isEmpty() && content.isEmpty()) {
                    continue;
                }

                try {
                    String link = getCellValue(row, colMap.get("News_Link"));
                    LocalDateTime date = parseExcelDate(row.getCell(colMap.get("News_Date")));

                    analyzeAndSave(title, content, link, date);
                    success++;

                    if (success % 5 == 0)
                        log.info("Загружено новостей: {}", success);

                } catch (Exception e) {
                    errors++;
                    failedTitles.add(title.isEmpty() ? "Строка " + (i + 1) : title);
                    log.warn("Ошибка на строке {}: {}", i, e.getMessage());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Ошибка чтения Excel: " + e.getMessage());
        }
        return new BulkUploadResponse(success, errors, failedTitles);
    }

    private String getCellValue(Row row, Integer colIndex) {
        if (colIndex == null) return "";
        Cell cell = row.getCell(colIndex);
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) return cell.getLocalDateTimeCellValue().toString();
                return String.valueOf((long) cell.getNumericCellValue());
            default: return "";
        }
    }

    private LocalDateTime parseExcelDate(Cell cell) {
        if (cell == null) return LocalDateTime.now();

        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            }
            if (cell.getCellType() == CellType.STRING) {
                String val = cell.getStringCellValue().trim();
                if (!val.isEmpty()) {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                    return LocalDate.parse(val, formatter).atStartOfDay();
                }
            }
        } catch (Exception e) {
            log.warn("Не удалось спарсить дату, ставим текущую");
        }
        return LocalDateTime.now();
    }
}
