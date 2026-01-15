package com.nubianlanguages.audioservices.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

//import  com.nubianlanguages.audioservices.service
@Service
public class MinioStorageService implements StorageService {

    private final MinioClient minio;
    private final String bucket;

    public MinioStorageService(MinioClient minio,
                               @Value("${minio.bucket}") String bucket) {
        this.minio = minio;
        this.bucket = bucket;
    }
    public String upload(MultipartFile file) {
        try {
            String objectKey = UUID.randomUUID() + "_" + file.getOriginalFilename();

            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return objectKey;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }
    @PostConstruct
    public void ensureBucketExists() {
        try {
            boolean exists = minio.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );
            if (!exists) {
                minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            // Don’t kill startup in dev if MinIO is down:
            // You can re-throw in prod if you want strict behavior.
            System.err.println("MinIO bucket check failed: " + e.getMessage());
        }
    }
    @Override
    public String put(Long userId, MultipartFile file, String objectKey) {
        try {
            byte[] bytes = file.getBytes(); // 👈 read ONCE

            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(
                                    new ByteArrayInputStream(bytes),
                                    bytes.length,
                                    -1
                            )
                            .contentType(file.getContentType())
                            .build()
            );

            return objectKey;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to MinIO", e);
        }
    }


   /* @Override
    public String put(Long userId, MultipartFile file) {
        String original = (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank())
                ? "recording.webm"
                : file.getOriginalFilename();

        String objectKey = userId + "/" + System.currentTimeMillis() + "-" + original;

        try {
            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to MinIO", e);
        }
    }*/

    @Override
    public InputStream get(String objectKey) {
        try {
            return minio.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to read from MinIO", e);
        }
    }

    @Override
    public void delete(String objectKey) {
        try {
            minio.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete from MinIO", e);
        }
    }

    @Override
    public void healthCheck() {
        try {
            String key = "healthcheck/" + System.currentTimeMillis() + ".txt";
            byte[] data = "ok".getBytes();
            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(
                                    new ByteArrayInputStream(data),
                                    data.length,
                                    -1
                            )
                            .contentType("text/plain")
                            .build()
            );

            minio.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .build()
            );

        } catch (Exception e) {
            throw new RuntimeException("MinIO health check failed", e);
        }
    }
    @Override
    public String getBucketName() {
        return bucket;
    }

}
