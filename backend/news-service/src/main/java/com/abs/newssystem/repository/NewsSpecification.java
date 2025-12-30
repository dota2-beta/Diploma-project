package com.abs.newssystem.repository;

import com.abs.newssystem.model.News;
import org.springframework.data.jpa.domain.Specification;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;

public class NewsSpecification {

    public static Specification<News> filterByScores(Map<String, Double> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            for (Map.Entry<String, Double> entry : filters.entrySet()) {
                String key = entry.getKey();
                Double value = entry.getValue();

                if (value != null && value > 0) {
                    try {
                        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(key), value));
                    } catch (IllegalArgumentException e) {
                    }
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
