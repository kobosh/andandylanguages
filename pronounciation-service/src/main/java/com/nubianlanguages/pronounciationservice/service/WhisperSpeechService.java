package com.nubianlanguages.pronounciationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class WhisperSpeechService {

    @Value("${whisper.cli.path}")
    private String whisperCliPath;

    @Value("${whisper.model.path}")
    private String whisperModelPath;

    @Value("${whisper.language:sw}")
    private String whisperLanguage;

    public String transcribe(File wavFile) throws Exception {
        if (wavFile == null || !wavFile.exists()) {
            throw new IllegalArgumentException("WAV file does not exist");
        }
        System.out.println("Transcribing file: " + wavFile.getAbsolutePath());
//System.out.println("calling transcribe from whisper speech "+ wavFile.getAbsolutePath());
        List<String> command = new ArrayList<>();
        command.add(whisperCliPath);
        command.add("-m");
        command.add(whisperModelPath);
        command.add("-f");
        command.add(wavFile.getAbsolutePath());
        command.add("-l");
        command.add(whisperLanguage);
        command.add("-nt"); // no timestamps, easier to parse

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        String output;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            output = sb.toString();
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("whisper-cli failed. Exit code: " + exitCode + "\nOutput:\n" + output);
        }
       // System.out.println("RAW WHISPER OUTPUT:\n" + output);
        return extractTranscript(output);
    }

    private String extractTranscript(String output) {
        if (output == null || output.isBlank()) {
            return "";
        }

        StringBuilder transcript = new StringBuilder();

        for (String line : output.split("\\R")) {
            String trimmed = line.trim();

            if (trimmed.isBlank()) continue;

            // Skip startup/log lines
            if (trimmed.startsWith("whisper_")
                    || trimmed.startsWith("main:")
                    || trimmed.startsWith("system_info:")
                    || trimmed.startsWith("ggml_")
                    || trimmed.startsWith("load_")
                    || trimmed.startsWith("encode_")
                    || trimmed.startsWith("decode_")) {
                continue;
            }

            // Remove timestamp prefix if present: [00:00:00.000 --> 00:00:02.000]
            trimmed = trimmed.replaceFirst("^\\[[^\\]]+\\]\\s*", "");

            if (!trimmed.isBlank()) {
                transcript.append(trimmed).append(" ");
            }
        }

        return transcript.toString().trim();
    }
}