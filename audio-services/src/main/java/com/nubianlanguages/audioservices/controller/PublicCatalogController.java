package com.nubianlanguages.audioservices.controller;

import com.nubianlanguages.audioservices.dto.PublicWordResponse;
import com.nubianlanguages.audioservices.service.PublicCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicCatalogController {

    private final PublicCatalogService publicCatalogService;

    public PublicCatalogController(PublicCatalogService publicCatalogService) {
        this.publicCatalogService = publicCatalogService;
    }

    @GetMapping("/words")
    public List<PublicWordResponse> getWords() {
        return publicCatalogService.getPublicWords();
    }
}