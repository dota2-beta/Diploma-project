package com.abs.newssystem.controller;

import com.abs.newssystem.Dto.AddNewsRequestDto;
import com.abs.newssystem.Dto.CachedPageDto;
import com.abs.newssystem.model.News;
import com.abs.newssystem.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NewsController {

    private final NewsService newsService;

    @GetMapping
    public ResponseEntity<CachedPageDto<News>> getAllNews(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam Map<String, String> allParams) {
        return ResponseEntity.ok(newsService.getAllNews(search, page, size, allParams));
    }

    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        News news = newsService.getById(id);
        return news != null ? ResponseEntity.ok(news) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<News> addNews(@RequestBody AddNewsRequestDto request) {
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

        News saved = newsService.analyzeAndSave(request.getTitle(), request.getContent(),
                request.getLink(), pubDate);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<News> updateNews(@PathVariable Long id, @RequestBody News details) {
        if (details.getOriginalLink() != null && details.getOriginalLink().trim().isEmpty()) {
            details.setOriginalLink(null);
        }
        return ResponseEntity.ok(newsService.update(id, details));
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        newsService.startExcelImport(file);
        return ResponseEntity.ok("Файл принят. Новости скоро будут добавлены и размечены.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.delete(id);
        return ResponseEntity.noContent().build(); // Код 204
    }
}
