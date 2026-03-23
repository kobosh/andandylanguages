package com.nubianlanguages.pronounciationservice.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhonemeFeedbackDto {

    private String expectedPhoneme;
    private String actualPhoneme;
    private String issue;

    public PhonemeFeedbackDto() {
    }

    public PhonemeFeedbackDto(String expectedPhoneme, String actualPhoneme, String issue) {
        this.expectedPhoneme = expectedPhoneme;
        this.actualPhoneme = actualPhoneme;
        this.issue = issue;
    }


}