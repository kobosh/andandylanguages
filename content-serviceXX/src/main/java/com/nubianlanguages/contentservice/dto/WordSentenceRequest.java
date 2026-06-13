package com.nubianlanguages.contentservice.dto;


import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
public class WordSentenceRequest {


    private String englishWord;
    private String englishSentence;

    public WordSentenceRequest() {
    }

}