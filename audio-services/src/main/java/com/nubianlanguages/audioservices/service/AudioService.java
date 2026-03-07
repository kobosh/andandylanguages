package com.nubianlanguages.audioservices.service;

import com.nubianlanguages.audioservices.dto.UploadResponse;
import io.minio.*;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class AudioService {

    private static final Logger logger = LoggerFactory.getLogger(AudioService.class); //

    private final MinioClient minioClient;

    @Value("${minio.bucket.word}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    public AudioService(MinioClient minioClient)
    {   logger.info("audio servic construct");
        this.minioClient = minioClient;
    }

    @PostConstruct
    public void createBucketIfNotExists() {
      logger.info("AudioService: checking MinIO bucket");

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
               logger.info("Bucket created: {}" , bucketName);
            } else {
                logger.info("Bucket exists: {}" ,bucketName);
            }

        } catch (Exception e) {
            // 🚨 DO NOT FAIL STARTUP
            logger.info(
                    "MinIO not ready at startup. Will retry on first request. Reason:{} "
                            , e.getMessage()
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
