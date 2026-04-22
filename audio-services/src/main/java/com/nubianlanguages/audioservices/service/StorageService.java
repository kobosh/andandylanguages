package com.nubianlanguages.audioservices.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {
    String putWord(Long userId, MultipartFile file, String objectKey);
    String putSentence(Long userId, MultipartFile file, String objectKey);

    InputStream getWord(String objectKey);
    InputStream getSentence(String objectKey);

    void deleteWord(String objectKey);
    void deleteSentence(String objectKey);

    void healthCheck();
}
