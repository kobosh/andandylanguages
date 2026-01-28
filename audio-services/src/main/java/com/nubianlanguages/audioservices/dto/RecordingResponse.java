package com.nubianlanguages.audioservices.dto;

import org.springframework.http.ResponseEntity;

public record RecordingResponse(
        Long id,
        String word,
        String meaning,
        Long userId,
        String objectKey
) {}
