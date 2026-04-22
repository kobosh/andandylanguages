package com.nubianlanguages.audioservices.controller;

import com.nubianlanguages.audioservices.dto.*;
import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.repository.RecordingRepository;
import com.nubianlanguages.audioservices.service.MinioStorageService;
import com.nubianlanguages.audioservices.service.PronunciationAssessmentService;
import com.nubianlanguages.audioservices.service.RecordingService;
import com.nubianlanguages.audioservices.service.StorageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.List;
import  com.nubianlanguages.audioservices.dto.PracticeWordResponse;

@RestController
@RequestMapping("/api/recordings")
@Slf4j
public class RecordingController {


    private final MinioStorageService minioStorageService;
    private final RecordingService recordingService;
    private final RecordingRepository recordingRepository;
    private final StorageService storageService;
    private final  PronunciationAssessmentService  pronunciationAssessmentService;

    @Value("${minio.bucket.word}")
    private String wordbucket;
    @PostConstruct
    public void testBucket() {
        System.out.println("wordbucket = " + wordbucket);
    }
    public RecordingController(
            MinioStorageService minioStorageService,
            RecordingService recordingService,
            RecordingRepository recordingRepository,
            StorageService storageService, PronunciationAssessmentService pronunciationAssessmentService
    ) {
        this.minioStorageService = minioStorageService;
        this.recordingService = recordingService;
        this.recordingRepository = recordingRepository;
        this.storageService = storageService;
        this.pronunciationAssessmentService = pronunciationAssessmentService;
    }



    // 🎧 STREAM WORD or SENTENCE
    // GET /api/recordings/{id}/stream?type=WORD
    @GetMapping("/practice-words")
    public List<PracticeWordResponse> getPracticeWords() {

        List<Recording> recordings = recordingRepository.findAll();

        return recordings.stream()

                .map(r -> new PracticeWordResponse(
                        r.getId(),
                        r.getWord(),
                        r.getMeaning(),
                        "/api/recordings/" + r.getId() + "/stream-word",
                        r.getSentence(),
                        r.getSentenceMeaning(),

                        "/api/recordings/" + r.getId() + "/stream-sentence",
                        r.getAuthorName()
                ))
                .toList();
    }

    @GetMapping("/{id}/stream-word")
    public ResponseEntity<StreamingResponseBody> streamWord(
            @PathVariable Long id,
            @RequestParam(defaultValue = "WORD") String type
    ) {

        Recording rec = recordingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recording not found"));

        String objectKey;
        InputStream input;


        try {
            if ("WORD".equalsIgnoreCase(type)) {
                objectKey = rec.getWordObjectKey();
                input = storageService.getWord(objectKey);
            } else {
                objectKey = rec.getSentenceObjectKey();
                input = storageService.getSentence(objectKey);
            }

            StreamingResponseBody body = outputStream -> {
                input.transferTo(outputStream);
            };
            System.out.println("Streaming type=" + type);
            System.out.println("objectKey=" + objectKey);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/webm"))
                    .body(body);

        } catch (Exception e) {
            throw new RuntimeException("Failed to stream audio", e);
        }
    }
    @GetMapping("/{id}/stream-sentence")
    public ResponseEntity<StreamingResponseBody> streamSentence(@PathVariable Long id,
                                                                @RequestParam(defaultValue = "WORD") String type) {
        Recording rec = recordingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recording not found"));
        String objectKey;
        InputStream input;

        try {
            if ("SENTENCE".equalsIgnoreCase(type)) {
                objectKey = rec.getWordObjectKey();
                input = storageService.getSentence(objectKey);
            } else {
                objectKey = rec.getSentenceObjectKey();
                input = storageService.getSentence(objectKey);
            }
            StreamingResponseBody body = outputStream -> {
                input.transferTo(outputStream);
            };
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/webm"))
                    .body(body);

        } catch (Exception e) {
            throw new RuntimeException("Failed to stream audio", e);
        }

    }


    // ⬆️ UPLOAD WORD (creates the record)
    @PostMapping(value = "/upload-word", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadWord(
            @RequestParam("word") String word,
            @RequestParam("meaning") String meaning,
            @RequestParam("file") MultipartFile file,
            @RequestParam("authorname")  String authorName,
            @RequestParam("dialect")  String dialect,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.parseLong(jwt.getSubject());

        log.info("UPLOAD WORD: name={} contentType={} size={}",
                file.getOriginalFilename(), file.getContentType(), file.getSize());

        RecordingRequest req = new RecordingRequest();
        req.setWord(word);
        req.setMeaning(meaning);
        req.setFile(file);
        req.setAuthorName(authorName);
        req.setDialect(dialect);

        Recording saved = recordingService.saveRecording(userId, req);
System.out.println("record id: "+saved.getId());//this prints id correctly
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

    @GetMapping("/{id}/url")
    public ResponseEntity<String> getAudioUrl(
            @PathVariable Long id,
            @RequestParam(defaultValue = "WORD") String type,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.parseLong(jwt.getSubject());

        Recording rec = recordingRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Recording not found"));

        String objectKey = resolveObjectKey(rec, type);
        String url = minioStorageService.getUrl(objectKey, type);

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
    @PostMapping(value = "/assess", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> assessLearnerAudio(
            @RequestParam("recordingId") Long recordingId,
            @RequestParam("mode") String mode, // WORD or SENTENCE
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.parseLong(jwt.getSubject());


        AssessmentResponse response =
                pronunciationAssessmentService.assess(recordingId, userId, mode, file);

        return ResponseEntity.ok(response);
    }
}




