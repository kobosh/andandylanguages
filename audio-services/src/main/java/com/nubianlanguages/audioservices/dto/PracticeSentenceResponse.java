package com.nubianlanguages.audioservices.dto;

public record PracticeSentenceResponse (

            Long id,
            String sentence,
            String meaning,
            String audioUrl,
            String author
    ) {}


