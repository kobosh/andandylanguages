package com.nubianlanguages.audioservices.service;

import com.nubianlanguages.audioservices.dto.RecordingRequest;
import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.repository.RecordingRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

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


/*
    public Recording saveRecording(Long userId, RecordingRequest req) {
        MultipartFile file = req.getFile();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        // Base name from original file, fallback if null/blank
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "recording.webm";
        }

        // Add timestamp to avoid overwriting
        String uniqueName = System.currentTimeMillis() + "-" + originalName;
// ✅ Use absolute base directory (VERY IMPORTANT)
        File baseDir = new File(System.getProperty("user.home"), "andandylanguages-recordings");
        File userDir = new File(baseDir, String.valueOf(userId));

        // ✅ Create directories if missing
        if (!userDir.exists()) {
            boolean created = userDir.mkdirs();
            if (!created) {
                throw new RuntimeException("Failed to create directory: " + userDir.getAbsolutePath());
            }
        }
        // recordings/<userId>/<uniqueName>
       // File userDir = new File("recordings", String.valueOf(userId));
        if (!userDir.exists() && !userDir.mkdirs()) {
            throw new RuntimeException("Failed to create directory: " + userDir.getAbsolutePath());
        }

        File dest = new File(userDir, uniqueName);

        try {
            System.out.println("=== FILE DEBUG ===");
            System.out.println("Original filename: " + file.getOriginalFilename());
            System.out.println("Size: " + file.getSize());
            System.out.println("Content type: " + file.getContentType());
            System.out.println("Destination: " + dest.getAbsolutePath());
            System.out.println("==================");
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save recording file", e);
        }

        Recording r = new Recording();
        r.setUserId(Math.toIntExact(userId));
        r.setWord(req.getWord());
        r.setMeaning(req.getMeaning());
        r.setFilename(uniqueName);
        r.setFilePath(dest.getAbsolutePath());

        return repo.save(r);
    }
*/


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











