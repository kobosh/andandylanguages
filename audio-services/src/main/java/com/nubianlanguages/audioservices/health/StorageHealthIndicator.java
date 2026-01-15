package com.nubianlanguages.audioservices.health;


import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class StorageHealthIndicator implements HealthIndicator {

    private final MinioClient minio;
    private final String bucket;

    public StorageHealthIndicator(MinioClient minio,
                                  @Value("${minio.bucket}") String bucket) {
        this.minio = minio;
        this.bucket = bucket;
    }

    @Override
    public Health health() {
        try {
            boolean exists = minio.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );

            if (!exists) {
                return Health.down()
                        .withDetail("bucket", bucket)
                        .withDetail("error", "Bucket does not exist")
                        .build();
            }

            return Health.up()
                    .withDetail("bucket", bucket)
                    .withDetail("provider", "minio")
                    .build();

        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}

