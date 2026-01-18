package com.nubianlanguages.audioservices.controller;

import com.nubianlanguages.audioservices.dto.UploadResponse;
import com.nubianlanguages.audioservices.service.AudioService;
import com.nubianlanguages.audioservices.service.AudioUploadService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
@CrossOrigin(origins = "http://localhost:4200")
public class AudioController {

    public AudioController(AudioService audioService, AudioUploadService audioUploadService) {
    }

    // Correct URL:  POST /api/audio/upload
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String,String>> upload(@RequestParam("file") MultipartFile file) {

        System.out.println("Received file: " + file.getOriginalFilename());
        try {
            String folder = "C:/language-ui/data/";
            String filename = file.getOriginalFilename();

            File dest = new File(folder + filename);

            // Create directory if not exists
            dest.getParentFile().mkdirs();

            // Save file to disk
            file.transferTo(dest);

            System.out.println("Saved file to: " + dest.getAbsolutePath());
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Saved");
            resp.put("filename", filename);

            return ResponseEntity.ok(resp);
           // return ResponseEntity.ok("Saved: " + filename);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of( "Failed to save file: " ,e.getMessage()));
        }

        // save to MinIO or disk here
        //audioUploadService.upload(file);

       // return ResponseEntity.ok("Uploaded: " + file.getOriginalFilename());
    }
}
