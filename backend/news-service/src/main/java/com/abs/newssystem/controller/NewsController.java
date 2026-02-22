package com.abs.newssystem.controller;

import com.abs.newssystem.Dto.AddNewsRequestDto;
import com.abs.newssystem.Dto.BulkUploadResponse;
import com.abs.newssystem.model.News;
import com.abs.newssystem.repository.NewsRepository;
import com.abs.newssystem.repository.NewsSpecification;
import com.abs.newssystem.service.NewsService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NewsController {

    private final NewsService newsService;
    private final NewsRepository newsRepository;

    @GetMapping
    public Page<News> getAllNews(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam Map<String, String> allParams
    ) {
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

        return newsRepository.findAll(
                spec,
                PageRequest.of(page, size, Sort.by("publishedDate").descending())
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        return newsRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public News addNews(@RequestBody AddNewsRequestDto request) {
        LocalDateTime pubDate = LocalDateTime.now();
        try {
            if (request.getDateStr() != null && !request.getDateStr().isEmpty()) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                LocalDate ld = LocalDate.parse(request.getDateStr(), formatter);
                pubDate = ld.atStartOfDay();
            }
        } catch (Exception e) {
            System.err.println("Ошибка парсинга даты: " + request.getDateStr());
        }

        return newsService.analyzeAndSave(
                request.getTitle(),
                request.getContent(),
                request.getLink(),
                pubDate          
        );
    }

    @PostMapping("/upload")
    public ResponseEntity<BulkUploadResponse> uploadExcel(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(newsService.processExcel(file));
    }

    @PutMapping("/{id}")
    public News updateNews(@PathVariable Long id, @RequestBody News newsDetails) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Новость не найдена"));

        news.setTitle(newsDetails.getTitle());
        news.setContent(newsDetails.getContent());
        news.setOriginalLink(newsDetails.getOriginalLink());

        return newsRepository.save(news);
    }

    @DeleteMapping("/{id}")
    public void deleteNews(@PathVariable Long id) {
        newsRepository.deleteById(id);
    }
}
