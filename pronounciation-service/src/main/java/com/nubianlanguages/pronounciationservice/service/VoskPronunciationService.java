
package com.nubianlanguages.pronounciationservice.service;

import com.nubianlanguages.pronounciationservice.dto.PronunciationResponse;
import com.nubianlanguages.pronounciationservice.dto.WordResultDto;
import com.nubianlanguages.pronounciationservice.entity.PronunciationAssessmentEntity;
import com.nubianlanguages.pronounciationservice.repository.PronunciationAssessmentRepository;
import com.nubianlanguages.pronounciationservice.util.FeedbackBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class VoskPronunciationService implements PronunciationService {

    private final PronunciationAssessmentRepository repository;
    private final VoskSpeechService voskSpeechService;
    private final AudioConversionService audioConversionService;
    private  final PhonemeService phonemeService;

    public VoskPronunciationService(
            PronunciationAssessmentRepository repository,
            VoskSpeechService voskSpeechService,
            AudioConversionService audioConversionService,
            PhonemeService phonemeService
    ) {
        this.repository = repository;
        this.voskSpeechService = voskSpeechService;
        this.audioConversionService = audioConversionService;
        this.phonemeService = phonemeService;
    }

    @Override
    public PronunciationResponse assess(MultipartFile audio,
                                        String expectedText,
                                        String languageCode,
                                        Long recordingId) {

        validate(audio, expectedText);

        File uploadedFile = null;
        File normalizedFile = null;

        try {
            System.out.println("original file name "+audio.getOriginalFilename());
           // System.out.println("original file path "+audio.g);
            uploadedFile = File.createTempFile("upload-", getSafeSuffix(audio.getOriginalFilename()));
            audio.transferTo(uploadedFile);

            normalizedFile = audioConversionService.normalizeToMono16kWav(uploadedFile);

            //String recognizedText = voskSpeechService.recognize(normalizedFile).trim();
            //String recognizedText = voskSpeechService.recognize(normalizedFile, expectedText).trim();

            String recognizedText = voskSpeechService.recognizeOpen(normalizedFile).trim();
            var phonemeFeedback = phonemeService.compare(expectedText, recognizedText);


            double overallScore = score(expectedText, recognizedText);
            double accuracyScore = overallScore;
            double fluencyScore = recognizedText.isBlank() ? 0.0 : overallScore;
            double completenessScore = computeCompleteness(expectedText, recognizedText);
            Double prosodyScore = null;

            List<String> feedback = FeedbackBuilder.build(
                    overallScore, accuracyScore, fluencyScore, completenessScore, prosodyScore
            );
            for (var pf : phonemeFeedback) {
                if ("missing".equals(pf.getIssue())) {
                    feedback.add("Missing sound: " + pf.getExpectedPhoneme());
                } else if ("extra".equals(pf.getIssue())) {
                    feedback.add("Unexpected extra sound: " + pf.getActualPhoneme());
                } else if ("different".equals(pf.getIssue())) {
                    feedback.add("Expected sound " + pf.getExpectedPhoneme()
                            + " but heard " + pf.getActualPhoneme());
                }
            }
            List<WordResultDto> wordResults = buildWordResults(recognizedText);

            PronunciationAssessmentEntity entity = new PronunciationAssessmentEntity();
            entity.setRecordingId(recordingId);
            entity.setExpectedText(expectedText);
            entity.setRecognizedText(recognizedText);
            entity.setLanguageCode(languageCode == null || languageCode.isBlank() ? "en-US" : languageCode);
            entity.setOverallScore(overallScore);
            entity.setAccuracyScore(accuracyScore);
            entity.setFluencyScore(fluencyScore);
            entity.setCompletenessScore(completenessScore);
            entity.setProsodyScore(prosodyScore);
            entity.setFeedbackJson(String.join(" | ", feedback));
            entity.setRawAzureJson(null);


            PronunciationAssessmentEntity saved = repository.save(entity);

            PronunciationResponse response = new PronunciationResponse();
            response.setAssessmentId(saved.getId());
            response.setRecordingId(recordingId);
           // response.setExpectedText(expectedText);
            response.setRecognizedText(recognizedText);
            response.setLanguageCode(entity.getLanguageCode());
            response.setOverallScore(overallScore);
            response.setAccuracyScore(accuracyScore);
            response.setFluencyScore(fluencyScore);
            response.setCompletenessScore(completenessScore);
            response.setProsodyScore(prosodyScore);
            response.setFeedback(feedback);
            response.setWordResults(wordResults);
            response.setCreatedAt(Instant.now());
            response.setPhonemeFeedback(phonemeFeedback);

            return response;

        } catch (Exception e) {
            throw new RuntimeException("Failed to assess pronunciation: " + e.getMessage(), e);
        } finally {
            deleteQuietly(uploadedFile);
            deleteQuietly(normalizedFile);
        }
    }

    private void validate(MultipartFile audio, String expectedText) {
        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("Audio file is required.");
        }
        if (expectedText == null || expectedText.isBlank()) {
            throw new IllegalArgumentException("expectedText is required.");
        }
    }

    private String getSafeSuffix(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".wav";
        }
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        return ext.length() > 10 ? ".wav" : ext;
    }

    private void deleteQuietly(File file) {
        if (file != null) {
            try {
                Files.deleteIfExists(file.toPath());
            } catch (Exception ignored) {
            }
        }
    }

    private List<WordResultDto> buildWordResults(String recognizedText) {
        List<WordResultDto> wordResults = new ArrayList<>();
        if (!recognizedText.isBlank()) {
            for (String word : recognizedText.split("\\s+")) {
                wordResults.add(new WordResultDto(word, null, null));
            }

        }

        return wordResults;
    }

    private double computeCompleteness(String expected, String actual) {
        String e = normalize(expected);
        String a = normalize(actual);

        if (e.isBlank()) {
            return 0.0;
        }
        if (a.equals(e)) {
            return 100.0;
        }
        if (a.isBlank()) {
            return 0.0;
        }

        String[] expectedWords = e.split("\\s+");
        String[] actualWords = a.split("\\s+");

        int matched = 0;
        for (String ew : expectedWords) {
            for (String aw : actualWords) {
                if (ew.equals(aw)) {
                    matched++;
                    break;
                }
            }
        }

        return Math.max(0.0, Math.min(100.0, (matched * 100.0) / expectedWords.length));
    }

    private double score(String expected, String actual) {
        String e = normalize(expected);
        String a = normalize(actual);

        if (e.isBlank() || a.isBlank()) {
            return 0.0;
        }

        int distance = levenshtein(e, a);
        int maxLen = Math.max(e.length(), a.length());

        if (maxLen == 0) {
            return 100.0;
        }

        double score = (1.0 - ((double) distance / maxLen)) * 100.0;
        return Math.max(0.0, Math.min(100.0, Math.round(score * 10.0) / 10.0));
    }

    private String normalize(String input) {
        return input == null ? "" : input.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }
}
//package com.nubianlanguages.pronounciationservice.service;
//
//import com.nubianlanguages.pronounciationservice.dto.PronunciationResponse;
//import com.nubianlanguages.pronounciationservice.dto.WordResultDto;
//import com.nubianlanguages.pronounciationservice.entity.PronunciationAssessmentEntity;
//import com.nubianlanguages.pronounciationservice.repository.PronunciationAssessmentRepository;
//import com.nubianlanguages.pronounciationservice.util.FeedbackBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import org.vosk.Model;
//import org.vosk.Recognizer;
//
//import javax.sound.sampled.AudioFormat;
//import javax.sound.sampled.AudioInputStream;
//import javax.sound.sampled.AudioSystem;
//import java.io.File;
//import java.io.FileInputStream;
//import java.nio.file.Files;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class VoskPronunciationService implements PronunciationService {
//
//    private final PronunciationAssessmentRepository repository;
//
//    @Value("${vosk.model-path}")
//    private String modelPath;
//
//    public VoskPronunciationService(PronunciationAssessmentRepository repository) {
//        this.repository = repository;
//    }
//
//    @Override
//    public PronunciationResponse assess(MultipartFile audio,
//                                        String expectedText,
//                                        String languageCode,
//                                        Long recordingId) {
//
//        validate(audio, expectedText);
//
//        File tempFile = null;
//        Model model = null;
//
//        try {
//            tempFile = File.createTempFile("pronunciation-", ".wav");
//            audio.transferTo(tempFile);
//
//            model = new Model(modelPath);
//
//            String recognizedText = recognizeWav(tempFile, model).trim();
//
//            int overall = score(expectedText, recognizedText);
//            double overallScore = (double) overall;
//            double accuracyScore = overallScore;
//            double fluencyScore = recognizedText.isBlank() ? 0.0 : Math.max(60.0, overallScore);
//            double completenessScore = recognizedText.isBlank() ? 0.0 :
//                    (recognizedText.equalsIgnoreCase(expectedText.trim()) ? 100.0 : 80.0);
//            Double prosodyScore = null;
//
//            List<String> feedback = FeedbackBuilder.build(
//                    overallScore, accuracyScore, fluencyScore, completenessScore, prosodyScore
//            );
//
//            List<WordResultDto> wordResults = new ArrayList<>();
//            if (!recognizedText.isBlank()) {
//                for (String word : recognizedText.split("\\s+")) {
//                    wordResults.add(new WordResultDto(word, null, null));
//                }
//            }
//
//            PronunciationAssessmentEntity entity = new PronunciationAssessmentEntity();
//            entity.setRecordingId(recordingId);
//            entity.setExpectedText(expectedText);
//            entity.setRecognizedText(recognizedText);
//            entity.setLanguageCode(languageCode == null || languageCode.isBlank() ? "en-US" : languageCode);
//            entity.setOverallScore(overallScore);
//            entity.setAccuracyScore(accuracyScore);
//            entity.setFluencyScore(fluencyScore);
//            entity.setCompletenessScore(completenessScore);
//            entity.setProsodyScore(prosodyScore);
//            entity.setFeedbackJson(String.join(" | ", feedback));
//            entity.setRawAzureJson(null);
//
//            PronunciationAssessmentEntity saved = repository.save(entity);
//
//            PronunciationResponse response = new PronunciationResponse();
//            response.setAssessmentId(saved.getId());
//            response.setRecordingId(recordingId);
//            response.setExpectedText(expectedText);
//            response.setRecognizedText(recognizedText);
//            response.setLanguageCode(entity.getLanguageCode());
//            response.setOverallScore(overallScore);
//            response.setAccuracyScore(accuracyScore);
//            response.setFluencyScore(fluencyScore);
//            response.setCompletenessScore(completenessScore);
//            response.setProsodyScore(prosodyScore);
//            response.setFeedback(feedback);
//            response.setWordResults(wordResults);
//            response.setCreatedAt(Instant.now());
//
//            return response;
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to assess pronunciation: " + e.getMessage(), e);
//        } finally {
//            if (model != null) {
//                model.close();
//            }
//            if (tempFile != null) {
//                try {
//                    Files.deleteIfExists(tempFile.toPath());
//                } catch (Exception ignored) {
//                }
//            }
//        }
//    }
//
//      private String recognizeWav(File wavFile, Model model) throws Exception {
//       try (AudioInputStream ais = AudioSystem.getAudioInputStream(wavFile)) {
//           AudioFormat format = ais.getFormat();
//
//           System.out.println("Sample rate: " + format.getSampleRate());
//           System.out.println("Channels: " + format.getChannels());
//           System.out.println("Frame size: " + format.getFrameSize());
//           System.out.println("Encoding: " + format.getEncoding());
//
//           if (format.getChannels() != 1) {
//               throw new IllegalArgumentException("WAV must be mono.");
//           }
//
//           Recognizer recognizer = new Recognizer(model, format.getSampleRate());
//
//           byte[] buffer = new byte[4096];
//           int bytesRead;
//
//           while ((bytesRead = ais.read(buffer)) != -1) {
//               recognizer.acceptWaveForm(buffer, bytesRead);
//           }
//
//           String finalJson = recognizer.getFinalResult();
//           recognizer.close();
//
//           System.out.println("Vosk final JSON: " + finalJson);
//
//           return extractText(finalJson);
//       }
//   }
//    private String extractText(String json) {
//        if (json == null || json.isBlank()) {
//            return "";
//        }
//
//        int keyIndex = json.indexOf("\"text\"");
//        if (keyIndex < 0) {
//            return "";
//        }
//
//        int colonIndex = json.indexOf(':', keyIndex);
//        int firstQuote = json.indexOf('"', colonIndex + 1);
//        int secondQuote = json.indexOf('"', firstQuote + 1);
//
//        if (firstQuote < 0 || secondQuote < 0) {
//            return "";
//        }
//
//        return json.substring(firstQuote + 1, secondQuote).trim();
//    }
//
//    private int score(String expected, String actual) {
//        String e = normalize(expected);
//        String a = normalize(actual);
//
//        if (e.isBlank() || a.isBlank()) {
//            return 0;
//        }
//
//        int distance = levenshtein(e, a);
//        int maxLen = Math.max(e.length(), a.length());
//
//        if (maxLen == 0) {
//            return 100;
//        }
//
//        int score = (int) Math.round((1.0 - ((double) distance / maxLen)) * 100.0);
//        return Math.max(0, score);
//    }
//
//    private String normalize(String input) {
//        return input == null ? "" : input.toLowerCase().replaceAll("[^a-z0-9\\s]", "").trim();
//    }
//
//    private int levenshtein(String a, String b) {
//        int[][] dp = new int[a.length() + 1][b.length() + 1];
//
//        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
//        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
//
//        for (int i = 1; i <= a.length(); i++) {
//            for (int j = 1; j <= b.length(); j++) {
//                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
//                dp[i][j] = Math.min(
//                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
//                        dp[i - 1][j - 1] + cost
//                );
//            }
//        }
//
//        return dp[a.length()][b.length()];
//    }
//
//    private void validate(MultipartFile audio, String expectedText) {
//        if (audio == null || audio.isEmpty()) {
//            throw new IllegalArgumentException("Audio file is required.");
//        }
//        if (expectedText == null || expectedText.isBlank()) {
//            throw new IllegalArgumentException("expectedText is required.");
//        }
//        if (modelPath == null || modelPath.isBlank()) {
//            throw new IllegalStateException("vosk.model-path is missing.");
//        }
//        File modelDir = new File(modelPath);
//        if (!modelDir.exists() || !modelDir.isDirectory()) {
//            throw new IllegalStateException("Vosk model path does not exist: " + modelPath);
//        }
//    }
//}
//package com.nubianlanguages.pronounciationservice.service;
//
//import com.microsoft.cognitiveservices.speech.*;
//import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
//import com.nubianlanguages.pronounciationservice.dto.PronunciationResponse;
//import com.nubianlanguages.pronounciationservice.dto.WordResultDto;
//import com.nubianlanguages.pronounciationservice.entity.PronunciationAssessmentEntity;
//import com.nubianlanguages.pronounciationservice.repository.PronunciationAssessmentRepository;
//import com.nubianlanguages.pronounciationservice.util.FeedbackBuilder;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.nio.file.Files;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class AzurePronunciationService implements PronunciationService {
//
//    private final PronunciationAssessmentRepository repository;
//
//    @Value("${azure.speech.key}")
//    private String speechKey;
//
//    @Value("${azure.speech.region}")
//    private String speechRegion;
//
//    @Value("${azure.speech.language:en-US}")
//    private String defaultLanguage;
//
//    public AzurePronunciationService(PronunciationAssessmentRepository repository) {
//        this.repository = repository;
//    }
//
//    @Override
//    public PronunciationResponse assess(MultipartFile audio,
//                                        String expectedText,
//                                        String languageCode,
//                                        Long recordingId) {
//
//        validate(audio, expectedText);
//
//        String effectiveLanguage = (languageCode == null || languageCode.isBlank())
//                ? defaultLanguage
//                : languageCode.trim();
//
//        File tempFile = null;
//
//        try {
//            tempFile = File.createTempFile("pronunciation-", "-" + safeFileName(audio.getOriginalFilename()));
//            audio.transferTo(tempFile);
//
//            SpeechConfig speechConfig = SpeechConfig.fromSubscription(speechKey, speechRegion);
//            speechConfig.setSpeechRecognitionLanguage(effectiveLanguage);
//
//            AudioConfig audioConfig = AudioConfig.fromWavFileInput(tempFile.getAbsolutePath());
//            SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig);
//
//            PronunciationAssessmentConfig pronunciationConfig =
//                    new PronunciationAssessmentConfig(
//                            expectedText,
//                            PronunciationAssessmentGradingSystem.HundredMark,
//                            PronunciationAssessmentGranularity.Phoneme,
//                            true
//                    );
//
//            pronunciationConfig.applyTo(recognizer);
//
//            SpeechRecognitionResult result = recognizer.recognizeOnceAsync().get();
//            System.out.println("Recognition reason: " + result.getReason());
//            System.out.println("Recognized text: [" + result.getText() + "]");
//            System.out.println("Raw JSON: " +
//                    result.getProperties().getProperty(PropertyId.SpeechServiceResponse_JsonResult));
//
//            if (result.getReason() == ResultReason.Canceled) {
//                CancellationDetails details = CancellationDetails.fromResult(result);
//                System.out.println("Cancellation reason: " + details.getReason());
//                System.out.println("Cancellation error code: " + details.getErrorCode());
//                System.out.println("Cancellation error details: " + details.getErrorDetails());
//
//                throw new RuntimeException(
//                        "Speech recognition canceled. Reason=" + details.getReason()
//                                + ", errorCode=" + details.getErrorCode()
//                                + ", details=" + details.getErrorDetails()
//                );
//            }
//
//            if (result.getReason() != ResultReason.RecognizedSpeech) {
//                throw new RuntimeException("Speech was not recognized. Reason: " + result.getReason());
//            }
//
//            if (result == null) {
//                throw new RuntimeException("Azure returned no result.");
//            }
//
//            String recognizedText = result.getText();
//            PronunciationAssessmentResult paResult = PronunciationAssessmentResult.fromResult(result);
//
//            Double overallScore = paResult != null ? paResult.getPronunciationScore() : null;
//            Double accuracyScore = paResult != null ? paResult.getAccuracyScore() : null;
//            Double fluencyScore = paResult != null ? paResult.getFluencyScore() : null;
//            Double completenessScore = paResult != null ? paResult.getCompletenessScore() : null;
//
//            Double prosodyScore = null;
//            try {
//                prosodyScore = paResult != null ? paResult.getProsodyScore() : null;
//            } catch (Exception ignored) {
//                // Some environments / SDK combinations may not expose this consistently.
//            }
//
//            List<String> feedback = FeedbackBuilder.build(
//                    overallScore, accuracyScore, fluencyScore, completenessScore, prosodyScore
//            );
//
//            List<WordResultDto> wordResults = new ArrayList<>();
//            // For v1, keep it simple.
//            // In a later version, parse the JSON detail payload to extract per-word results.
//            if (recognizedText != null && !recognizedText.isBlank()) {
//                for (String word : recognizedText.split("\\s+")) {
//                    wordResults.add(new WordResultDto(word, null, null));
//                }
//            }
//
//            PronunciationAssessmentEntity entity = new PronunciationAssessmentEntity();
//            entity.setRecordingId(recordingId);
//            entity.setExpectedText(expectedText);
//            entity.setRecognizedText(recognizedText);
//            entity.setLanguageCode(effectiveLanguage);
//            entity.setOverallScore(overallScore);
//            entity.setAccuracyScore(accuracyScore);
//            entity.setFluencyScore(fluencyScore);
//            entity.setCompletenessScore(completenessScore);
//            entity.setProsodyScore(prosodyScore);
//            entity.setFeedbackJson(String.join(" | ", feedback));
//            entity.setRawAzureJson(result.getProperties().getProperty(PropertyId.SpeechServiceResponse_JsonResult));
//
//            PronunciationAssessmentEntity saved = repository.save(entity);
//
//            PronunciationResponse response = new PronunciationResponse();
//            response.setAssessmentId(saved.getId());
//            response.setRecordingId(recordingId);
//            response.setExpectedText(expectedText);
//            response.setRecognizedText(recognizedText);
//            response.setLanguageCode(effectiveLanguage);
//            response.setOverallScore(overallScore);
//            response.setAccuracyScore(accuracyScore);
//            response.setFluencyScore(fluencyScore);
//            response.setCompletenessScore(completenessScore);
//            response.setProsodyScore(prosodyScore);
//            response.setFeedback(feedback);
//            response.setWordResults(wordResults);
//            response.setCreatedAt(Instant.now());
//
//            recognizer.close();
//            audioConfig.close();
//            speechConfig.close();
//
//            return response;
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to assess pronunciation: " + e.getMessage(), e);
//        } finally {
//            if (tempFile != null) {
//                try {
//                    Files.deleteIfExists(tempFile.toPath());
//                } catch (Exception ignored) {
//                }
//            }
//        }
//    }
//
//    private void validate(MultipartFile audio, String expectedText) {
//        if (speechKey == null || speechKey.isBlank()) {
//            throw new IllegalStateException("AZURE_SPEECH_KEY is missing.");
//        }
//        if (speechRegion == null || speechRegion.isBlank()) {
//            throw new IllegalStateException("AZURE_SPEECH_REGION is missing.");
//        }
//        if (audio == null || audio.isEmpty()) {
//            throw new IllegalArgumentException("Audio file is required.");
//        }
//        if (expectedText == null || expectedText.isBlank()) {
//            throw new IllegalArgumentException("expectedText is required.");
//        }
//    }
//
//    private String safeFileName(String original) {
//        if (original == null || original.isBlank()) {
//            return "audio.wav";
//        }
//        return original.replaceAll("[^a-zA-Z0-9._-]", "_");
//    }
//}