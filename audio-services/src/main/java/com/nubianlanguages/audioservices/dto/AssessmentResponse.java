package com.nubianlanguages.audioservices.dto;

public class AssessmentResponse {
    private Long recordingId;
    private String mode;
    private String expectedText;
    private String transcript;
    private String normalizedExpected;
    private String normalizedActual;
    private double score;
    private String rating;

    public AssessmentResponse(
            Long recordingId,
            String mode,
            String expectedText,
            String transcript,
            String normalizedExpected,
            String normalizedActual,
            double score,
            String rating
    ) {
        this.recordingId = recordingId;
        this.mode = mode;
        this.expectedText = expectedText;
        this.transcript = transcript;
        this.normalizedExpected = normalizedExpected;
        this.normalizedActual = normalizedActual;
        this.score = score;
        this.rating = rating;
    }

    public AssessmentResponse() {}
    // getters/setters
}