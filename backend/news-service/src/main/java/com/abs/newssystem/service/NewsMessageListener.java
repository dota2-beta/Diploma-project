package com.abs.newssystem.service;

import com.abs.newssystem.Dto.MlResultMessageDto;
import com.abs.newssystem.configuration.RabbitConfig;
import com.abs.newssystem.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsMessageListener {
    private final NewsRepository newsRepository;

    @RabbitListener(queues = RabbitConfig.RESULTS_QUEUE)
    @Caching(evict = {
            @CacheEvict(value = "news_list", allEntries = true),
            @CacheEvict(value = "single_news", key = "#result.id")
    })
    public void handleMlResults(MlResultMessageDto result) {
        Long newsId = result.getId();
        Map<String, Double> probs = result.getProbabilities();

        newsRepository.findById(newsId).ifPresent(news -> {
            NewsService.updateWeights(news, probs);
            news.setIsAnalyzed(true);
            newsRepository.save(news);
            log.info("News {} updated successfully",  newsId);
        });
    }
}
