package com.nubianlanguages.pronounciationservice.controller;

import com.nubianlanguages.pronounciationservice.dto.ErrorResponse;
import com.nubianlanguages.pronounciationservice.dto.PronunciationResponse;
import com.nubianlanguages.pronounciationservice.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

@RestController
@RequestMapping("/api/pronunciation")
@Validated
public class PronunciationController {

    private final WhisperPronunciationAssessmentService whisperPronunciationAssessmentService;
private final FfmpegAudioConversionService ffmpegaudioConversionService;
private  final WhisperSpeechService whisperSpeechService;

    public PronunciationController(WhisperPronunciationAssessmentService pronunciationService, AudioConversionService audioConversionService, FfmpegAudioConversionService ffmpegaudioConversionService,
                                   WhisperSpeechService whisperSpeechService) {
        this.whisperPronunciationAssessmentService = pronunciationService;
        this.ffmpegaudioConversionService = ffmpegaudioConversionService;
        this.whisperSpeechService = whisperSpeechService;



    }
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> transcribe(
            @RequestPart("audio") MultipartFile audio,
            @RequestParam(value = "languageCode", required = false, defaultValue = "sw") String languageCode,
            @RequestParam(value = "recordingId", required = false) Long recordingId
    ) throws Exception {
            System.out.println("CALLING Contrl transcribe "+audio.getOriginalFilename());
        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("Audio file is required");
        }

        File tempFile = File.createTempFile("upload-", ".webm");

        try {
            audio.transferTo(tempFile);
          tempFile=  ffmpegaudioConversionService.convertToWav(tempFile);

            // call whisper
            String transcript = whisperSpeechService.transcribe(tempFile);

            return ResponseEntity.ok(Map.of(
                    "recordingId", recordingId != null ? recordingId.toString() : "",
                    "transcript", transcript
            ));

        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    @PostMapping(value = "/assess", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PronunciationResponse> assess(
            @RequestPart("audio") MultipartFile audio,
            @RequestParam("expectedText") String expectedText,
            @RequestParam(value = "languageCode", required = false, defaultValue = "sw") String languageCode,
            @RequestParam(value = "recordingId", required = false) Long recordingId
    ) throws Exception {

       PronunciationResponse result =
                whisperPronunciationAssessmentService.
                        assess(audio, expectedText, languageCode, recordingId);

        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/convert-webm", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ByteArrayResource> convertWebmToWav(
            @RequestPart("audio") MultipartFile audio
    ) throws Exception {

        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("Audio file is required.");
        }

        String originalFilename = audio.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".webm")) {
            throw new IllegalArgumentException("Only .webm files are supported by this endpoint.");
        }

        File uploadedFile = File.createTempFile("upload-", ".webm");
        File convertedFile = null;

        try {
            audio.transferTo(uploadedFile);


            convertedFile = ffmpegaudioConversionService.convertToWav(uploadedFile);

            byte[] wavBytes = Files.readAllBytes(convertedFile.toPath());
            ByteArrayResource resource = new ByteArrayResource(wavBytes);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("audio/wav"))
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"converted.wav\""
                    )
                    .contentLength(wavBytes.length)
                    .body(resource);
        }


          finally {
         Files.deleteIfExists(uploadedFile.toPath());
           if (convertedFile != null) {
                Files.deleteIfExists(convertedFile.toPath());
            }
        }
    }


    private String normalize(String text) {
        if (text == null) return "";
        return text.toLowerCase()
                .replaceAll("[^a-z\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }
    private int levenshtein(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {

                if (i == 0)
                    dp[i][j] = j;
                else if (j == 0)
                    dp[i][j] = i;
                else {
                    dp[i][j] = Math.min(
                            Math.min(
                                    dp[i - 1][j] + 1,
                                    dp[i][j - 1] + 1
                            ),
                            dp[i - 1][j - 1] +
                                    (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }
    private int similarityScore(String expected, String actual) {
        int distance = levenshtein(expected, actual);
        int maxLen = Math.max(expected.length(), actual.length());

        return (int)((1.0 - (double)distance / maxLen) * 100);
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex,
                                                          HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        400,
                        "Bad Request",
                        ex.getMessage(),
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                       HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(
                        500,
                        "Internal Server Error",
                        ex.getMessage(),
                        request.getRequestURI()
                )
        );
    }
}