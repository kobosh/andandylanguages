package com.nubianlanguages.audioservices.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PracticeItemSummaryResponse {

    private Long id;
    private String word;
    private String meaning;
    // ✅ REQUIRED: constructor with arguments (THIS FIXES YOUR ERROR)
    public PracticeItemSummaryResponse(Long id, String word, String meaning) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
    }

}
