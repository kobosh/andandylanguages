package com.nubianlanguages.audioservices.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
@Getter
@Setter
@Data
public class RecordingRequest {
    private String authorName;
    private String word;
    private String meaning;
    private String sentence;
    private String sentenceMeaning;
    private MultipartFile file;
    private  String dialect;
}
