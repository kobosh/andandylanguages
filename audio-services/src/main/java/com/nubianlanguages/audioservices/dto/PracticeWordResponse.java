package com.nubianlanguages.audioservices.dto;

public record PracticeWordResponse(
        Long id,
        String word,
        String meaning,
        String wordAudioUrl,
        String sentence,
        String sentenceMeaning,
        String sentenceAudioUrl,
        String author
) {}
