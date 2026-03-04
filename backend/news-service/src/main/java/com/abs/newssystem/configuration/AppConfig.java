package com.abs.newssystem.configuration;

import com.abs.newssystem.model.User;
import com.abs.newssystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
public class AppConfig {
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(3000);
        factory.setReadTimeout(4000);

        RestTemplate restTemplate = new RestTemplate(factory);

        restTemplate.setErrorHandler(new org.springframework.web.client.DefaultResponseErrorHandler());
        return restTemplate;
    }

    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ROLE_ADMIN");
                userRepository.save(admin);
                log.info("Admin has been created");
            }
        };
    }
}
