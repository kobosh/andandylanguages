package com.nubianlanguages.audioservices.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class RecordingRequest {
    private String authorName;
    private String word;
    private String meaning;
    private String sentence;
    private String sentenceMeaning;
    private MultipartFile file;
}
