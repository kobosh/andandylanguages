package com.nubianlanguages.audioservices.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicWordResponseXX {
    private Long id;
    private String word;
    private String meaning;
    private String audioUrl;

    public PublicWordResponseXX() {
    }

    public PublicWordResponseXX(Long id, String word, String meaning, String audioUrl) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.audioUrl = audioUrl;
    }


}