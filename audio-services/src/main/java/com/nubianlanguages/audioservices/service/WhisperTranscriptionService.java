package com.nubianlanguages.audioservices.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;

@Service
public class WhisperTranscriptionService {

    @Value("${whisper.cli.path}")
    private String whisperCliPath;

    @Value("${whisper.model.path}")
    private String whisperModelPath;

    @Value("${whisper.work-dir}")
    private String whisperWorkDir;

    public String transcribe(File wavFile, String language) {
        try {
            String outputBase = new File(whisperWorkDir, "whisper-" + System.currentTimeMillis()).getAbsolutePath();

            ProcessBuilder pb = new ProcessBuilder(
                    whisperCliPath,
                    "-m", whisperModelPath,
                    "-f", wavFile.getAbsolutePath(),
                    "-l", language,
                    "-otxt",
                    "-of", outputBase
            );

            pb.redirectErrorStream(true);
            Process process = pb.start();

            String logs;
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                logs = br.lines().reduce("", (a, b) -> a + b + System.lineSeparator());
            }

            int exit = process.waitFor();
            if (exit != 0) {
                throw new RuntimeException("Whisper failed: " + logs);
            }

            File txtFile = new File(outputBase + ".txt");
            if (!txtFile.exists()) {
                throw new RuntimeException("Whisper output text file not found");
            }

            return Files.readString(txtFile.toPath()).trim();

        } catch (Exception e) {
            throw new RuntimeException("Failed to transcribe audio", e);
        }
    }
}