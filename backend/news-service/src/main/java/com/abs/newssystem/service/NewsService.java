package com.abs.newssystem.service;

import com.abs.newssystem.Dto.NewsRequestDto;
import com.abs.newssystem.Dto.PredictionResponseDto;
import com.abs.newssystem.model.News;
import com.abs.newssystem.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NewsService {

    private final NewsRepository newsRepository;
    private final RestTemplate restTemplate;

    @Value("${ML_SERVICE_URL:http://localhost:5000/predict}")
    private String mlServiceUrl;

    public News analyzeAndSave(String title, String content, String link, LocalDateTime date) {
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

            news.setPersonStudents(probs.getOrDefault("person_students", 0.0));
            news.setPersonAcademics(probs.getOrDefault("person_academics", 0.0));
            news.setPersonStaffAdmin(probs.getOrDefault("person_staff_admin", 0.0));
            news.setPersonApplicants(probs.getOrDefault("person_applicants", 0.0));
            news.setPersonAlumni(probs.getOrDefault("person_alumni", 0.0));
            news.setPersonGeneral(probs.getOrDefault("person_general", 0.0));
        }

        return newsRepository.save(news);
    }
}
