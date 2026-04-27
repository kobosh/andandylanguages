package com.nubianlanguages.audioservices.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AudioUploadService {

    private final MinioClient minioClient;

    @Value("${minio.bucket.word}")
    private String wordBucket;

    @Value("${minio.bucket.sentence}")
    private String sentenceBucket;

    @PostConstruct
    public void createBucketsIfNotExists() {



        createIfMissing(wordBucket);
        createIfMissing(sentenceBucket);
    }


    private void createIfMissing(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );

            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );

                System.out.println("Created bucket: " + bucket);
            } else {
                System.out.println("Bucket exists: " + bucket);
            }

        } catch (Exception e) {

            // Do NOT crash service (important in Docker)
            System.out.println("MinIO not ready for bucket: " + bucket);
            System.out.println("Reason: " + e.getMessage());
        }
    }






private void ensureBucket(String bucket) throws Exception {
    if (!minioClient.bucketExists(
            BucketExistsArgs.builder().bucket(bucket).build()
    )) {
        minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucket).build()
        );
    }
}



    public String upload(MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "-" + file.getOriginalFilename();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(wordBucket)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            return fileName;

        } catch (Exception e) {
            throw new RuntimeException("MinIO upload failed", e);
        }
    }

}
