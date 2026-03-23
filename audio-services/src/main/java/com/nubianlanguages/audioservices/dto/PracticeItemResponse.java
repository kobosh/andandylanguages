package com.nubianlanguages.audioservices.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PracticeItemResponse {
    private Long id;
    private String word;
    private String meaning;
    private String sentence;
    private String sentenceMeaning;
    private String wordBucket;
    private String sentenceBucket;
    private String wordObjectKey;
    private String sentenceObjectKey;
}