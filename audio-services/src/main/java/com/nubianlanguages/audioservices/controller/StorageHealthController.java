package com.nubianlanguages.audioservices.controller;

import com.nubianlanguages.audioservices.service.StorageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class StorageHealthController {

    private final StorageService storageService;

    public StorageHealthController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/health/storage")
    public Map<String, Object> storageHealth() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "storage");
        status.put("timestamp", Instant.now().toString());

        try {
            storageService.healthCheck();

            status.put("status", "UP");
        } catch (Exception e) {
            status.put("status", "DOWN");
            status.put("error", e.getMessage());
        }

        return status;
    }
}
