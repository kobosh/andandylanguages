package com.nubianlanguages.audioservices.service;

import com.nubianlanguages.audioservices.entity.Recording;
import com.nubianlanguages.audioservices.repository.RecordingRepository;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

//import  com.nubianlanguages.audioservices.service
@Service
public class MinioStorageService implements StorageService {

    private final MinioClient minio;
    private final String wordBucket;
    private final String sentenceBucket;
    private final RecordingRepository recordingRepository;

    public MinioStorageService(
            MinioClient minio,
            @Value("${minio.bucket.word}") String wordBucket,
            @Value("${minio.bucket.sentence}") String sentenceBucket,
            RecordingRepository recordingRepository
    ) {
        this.minio = minio;
        this.wordBucket = wordBucket;
        this.sentenceBucket = sentenceBucket;
        this.recordingRepository = recordingRepository;
    }

    // ---------------------------
    // Bucket init
    // ---------------------------
    @PostConstruct
    public void ensureBucketsExist() {
        ensureBucketExists(wordBucket);
        ensureBucketExists(sentenceBucket);
    }

    private void ensureBucketExists(String bucket) {
        try {
            boolean exists = minio.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );
            if (!exists) {
                minio.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                System.out.println("Created bucket: " + bucket);
            }
        } catch (Exception e) {
            System.err.println("MinIO bucket check failed for " + bucket + ": " + e.getMessage());
        }
    }

    // ---------------------------
    // Presigned URLs
    // ---------------------------

    public String getWordUrl(Long recordingId) throws Exception {
        Recording rec = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new RuntimeException("Recording not found"));

        String wordKey = rec.getWordObjectKey();
        if (wordKey == null || wordKey.isBlank()) {
            throw new RuntimeException("Word object key is null");
        }

        return presignedGet(wordBucket, wordKey);
    }

    public String getSentenceUrl(Long recordingId) throws Exception {
        Recording rec = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new RuntimeException("Recording not found"));

        String sentenceKey = rec.getSentenceObjectKey();
        if (sentenceKey == null || sentenceKey.isBlank()) {
            throw new RuntimeException("Sentence object key is null");
        }

        return presignedGet(sentenceBucket, sentenceKey);
    }
    public String getUrl(Long recordingId, String type) throws Exception {
        if ("SENTENCE".equalsIgnoreCase(type)) {
            return getSentenceUrl(recordingId);
        }
        // default WORD
        return getWordUrl(recordingId);
    }
    public String presignedGet(String bucket, String objectKey) throws Exception {
        return minio.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(objectKey)
                        .expiry(10 * 60)
                        .build()
        );
    }

    // ---------------------------
    // Upload helpers (recommended)
    // ---------------------------

    public String uploadToWordBucket(MultipartFile file) {
        return uploadToBucket(wordBucket, file);
    }

    public String uploadToSentenceBucket(MultipartFile file) {
        return uploadToBucket(sentenceBucket, file);
    }

    private String uploadToBucket(String bucket, MultipartFile file) {
        try {
            String original = file.getOriginalFilename() != null ? file.getOriginalFilename() : "audio.webm";
            String objectKey = UUID.randomUUID() + "_" + original;

            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                            .build()
            );

            return objectKey;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO (bucket=" + bucket + ")", e);
        }
    }

    // ---------------------------
    // StorageService interface methods
    // ---------------------------
    // NOTE: These methods need a bucket. To keep your interface unchanged,
    // we’ll default them to wordBucket. Prefer using uploadToWordBucket/uploadToSentenceBucket above.

    @Override
    public String put(Long userId, MultipartFile file, String objectKey) {
        return putToBucket(wordBucket, file, objectKey);
    }

    public String putSentence(Long userId, MultipartFile file, String objectKey) {
        return putToBucket(sentenceBucket, file, objectKey);
    }

    private String putToBucket(String bucket, MultipartFile file, String objectKey) {
        try {
            byte[] bytes = file.getBytes();

            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                            .build()
            );

            return objectKey;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload to MinIO (bucket=" + bucket + ")", e);
        }
    }

    @Override
    public InputStream get(String objectKey) {
        // default word bucket (keep interface)
        return getFromBucket(wordBucket, objectKey);
    }

    public InputStream getSentence(String objectKey) {
        return getFromBucket(sentenceBucket, objectKey);
    }

    private InputStream getFromBucket(String bucket, String objectKey) {
        try {
            return minio.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to read from MinIO (bucket=" + bucket + ")", e);
        }
    }

    @Override
    public void delete(String objectKey) {
        deleteFromBucket(wordBucket, objectKey);
    }

    public void deleteSentence(String objectKey) {
        deleteFromBucket(sentenceBucket, objectKey);
    }

    private void deleteFromBucket(String bucket, String objectKey) {
        try {
            minio.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete from MinIO (bucket=" + bucket + ")", e);
        }
    }

    @Override
    public void healthCheck() {
        healthCheckBucket(wordBucket);
        healthCheckBucket(sentenceBucket);
    }

    private void healthCheckBucket(String bucket) {
        try {
            String key = "healthcheck/" + System.currentTimeMillis() + ".txt";
            byte[] data = "ok".getBytes();

            minio.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(key)
                            .stream(new ByteArrayInputStream(data), data.length, -1)
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
            throw new RuntimeException("MinIO health check failed (bucket=" + bucket + ")", e);
        }
    }

    @Override
    public String getBucketName() {
        // interface method can’t return 2 buckets; return word as default
        return wordBucket;
    }

    // Optional getters if you need them elsewhere
    public String getWordBucketName() { return wordBucket; }
    public String getSentenceBucketName() { return sentenceBucket; }



}