package com.nubianlanguages.audioservices.controller;

import com.nubianlanguages.audioservices.dto.RecordingRequest;
import com.nubianlanguages.audioservices.dto.RecordingResponse;
import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.repository.RecordingRepository;
import com.nubianlanguages.audioservices.service.MinioStorageService;
import com.nubianlanguages.audioservices.service.RecordingService;
import com.nubianlanguages.audioservices.service.StorageService;
import org.springframework.http.MediaType;
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

    private final MinioStorageService minioStorageService;
    private final RecordingService recordingService;
    private final RecordingRepository recordingRepository;
    private final StorageService storageService;

    public RecordingController(
            MinioStorageService minioStorageService,
            RecordingService recordingService,
            RecordingRepository recordingRepository,
            StorageService storageService
    ) {
        this.minioStorageService = minioStorageService;
        this.recordingService = recordingService;
        this.recordingRepository = recordingRepository;
        this.storageService = storageService;
    }

    // 🎧 STREAM WORD or SENTENCE
    // GET /api/recordings/{id}/stream?type=WORD
   /* @GetMapping("/{id}/stream")
    public ResponseEntity<StreamingResponseBody> stream(
            @PathVariable Long id,
            @RequestParam(defaultValue = "WORD") String type,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.parseLong(jwt.getSubject());

        Recording rec = recordingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recording not found"));

        String objectKey = resolveObjectKey(rec, type);

        InputStream inputStream = storageService.get(objectKey);

        StreamingResponseBody stream = outputStream -> {
            try (inputStream) {
                inputStream.transferTo(outputStream);
            }
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "audio/webm")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(stream);
    }*/
    @GetMapping("/{id}/stream")
    public ResponseEntity<StreamingResponseBody> stream(
            @PathVariable Long id,
            @RequestParam(defaultValue = "WORD") String type,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.parseLong(jwt.getSubject());

        Recording rec = recordingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recording not found"));

        InputStream inputStream;
        if ("SENTENCE".equalsIgnoreCase(type)) {
            inputStream = minioStorageService.getSentence(rec.getSentenceObjectKey());
        } else {
            inputStream = minioStorageService.get(rec.getWordObjectKey());
        }

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

    // ⬆️ UPLOAD WORD (creates the record)
    @PostMapping(value = "/upload-word", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadWord(
            @RequestParam("word") String word,
            @RequestParam("meaning") String meaning,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.parseLong(jwt.getSubject());

        log.info("UPLOAD WORD: name={} contentType={} size={}",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        RecordingRequest req = new RecordingRequest();
        req.setWord(word);
        req.setMeaning(meaning);
        req.setFile(file);

        Recording saved = recordingService.saveRecording(userId, req);

        return ResponseEntity.ok(
                new RecordingResponse(
                        saved.getId(),
                        saved.getWord(),
                        saved.getMeaning(),
                        userId,
                        saved.getWordObjectKey()
                )
        );
    }

    // ⬆️ UPLOAD SENTENCE (updates existing record)
    @PostMapping(value = "/upload-sentence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSentence(
            @RequestParam("recordingId") Long recordingId,
            @RequestParam("sentence") String sentence,
            @RequestParam("sentenceMeaning") String sentenceMeaning,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.parseLong(jwt.getSubject());

        log.info("UPLOAD SENTENCE: name={} contentType={} size={}",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        Recording saved = recordingService.updateSentence(
                recordingId, userId, sentence, sentenceMeaning, file
        );

        return ResponseEntity.ok(
                new RecordingResponse(
                        saved.getId(),
                        saved.getSentence(),
                        saved.getSentenceMeaning(),
                        userId,
                        saved.getSentenceObjectKey()
                )
        );
    }

    // 🔗 GET PRESIGNED URL for WORD or SENTENCE
    // GET /api/recordings/{id}/url?type=WORD
    /*@GetMapping("/{id}/url")
    public ResponseEntity<String> getAudioUrl(
            @PathVariable Long id,
            @RequestParam(defaultValue = "WORD") String type,
            @AuthenticationPrincipal Jwt jwt
    ) throws Exception {
        Long userId = Long.parseLong(jwt.getSubject());

        Recording rec = recordingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recording not found"));
        String objectKey = resolveObjectKey(rec, type);
        String url = minioStorageService.getUrl(objectKey, type);
        return ResponseEntity.ok(url);
    }
       */
    @GetMapping("/{id}/url")
    public ResponseEntity<String> getAudioUrl(
            @PathVariable Long id,
            @RequestParam(defaultValue = "WORD") String type,
            @AuthenticationPrincipal Jwt jwt
    ) throws Exception {

        // optional security improvement: ensure user owns recording
        Long userId = Long.parseLong(jwt.getSubject());
        recordingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recording not found"));

        String url = minioStorageService.getUrl(id, type);
        return ResponseEntity.ok(url);
    }
    private String resolveObjectKey(Recording rec, String type) {
        if ("SENTENCE".equalsIgnoreCase(type)) {
            if (rec.getSentenceObjectKey() == null || rec.getSentenceObjectKey().isBlank()) {
                throw new RuntimeException("Sentence audio not uploaded yet");
            }
            return rec.getSentenceObjectKey();
        }
        // default WORD
        if (rec.getWordObjectKey() == null || rec.getWordObjectKey().isBlank()) {
            throw new RuntimeException("Word audio not uploaded yet");
        }
        return rec.getWordObjectKey();
    }
}

/*@RestController
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

        InputStream inputStream = storageService.get(rec.getWordObjectKey());

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
    @PostMapping(value = "/upload-word",consumes = "multipart/form-data")
    public ResponseEntity<?> uploadWord(
            @RequestParam("word") String word,
            @RequestParam("meaning") String meaning,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt   // ✅ THIS IS THE KEY
    ) {
        log.info("UPLOAD RECEIVED: name={} contentType={} size={}",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

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
                        saved.getWordObjectKey()
                )
        );

        //return ResponseEntity.ok(saved);
    }

    // ⬆️ UPLOAD SENTENCE (JWT-secured)
    @PostMapping(value = "/upload-sentence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSentence(
            @RequestParam("recordingId") Long recordingId,
            @RequestParam("sentence") String sentence,
            @RequestParam("sentenceMeaning") String sentenceMeaning,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        log.info("UPLOAD SENTENCE RECEIVED: name={} contentType={} size={}",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        Long userId = Long.parseLong(jwt.getSubject());

        RecordingRequest req = new RecordingRequest();
        req.setSentence(sentence);
        req.setSentenceMeaning(sentenceMeaning);
        req.setFile(file);

        Recording saved = recordingService.updateSentence(recordingId,userId,sentence,sentenceMeaning,file);
        // or: recordingService.saveRecording(userId, req); (depends on how you designed it)

        log.info("Uploaded sentence recording id={} userId={} size={} bytes",
                saved.getId(), userId, file.getSize());

        return ResponseEntity.ok(
                new RecordingResponse(
                        saved.getId().longValue(),
                        saved.getSentence(),          // make sure Recording has getSentence()
                        saved.getSentenceMeaning(),   // make sure Recording has getSentenceMeaning()
                        userId,
                        saved.getSentenceObjectKey()
                )
        );
    }
    @GetMapping("/recordings/{id}/audio")
    public ResponseEntity<String> getAudioUrl(@PathVariable Long id) throws Exception {


        String url = minioStorageService.getminiourl(id);

        return ResponseEntity.ok(url);
    }

}*/


