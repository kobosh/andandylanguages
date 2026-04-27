package com.nubianlanguages.pronounciationservice.service;

import com.nubianlanguages.pronounciationservice.dto.PronunciationResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class WhisperPronunciationAssessmentService {

    private final FfmpegAudioConversionService ffmpegAudioConversionService;
    private final WhisperSpeechService whisperSpeechService;

    public WhisperPronunciationAssessmentService(
            FfmpegAudioConversionService ffmpegAudioConversionService,
            WhisperSpeechService whisperSpeechService
    ) {
        this.ffmpegAudioConversionService = ffmpegAudioConversionService;
        this.whisperSpeechService = whisperSpeechService;
    }

    public PronunciationResponse assess(MultipartFile audio,
                                        String expectedText,

                                        String languageCode,
                                        Long recordingId) throws Exception {


        File uploaded = File.createTempFile("learner-", ".webm");
        File wavFile = null;

        try {
            audio.transferTo(uploaded);

            wavFile = ffmpegAudioConversionService.convertToWav(uploaded);
                      String recognizedText = whisperSpeechService.transcribe(wavFile);

            double accuracy = scoreSimilarity(expectedText, recognizedText);
            double completeness = recognizedText.isBlank() ? 0 : 100;
            double overall = (accuracy + completeness) / 2.0;

           PronunciationResponse response = new PronunciationResponse();
            response.setRecordingId(recordingId);
            response.setExpectedText(expectedText);
            response.setRecognizedText(recognizedText);
            response.setAccuracyScore(accuracy);
            response.setCompletenessScore(completeness);
            response.setOverallScore(overall);

            return response;

        } finally {
            if (uploaded.exists()) uploaded.delete();
            if (wavFile != null && wavFile.exists()) wavFile.delete();
        }
    }

    private double scoreSimilarity(String expected, String actual) {
        String e = normalize(expected);
        String a = normalize(actual);

        if (e.isBlank() && a.isBlank()) return 100.0;
        if (e.isBlank() || a.isBlank()) return 0.0;
        if (e.equals(a)) return 100.0;

        int distance = levenshtein(e, a);
        int maxLen = Math.max(e.length(), a.length());

        return Math.max(0, (1.0 - ((double) distance / maxLen)) * 100.0);
    }

    private String normalize(String s) {
        return s == null ? "" : s.toLowerCase().replaceAll("[^\\p{L}\\p{Nd}\\s]", "").replaceAll("\\s+", " ").trim();
    }

    private int levenshtein(String s1, String s2) {
        int[] prev = new int[s2.length() + 1];
        int[] curr = new int[s2.length() + 1];

        for (int j = 0; j <= s2.length(); j++) prev[j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            curr[0] = i;
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(
                        Math.min(curr[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost
                );
            }
            int[] temp = prev;
            prev = curr;
            curr = temp;
        }

        return prev[s2.length()];
    }
    public String transcribe(MultipartFile webmFile) throws IOException {
        if (webmFile == null || webmFile.isEmpty()) {
            throw new IllegalArgumentException("Audio file is required.");
        }

        File uploaded = File.createTempFile("learner-", ".webm");
        File wavFile = null;

        try {
            webmFile.transferTo(uploaded);
            wavFile = ffmpegAudioConversionService.convertToWav(uploaded);

            return whisperSpeechService.transcribe(wavFile).trim();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to transcribe uploaded WEBM audio", ex);

        } finally {
            if (uploaded.exists() && !uploaded.delete()) {
                uploaded.deleteOnExit();
            }
            if (wavFile != null && wavFile.exists() && !wavFile.delete()) {
                wavFile.deleteOnExit();
            }
        }
    }
}

