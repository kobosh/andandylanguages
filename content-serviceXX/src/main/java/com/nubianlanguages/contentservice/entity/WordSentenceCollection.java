package com.nubianlanguages.contentservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Entity
@Table(name = "word_sentence_collection")
public class WordSentenceCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    @NotBlank
    @Column(nullable = false)
    private String englishWord;

    private String englishSentence;

    public WordSentenceCollection() {}

    public void setEnglishWord(String s) {
    }

    public void setEnglishSentence(String s) {

    }
}

//    public void setItemType(ItemType itemType) {
//        this.itemType = itemType;
//    }
//
//    public void setEnglishWord(String englishWord) {
//        this.englishWord = englishWord;
//    }
//
//    public void setEnglishSentence(String englishSentence) {
//        this.englishSentence = englishSentence;
//    }
