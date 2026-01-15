package com.nubianlanguages.audioservices.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Slf4j
@Service
public class FfmpegAudioService {



    @PostConstruct
    public void init() {
        log.info("AudioService initialized");
    }

    public void uploadOriginal(MultipartFile file, String word, String meaning) {
        log.info("uploadOriginal called");

        // For now: just store as-is
        // Later: call trimWithFfmpeg(...)
    }

    public File trimWithFfmpeg(
            File input,
            double start,
            double end,
            String word,
            String meaning
    ) throws Exception {

        log.info("Trimming audio with ffmpeg: start={}, end={}", start, end);

        File output = File.createTempFile("trimmed-", ".wav");

        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", input.getAbsolutePath(),
                "-ss", String.valueOf(start),
                "-to", String.valueOf(end),
                output.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();
        process.waitFor();

        log.info("FFmpeg finished, output={}", output.getAbsolutePath());
        return output;
    }
}

