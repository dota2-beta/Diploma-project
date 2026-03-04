package com.abs.newssystem.repository;

import com.abs.newssystem.model.News;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long>, JpaSpecificationExecutor<News> {
    Page<News> findAll(Pageable pageable);
    List<News> findAllByIsAnalyzedFalse();
    boolean existsByOriginalLink(String originalLink);
    boolean existsByTitleAndContent(String title, String content);
}
