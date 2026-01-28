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


    /*@Bean
    CommandLineRunner runAtStartup(MinioStorageService recordingService) {
        return args -> {
            //String objectKey = "1/1769388186997-koman-1769388186997.webm";

            String url = recordingService.getminiourl(2L);
            System.out.println(url);
        };
    }*/
}