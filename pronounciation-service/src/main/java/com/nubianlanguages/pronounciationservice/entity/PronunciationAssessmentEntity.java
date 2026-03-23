package com.nubianlanguages.pronounciationservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "pronunciation_assessment")
public class PronunciationAssessmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long recordingId;

    @Column(nullable = false, length = 1000)
    private String expectedText;

    @Column(length = 1000)
    private String recognizedText;

    private String languageCode;

    private Double overallScore;
    private Double accuracyScore;
    private Double fluencyScore;
    private Double completenessScore;
    private Double prosodyScore;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String feedbackJson;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String rawAzureJson;

    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }


}