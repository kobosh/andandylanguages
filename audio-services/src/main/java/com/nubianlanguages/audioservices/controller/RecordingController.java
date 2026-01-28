package com.nubianlanguages.audioservices.controller;

import com.nubianlanguages.audioservices.dto.RecordingRequest;
import com.nubianlanguages.audioservices.dto.RecordingResponse;
import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.repository.RecordingRepository;
import com.nubianlanguages.audioservices.service.MinioStorageService;
import com.nubianlanguages.audioservices.service.RecordingService;
import com.nubianlanguages.audioservices.service.StorageService;
import org.springframework.security.oauth2.jwt.Jwt;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
@RestController
@RequestMapping("/api/recordings")
@Slf4j
public class RecordingController {
   private  final MinioStorageService minioStorageService;
    private final RecordingService recordingService;
    private final RecordingRepository recordingRepository;
    private final StorageService storageService;

    public RecordingController(
            MinioStorageService minioStorageService, RecordingService recordingService,
            RecordingRepository recordingRepository,
            StorageService storageService
    ) {
        this.minioStorageService = minioStorageService;
        this.recordingService = recordingService;
        this.recordingRepository = recordingRepository;
        this.storageService = storageService;
    }

    // 🎧 STREAM AUDIO (protected automatically)
    @GetMapping("/{id}/stream")
    public ResponseEntity<StreamingResponseBody> stream(@PathVariable Long id) {

        Recording rec = recordingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recording not found"));

        InputStream inputStream = storageService.get(rec.getObjectKey());

        StreamingResponseBody stream = outputStream -> {
            try (inputStream) {
                inputStream.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/webm")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(stream);
    }

    // ⬆️ UPLOAD (JWT-secured)
    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("word") String word,
            @RequestParam("meaning") String meaning,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt   // ✅ THIS IS THE KEY
    ) {

        // 🔑 userId comes from JWT "sub"
        Long userId = Long.parseLong(jwt.getSubject());

        RecordingRequest req = new RecordingRequest();
        req.setWord(word);
        req.setMeaning(meaning);
        req.setFile(file);

        Recording saved = recordingService.saveRecording(userId, req);

        log.info("Uploaded recording id={} userId={} size={} bytes",
                saved.getId(), userId, file.getSize());
        System.out.println("Uploaded recording"+saved.getId()+" "+userId+" "+file.getSize());
        return ResponseEntity.ok(
                new RecordingResponse(
                      saved.getId().longValue(),
                        saved.getWord(),
                        saved.getMeaning(),
                        userId,
                        saved.getObjectKey()
                )
        );

        //return ResponseEntity.ok(saved);
    }
    @GetMapping("/recordings/{id}/audio")
    public ResponseEntity<String> getAudioUrl(@PathVariable Long id) throws Exception {


        String url = minioStorageService.getminiourl(id);

        return ResponseEntity.ok(url);
    }

}


