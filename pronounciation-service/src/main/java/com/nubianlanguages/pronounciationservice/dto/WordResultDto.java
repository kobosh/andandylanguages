package com.nubianlanguages.pronounciationservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class WordResultDto {

    private String word;
    private Double accuracyScore;
    private String errorType;

    public WordResultDto() {
    }

    public WordResultDto(String word, Double accuracyScore, String errorType) {
        this.word = word;
        this.accuracyScore = accuracyScore;
        this.errorType = errorType;
    }


}