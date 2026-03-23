package com.nubianlanguages.audioservices.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicWordResponse {
    private Long id;
    private String word;
    private String meaning;
    private String audioUrl;

    public PublicWordResponse() {
    }

    public PublicWordResponse(Long id, String word, String meaning, String audioUrl) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.audioUrl = audioUrl;
    }


}