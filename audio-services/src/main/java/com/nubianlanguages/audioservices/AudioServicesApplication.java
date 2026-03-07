package com.nubianlanguages.audioservices;

import com.nubianlanguages.audioservices.service.MinioStorageService;
import com.nubianlanguages.audioservices.service.RecordingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AudioServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(AudioServicesApplication.class, args);
    }



}