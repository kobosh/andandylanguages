package com.nubianlanguages.audioservices.controller;


import com.nubianlanguages.audioservices.dto.PracticeItemResponse;
import com.nubianlanguages.audioservices.service.RecordingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/practice-items")
public class PracticeItemController {

   /* private final RecordingService recordingService;

    public PracticeItemController(RecordingService recordingService) {
        this.recordingService = recordingService;
    }

    @GetMapping
    public ResponseEntity<List<PracticeItemSummaryResponse>> getPublishedItems() {
        return ResponseEntity.ok(recordingService.getPublishedItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PracticeItemResponse> getPublishedItem(@PathVariable Long id) {
        return ResponseEntity.ok(recordingService.getPublishedItem(id));
    }*/
}