package com.nubianlanguages.pronounciationservice.service;

import com.nubianlanguages.pronounciationservice.dto.PronunciationResponse;
import org.springframework.web.multipart.MultipartFile;

public interface PronunciationService {
    PronunciationResponse assess(MultipartFile audio,
                                 String expectedText,
                                 String languageCode,
                                 Long recordingId);
}