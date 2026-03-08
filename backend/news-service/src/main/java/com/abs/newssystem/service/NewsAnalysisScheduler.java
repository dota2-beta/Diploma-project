package com.abs.newssystem.service;

import com.abs.newssystem.configuration.RabbitConfig;
import com.abs.newssystem.model.News;
import com.abs.newssystem.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsAnalysisScheduler {

    private final NewsRepository newsRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 300000)
    public void retryAnalysis() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<News> stuckNews = newsRepository.findAllByIsAnalyzedFalseAndPublishedDateBefore(threshold);
        if (stuckNews.isEmpty()) {
            return;
        }

        log.info("{} hung news items were found. I'm forwarding it to RabbitMQ....", stuckNews.size());
        for (News news : stuckNews) {
            Map<String, Object> task = Map.of(
                    "id", news.getId(),
                    "text", news.getTitle() + ". " + news.getContent()
            );
            rabbitTemplate.convertAndSend(RabbitConfig.TASKS_QUEUE, task);
        }
    }
}
