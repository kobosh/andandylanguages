package com.nubianlanguages.audioservices.controller;

import com.nubianlanguages.audioservices.dto.PublicWordResponseXX;
import com.nubianlanguages.audioservices.service.PublicCatalogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicCatalogControllerXX {

    private final PublicCatalogService publicCatalogService;

    public PublicCatalogControllerXX(PublicCatalogService publicCatalogService) {
        this.publicCatalogService = publicCatalogService;
    }

    @GetMapping("/words")
    public List<PublicWordResponseXX> getWords() {
        return publicCatalogService.getPublicWords();
    }
}