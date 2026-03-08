package com.abs.newssystem.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "news", indexes = {
        @Index(name = "idx_news_published_date", columnList = "publishedDate DESC"),

        // индексы для самых популярных фильтров
        @Index(name = "idx_news_theme_science", columnList = "themeScienceResearch"),
        @Index(name = "idx_news_person_students", columnList = "personStudents"),
        @Index(name = "idx_news_person_general", columnList = "personGeneral"),

        @Index(name = "idx_news_original_link", columnList = "originalLink", unique = true)
})
@Data
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String originalLink;

    private LocalDateTime publishedDate;
    private Boolean isAnalyzed;

    // тематика
    private Double themeScienceResearch;
    private Double themeAcademicProcess;
    private Double themeAcademicContests;
    private Double themeExtracurricular;
    private Double themeSport;
    private Double themeCultureArt;
    private Double themeCareerEmployment;
    private Double themeAdministrationOfficial;
    private Double themePartnershipCollaboration;
    private Double themeCivicPatriotic;
    private Double themeAdmissionCampaign;

    // аудитория
    private Double personStudents;
    private Double personAcademics;
    private Double personStaffAdmin;
    private Double personApplicants;
    private Double personAlumni;
    private Double personGeneral;

    @PrePersist
    @PreUpdate
    public void ensureLinkIsNull() {
        if (this.originalLink != null && this.originalLink.trim().isEmpty()) {
            this.originalLink = null;
        }
    }
}