package com.nubianlanguages.pronounciationservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
@Getter
@Setter
public class PronunciationResponse {
    private java.util.List<PhonemeFeedbackDto> phonemeFeedback = new java.util.ArrayList<>();

    private Long assessmentId;
    private Long recordingId;
    private String expectedText;
    private String recognizedText;
    private String languageCode;

    private Double overallScore;
    private Double accuracyScore;
    private Double fluencyScore;
    private Double completenessScore;
    private Double prosodyScore;

    private List<String> feedback = new ArrayList<>();
    private List<WordResultDto> wordResults = new ArrayList<>();

    private Instant createdAt;

    }