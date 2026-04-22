package com.nubianlanguages.pronounciationservice.service;




import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class FfmpegAudioConversionService {

    @Value("${ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    public File convertToWav(File inputFile) throws Exception {
        File outputFile = File.createTempFile("converted-", ".wav");

        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-y");
        command.add("-i");
        command.add(inputFile.getAbsolutePath());
        command.add("-vn");
        command.add("-ac");
        command.add("1");
        command.add("-ar");
        command.add("16000");
        command.add("-acodec");
        command.add("pcm_s16le");
        command.add("-f");
        command.add("wav");
        command.add(outputFile.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append(System.lineSeparator());
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg conversion failed:\n" + output);
        }

        return outputFile;
    }
}