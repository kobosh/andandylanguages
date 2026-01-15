package com.nubianlanguages.audioservices.controller;

import com.nubianlanguages.audioservices.dto.RecordingRequest;
import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.repository.RecordingRepository;
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

    private final RecordingService recordingService;
    private final RecordingRepository recordingRepository;
    private final StorageService storageService;

    public RecordingController(
            RecordingService recordingService,
            RecordingRepository recordingRepository,
            StorageService storageService
    ) {
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

        return ResponseEntity.ok(saved);
    }
}


/*@RestController
@RequestMapping("/api/recordings")
@Slf4j
public class RecordingController {
    private final RecordingService recordingService;
    private final JwtService jwtService;
    private final RecordingRepository recordingRepository;
    private final StorageService storageService;

    public RecordingController(MinioStorageService minioStorageService,
                               RecordingService recordingService,
                               JwtService jwtService,
                               RecordingRepository recordingRepository,
                               StorageService storageService


    ) {
        this.recordingService = recordingService;
        this.jwtService = jwtService;
        this.recordingRepository = recordingRepository;
        this.storageService = storageService;
    }

    // 🎧 STREAM AUDIO
    @GetMapping("/{id}/stream")
    public ResponseEntity<StreamingResponseBody> stream( @PathVariable Long id) {

        Recording rec = recordingRepository.findById( id)
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

    // ⬆️ UPLOAD (trimmed OR original — backend does not care)
    @PostMapping("/upload")
    public ResponseEntity<?> upload(
            @RequestParam("word") String word,
            @RequestParam("meaning") String meaning,
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        System.out.println("🔥 ENTERED upload controller");
        try {
            /*if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Missing or invalid Authorization header");
            }*/

           /* String token = authHeader.substring(7);
            String email = jwtService.extractEmail(token);
            if (email == null || email.isBlank()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid token");
            }
            */
         /*   Long userId = 1L;//(long) email.hashCode(); // TEMP

            RecordingRequest req = new RecordingRequest();
            req.setWord(word);
            req.setMeaning(meaning);
            req.setFile(file);
            System.out.println("🔥 upload ENTERED saving");

            Recording saved = recordingService.saveRecording(userId, req);

            log.info("Uploaded recording id={} size={} bytes",
                    saved.getId(), file.getSize());

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            log.error("Upload failed", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
  /*  @PostMapping("/{wordId}/usage")
    public ResponseEntity<?> uploadUsageRecording(
            @PathVariable Integer wordId,
            @RequestParam MultipartFile audioFile
    ) {
        Recording word = recordingRepository.findById(Long.valueOf(wordId))
                .orElseThrow(() -> new RuntimeException("Word not found"));

        String objectKey =minioStorageService .upload(audioFile);

        Recording usage = new Recording();
        usage.setUserId(word.getUserId());
        usage.setWord(word.getWord());
       // usage.setType(RecordingType.USAGE);
        //usage.setParentRecordingId(word.getId());
        usage.setObjectKey(objectKey);

        recordingRepository.save(usage);

        return ResponseEntity.ok(usage);
    }
*/


