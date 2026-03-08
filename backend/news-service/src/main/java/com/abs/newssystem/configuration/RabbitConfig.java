package com.abs.newssystem.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String TASKS_QUEUE = "news_tasks";
    public static final String RESULTS_QUEUE = "news_results";

    @Bean public Queue tasksQueue() {
        return new Queue(TASKS_QUEUE, true);
    }
    @Bean public Queue resultsQueue() {
        return new Queue(RESULTS_QUEUE, true);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
