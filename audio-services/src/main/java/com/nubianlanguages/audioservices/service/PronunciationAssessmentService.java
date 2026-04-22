package com.nubianlanguages.audioservices.service;

import com.nubianlanguages.audioservices.dto.AssessmentResponse;
import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.repository.RecordingRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class PronunciationAssessmentService {

    private final RecordingRepository recordingRepository;
    private final AudioConversionService audioConversionService;
    private final WhisperTranscriptionService whisperTranscriptionService;
    private final TextComparisonService textComparisonService;

    public PronunciationAssessmentService(
            RecordingRepository recordingRepository,
            AudioConversionService audioConversionService,
            WhisperTranscriptionService whisperTranscriptionService,
            TextComparisonService textComparisonService
    ) {
        this.recordingRepository = recordingRepository;
        this.audioConversionService = audioConversionService;
        this.whisperTranscriptionService = whisperTranscriptionService;
        this.textComparisonService = textComparisonService;
    }

    public AssessmentResponse assess(Long recordingId, Long userId, String mode, MultipartFile file) {
        Recording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new RuntimeException("Recording not found"));

        String expectedText;
        if ("SENTENCE".equalsIgnoreCase(mode)) {
            expectedText = recording.getSentence();
        } else {
            expectedText = recording.getWord();
        }

        File uploaded = null;
        File wav = null;
        try {
            String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();

            if (original.endsWith(".wav")) {
                wav = audioConversionService.multipartToTempFile(file, ".wav");
            } else {
                uploaded = audioConversionService.multipartToTempFile(file, ".webm");
                wav = audioConversionService.convertWebmToWav(uploaded);
            }

            String transcript = whisperTranscriptionService.transcribe(wav, "sw"); // or your target language
            String normalizedExpected = textComparisonService.normalize(expectedText);
            String normalizedActual = textComparisonService.normalize(transcript);
            double score = textComparisonService.similarityPercent(expectedText, transcript);
            String rating = textComparisonService.rating(score);

            return new AssessmentResponse(
                    recordingId,
                    mode,
                    expectedText,
                    transcript,
                    normalizedExpected,
                    normalizedActual,
                    score,
                    rating
            );

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (uploaded != null && uploaded.exists()) uploaded.delete();
            if (wav != null && wav.exists()) wav.delete();
        }
    }
}