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
    private final String bucket;
    private final RecordingRepository recordingRepository;
    public MinioStorageService(MinioClient minio,
                               @Value("${minio.bucket}") String bucket, RecordingRepository recordingRepository) {
        this.minio = minio;
        this.bucket = bucket;
        this.recordingRepository = recordingRepository;
    }
    public String getminiourl(Long id) throws ServerException,
            InsufficientDataException, ErrorResponseException, IOException,
            NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
       Recording rec = recordingRepository.findById(id)
              .orElseThrow(() -> new RuntimeException("Recording not found"));
        String key = rec.getObjectKey();

        String url = minio.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket("language-sound")
                        .object(key)
                        .expiry(10 * 60) // 10 minutes
                        .build()
        );

        return url;
    }
    public String getMinioUrlByObjectKey(String objectKey) throws Exception {

        return minio.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket("language-sound")
                        .object(objectKey)
                        .expiry(10 * 60)
                        .build()
        );
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
