package com.nubianlanguages.pronounciationservice.controller;

import com.nubianlanguages.pronounciationservice.dto.ErrorResponse;
import com.nubianlanguages.pronounciationservice.dto.PronunciationResponse;
import com.nubianlanguages.pronounciationservice.service.PronunciationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/pronunciation")
@Validated
public class PronunciationController {

    private final PronunciationService pronunciationService;

    public PronunciationController(PronunciationService pronunciationService) {
        this.pronunciationService = pronunciationService;
    }

    @PostMapping(value = "/assess", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PronunciationResponse> assess(
            @RequestPart("audio") MultipartFile audio,
            @RequestParam("expectedText") @NotBlank String expectedText,
            @RequestParam(value = "languageCode", required = false) String languageCode,
            @RequestParam(value = "recordingId", required = false) Long recordingId
    ) {
        PronunciationResponse response = pronunciationService.assess(
                audio, expectedText, languageCode, recordingId
        );
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex,
                                                          HttpServletRequest request) {
        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        400,
                        "Bad Request",
                        ex.getMessage(),
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                       HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(
                        500,
                        "Internal Server Error",
                        ex.getMessage(),
                        request.getRequestURI()
                )
        );
    }
}