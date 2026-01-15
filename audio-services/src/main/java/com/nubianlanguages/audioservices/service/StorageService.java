package com.nubianlanguages.audioservices.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {
    String put(Long userId, MultipartFile file,String
               objectKey);
    InputStream get(String objectKey);
    void delete(String objectKey);
    void healthCheck();

    Object getBucketName();
}
