package com.nubianlanguages.audioservices.service;

import com.nubianlanguages.audioservices.dto.PublicWordResponse;
import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.repository.RecordingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PublicCatalogService {

    private final RecordingRepository recordingRepository;
    private final MinioStorageService minioStorageService;

    public PublicCatalogService(RecordingRepository recordingRepository,
                                MinioStorageService minioStorageService) {
        this.recordingRepository = recordingRepository;
        this.minioStorageService = minioStorageService;
    }

    public List<PublicWordResponse> getPublicWords() {
        List<Recording> recordings = recordingRepository.findAll();
        List<PublicWordResponse> result = new ArrayList<>();

        for (Recording rec : recordings) {
            if (rec.getWordObjectKey() == null || rec.getWordObjectKey().isBlank()) {
                continue;
            }

            try {
                String audioUrl = minioStorageService.getWordUrl(rec.getId());

                result.add(new PublicWordResponse(
                        rec.getId(),
                        rec.getWord(),
                        rec.getMeaning(),
                        audioUrl
                ));
            } catch (Exception e) {
                // skip broken audio entries instead of failing entire response
                System.err.println("Failed to build public word for recording id=" + rec.getId() + ": " + e.getMessage());
            }
        }

        return result;
    }
}