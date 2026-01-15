package com.nubianlanguages.audioservices.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "services")
@Getter
@Setter
public class ServiceUrls {

    private Service auth;
    private Service audio;
    private Service recording;

    @Getter
    @Setter
    public static class Service {
        private String url;
    }
}
