package com.abs.newssystem.service;

import com.abs.newssystem.model.News;
import com.abs.newssystem.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsAnalysisScheduler {

    private final NewsRepository newsRepository;
    private final MlClientService mlClientService;

    @Scheduled(fixedDelay = 60000)
    public void retryAnalysis() {
        List<News> unanalyzedNews = newsRepository.findAllByIsAnalyzedFalse();

        if (unanalyzedNews.isEmpty()) {
            return;
        }

        log.info("Найдено {} неразмеченных новостей. Пробую обработать...", unanalyzedNews.size());

        for (News news : unanalyzedNews) {
            try {
                String fullText = news.getTitle() + ". " + news.getContent();
                Map<String, Double> probs = mlClientService.getPredictions(fullText);

                if (probs != null) {
                    NewsService.mapProbabilities(news, probs);
                    news.setIsAnalyzed(true);
                    newsRepository.save(news);
                    log.info("Новость c ID:{} успешно доразмечена.", news.getId());
                }
            } catch (Exception e) {
                log.warn("Не удалось доразметить новость c ID:{}. ML сервис всё еще недоступен.", news.getId());
                break;
            }
        }
    }
}
