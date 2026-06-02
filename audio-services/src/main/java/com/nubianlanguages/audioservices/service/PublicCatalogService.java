package com.nubianlanguages.audioservices.service;

import com.nubianlanguages.audioservices.dto.PublicWordResponseXX;
import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.repository.RecordingRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class PublicCatalogService {

    private final RecordingRepository recordingRepository;

    public PublicCatalogService(RecordingRepository recordingRepository) {
        this.recordingRepository = recordingRepository;
    }

    public List<PublicWordResponseXX> getPublicWords() {
        List<Recording> recordings = recordingRepository.findAll();
        List<PublicWordResponseXX> result = new ArrayList<>();

        for (Recording rec : recordings) {
            if (rec.getWordObjectKey() == null || rec.getWordObjectKey().isBlank()) {
                continue;
            }

            String audioUrl = "http://localhost:8083/api/recordings/" + rec.getId() + "/word-audio";

            result.add(new PublicWordResponseXX(
                    rec.getId(),
                    rec.getWord(),
                    rec.getMeaning(),
                    audioUrl
            ));
        }

        return result;
    }
}