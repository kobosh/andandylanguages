
package com.nubianlanguages.pronounciationservice.service;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
public class VoskSpeechService {
    @Value("${vosk.model-path}")
    private String modelPath;

    private Model model;

    @PostConstruct
    public void init() throws IOException {
        File dir = new File(modelPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalStateException("Vosk model path does not exist: " + modelPath);
        }

        this.model = new Model(modelPath);
        System.out.println("Vosk model loaded from: " + modelPath);
    }

    @PreDestroy
    public void destroy() {
        if (model != null) {
            model.close();
        }
    }
    public String recognizeOpen(File wavFile) throws Exception {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(wavFile)) {
            AudioFormat format = ais.getFormat();

            Recognizer recognizer = new Recognizer(model, format.getSampleRate());

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = ais.read(buffer)) != -1) {
                recognizer.acceptWaveForm(buffer, bytesRead);
            }

            String finalJson = recognizer.getFinalResult();
            recognizer.close();

            System.out.println("Vosk final JSON: " + finalJson);

            return extractText(finalJson);
        }
    }
    public String recognize(File wavFile, String expectedText) throws Exception {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(wavFile)) {
            AudioFormat format = ais.getFormat();

            String grammar = buildGrammar(expectedText);
            System.out.println("Using grammar: " + grammar);

            Recognizer recognizer = new Recognizer(model, format.getSampleRate(), grammar);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = ais.read(buffer)) != -1) {
                recognizer.acceptWaveForm(buffer, bytesRead);
            }

            String finalJson = recognizer.getFinalResult();
            recognizer.close();

            System.out.println("Vosk final JSON: " + finalJson);

            return extractText(finalJson);
        }
    }

    private String buildGrammar(String expectedText) {
        String cleaned = expectedText == null ? "" : expectedText.toLowerCase()
                .replace("\"", "")
                .trim();

        if (cleaned.isBlank()) {
            return "[\"[unk]\"]";
        }

        return "[\"" + cleaned + "\", \"[unk]\"]";
    }

      /*  public String recognize(File wavFile) throws Exception {
            try (AudioInputStream ais = AudioSystem.getAudioInputStream(wavFile)) {
                AudioFormat format = ais.getFormat();

                System.out.println("Sample rate: " + format.getSampleRate());
                System.out.println("Channels: " + format.getChannels());
                System.out.println("Encoding: " + format.getEncoding());

                if (format.getChannels() != 1) {
                    throw new IllegalArgumentException("WAV must be mono.");
                }

                Recognizer recognizer = new Recognizer(model, format.getSampleRate());

                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = ais.read(buffer)) != -1) {
                    recognizer.acceptWaveForm(buffer, bytesRead);
                }

                String finalJson = recognizer.getFinalResult();
                recognizer.close();

                System.out.println("Vosk final JSON: " + finalJson);

                return extractText(finalJson);
            }
        }*/

        private String extractText(String json) {
            if (json == null || json.isBlank()) {
                return "";
            }

            int keyIndex = json.indexOf("\"text\"");
            if (keyIndex < 0) {
                return "";
            }

            int colonIndex = json.indexOf(':', keyIndex);
            int firstQuote = json.indexOf('"', colonIndex + 1);
            int secondQuote = json.indexOf('"', firstQuote + 1);

            if (firstQuote < 0 || secondQuote < 0) {
                return "";
            }

            return json.substring(firstQuote + 1, secondQuote).trim();
        }
    }
