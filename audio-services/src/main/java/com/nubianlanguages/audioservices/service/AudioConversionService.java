package com.nubianlanguages.audioservices.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class AudioConversionService {

    public File multipartToTempFile(MultipartFile file, String suffix) throws IOException {
        File temp = File.createTempFile("audio-", suffix);
        file.transferTo(temp);
        return temp;
    }

    public File convertWebmToWav(File inputWebm) throws IOException, InterruptedException {
        File outputWav = File.createTempFile("audio-converted-", ".wav");

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", inputWebm.getAbsolutePath(),
                "-ar", "16000",
                "-ac", "1",
                "-c:a", "pcm_s16le",
                outputWav.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();
        int exit = process.waitFor();

        if (exit != 0) {
            throw new RuntimeException("FFmpeg conversion failed");
        }

        return outputWav;
    }
}