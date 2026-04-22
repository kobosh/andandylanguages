package com.nubianlanguages.audioservices.service;

import com.nubianlanguages.audioservices.service.StorageService;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class MinioStorageService implements StorageService {

    private final MinioClient minio;

    @Value("${minio.bucket.word}")
    private String wordbucket;

    @Value("${minio.bucket.sentence}")
    private String sentencebucket;

    public MinioStorageService(MinioClient minio) {
        this.minio = minio;
    }
    public String getUrl(String objectKey, String type) {
        try {
            String bucket = "SENTENCE".equalsIgnoreCase(type) ? sentencebucket : wordbucket;

            return minio.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(60 * 60) // 1 hour
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to build presigned URL", e);
        }
    }
    @Override
    public String putWord(Long userId, MultipartFile file, String objectKey) {
        try {

            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(wordbucket)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload word audio", e);
        }
    }

    @Override
    public String putSentence(Long userId, MultipartFile file, String objectKey) {
        try {
            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(sentencebucket)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            return objectKey;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload sentence audio", e);
        }
    }

    @Override
    public InputStream getWord(String objectKey) {
        try {
            return minio.getObject(
                    GetObjectArgs.builder()
                            .bucket(wordbucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch word audio", e);
        }
    }

    @Override
    public InputStream getSentence(String objectKey) {
        try {
            return minio.getObject(
                    GetObjectArgs.builder()
                            .bucket(sentencebucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch sentence audio", e);
        }
    }

    @Override
    public void deleteWord(String objectKey) {
        try {
            minio.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(wordbucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete word audio", e);
        }
    }

    @Override
    public void deleteSentence(String objectKey) {
        try {
            minio.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(sentencebucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete sentence audio", e);
        }
    }

    @Override
    public void healthCheck() {
        try {
            minio.listBuckets();
        } catch (Exception e) {
            throw new RuntimeException("MinIO health check failed", e);
        }
    }
}