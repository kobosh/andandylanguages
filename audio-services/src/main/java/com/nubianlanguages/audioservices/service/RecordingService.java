package com.nubianlanguages.audioservices.service;

import com.nubianlanguages.audioservices.dto.RecordingRequest;
import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.repository.RecordingRepository;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;import java.io.IOException;

@Service
@Slf4j
public class RecordingService {

   private final RecordingRepository  recordingRepository;
    private final StorageService storageService;
   private final MinioStorageService minioStorageService;

    public RecordingService(RecordingRepository repo, StorageService storageService, MinioStorageService minioStorageService) {
        this.storageService = storageService;
        this.recordingRepository=repo;
        this.minioStorageService = minioStorageService;
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
    @Transactional
    public Recording saveRecording(Long userId, RecordingRequest req) {

        MultipartFile file = req.getFile();
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Missing audio file");
        }

        // 1) Create DB row first
        Recording rec = new Recording();
        rec.setUserId(userId);
        rec.setWord(req.getWord());
        rec.setMeaning(req.getMeaning());
        // rec.setCreatedAt(LocalDateTime.now()); // only if you don't use @CreationTimestamp

        rec = recordingRepository.save(rec);

        // 2) Upload word using recording ID
        String objectKey = "recordings/" + rec.getId() + "/word.webm";
        String savedKey = storageService.put(userId, file, objectKey);

        // 3) Update DB with uploaded info
        rec.setWordObjectKey(savedKey);
        rec.setWordUploaded(true);

        return recordingRepository.save(rec);
    }

    @Transactional
    public Recording updateSentence(
            Long recordingId,
            Long userId,
            String sentence,
            String sentenceMeaning,
            MultipartFile file
    ) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Missing sentence audio file");
        }

        Recording recording = recordingRepository
                .findByIdAndUserId(recordingId, userId)
                .orElseThrow(() -> new RuntimeException("Recording not found"));

        // upload sentence to sentence bucket using stable key
        String objectKey = "recordings/" + recordingId + "/sentence.webm";
        String savedKey = minioStorageService.putSentence(userId, file, objectKey);

        recording.setSentence(sentence);
        recording.setSentenceMeaning(sentenceMeaning);
        recording.setSentenceObjectKey(savedKey);
        recording.setSentenceUploaded(true);

        return recordingRepository.save(recording);
    }







}











