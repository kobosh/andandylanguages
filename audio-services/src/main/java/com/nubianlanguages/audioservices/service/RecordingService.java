package com.nubianlanguages.audioservices.service;

import com.nubianlanguages.audioservices.dto.RecordingRequest;
import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.repository.RecordingRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;import java.io.IOException;

@Service
public class RecordingService {

   private final RecordingRepository  recordingRepository;
    private final StorageService storageService;

    public RecordingService(RecordingRepository repo,StorageService storageService) {
        this.storageService = storageService;
        this.recordingRepository=repo;
    }
    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-");
    }


    //@Transactional
    public Recording saveRecording(Long userId, RecordingRequest req) {
        System.out.println("🔥 ENTERED saveRecording");

        long now = System.currentTimeMillis();
        String wordSafe = safe(req.getWord());   // or req.getMeaning()
        String objectKey =
                userId + "/" + now + "-" + wordSafe + "-" + now + ".webm";
        storageService.put(userId, req.getFile(), objectKey);
        Recording rec = new Recording();
        rec.setUserId(Math.toIntExact(userId));
        rec.setObjectKey(objectKey);   // STORE MinIO key, NOT a file path
        rec.setWord(req.getWord());
        rec.setMeaning(req.getMeaning());
        return recordingRepository.save(rec);
    }
}











