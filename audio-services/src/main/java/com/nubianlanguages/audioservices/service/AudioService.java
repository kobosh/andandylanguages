package com.nubianlanguages.audioservices.service;

import com.nubianlanguages.audioservices.dto.UploadResponse;
import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class AudioService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    public AudioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void createBucketIfNotExists() {
        System.out.println("AudioService: checking MinIO bucket");

        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                System.out.println("Bucket created: " + bucketName);
            } else {
                System.out.println("Bucket exists: " + bucketName);
            }

        } catch (Exception e) {
            // 🚨 DO NOT FAIL STARTUP
            System.err.println(
                    "MinIO not ready at startup. Will retry on first request. Reason: "
                            + e.getMessage()
            );
        }
    }

    public UploadResponse uploadAudio(MultipartFile file) throws Exception {

        String objectName =
                "audio/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

        String fileUrl =
                minioUrl + "/" + bucketName + "/" + objectName;

        return new UploadResponse(objectName, fileUrl);
    }
}
