package com.nubianlanguages.contentservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "word_sentence_collection")
public class WordSentenceCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private ItemType itemType;

    //    public WordSentenceCollection() {}
    //

    @Setter
    @NotBlank
    @Column(nullable = false)
    private String englishWord;

    @Setter
    private String englishSentence;

    public WordSentenceCollection() {
    }

    public WordSentenceCollection( String englishWord, String englishSentence) {
        //this.itemType = itemType;
        this.englishWord = englishWord;
        this.englishSentence = englishSentence;
    }

//
//    public void setEnglishSentence(String s) {
//
//    }




}
