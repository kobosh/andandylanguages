package com.nubianlanguages.contentservice.dto;


import com.nubianlanguages.contentservice.entity.ItemType;
import lombok.Getter;
import lombok.Setter;
@Setter
@Getter
public class WordSentenceRequest {


    private String englishWord;
    private String englishSentence;
    //private ItemType itemType;
    public WordSentenceRequest() {
    }


}