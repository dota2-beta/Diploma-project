package com.abs.newssystem.service;

import com.abs.newssystem.Dto.NewsRequestDto;
import com.abs.newssystem.Dto.PredictionResponseDto;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.function.Supplier;

@Service
@Slf4j
public class MlClientService {

    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;

    @Value("${ML_SERVICE_URL:http://ml-service:5000/predict}")
    private String mlServiceUrl;

    public MlClientService(RestTemplate restTemplate, CircuitBreakerRegistry registry) {
        this.restTemplate = restTemplate;
        this.circuitBreaker = registry.circuitBreaker("ml-service");
    }

    public Map<String, Double> getPredictions(String fullText) {
        Supplier<Map<String, Double>> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
            NewsRequestDto request = new NewsRequestDto(fullText);
            PredictionResponseDto response = restTemplate.
                    postForObject(mlServiceUrl, request, PredictionResponseDto.class);

            if (response == null || response.getProbabilities() == null) {
                throw new RuntimeException("Empty response from ML");
            }
            return response.getProbabilities();
        });

        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            return fallbackGetPredictions(e);
        }
    }

    // запасной метод, возвращает null, чтобы NewsService понял, что разметка не удалась
    private Map<String, Double> fallbackGetPredictions(Throwable e) {
        log.error("Circuit Breaker сработал или ML сервис упал. Причина: {}", e.getMessage());
        return null;
    }
}
