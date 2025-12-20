package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${app.minio.bucket:patient-profiles}")
    private String defaultBucket;

    @Override
    public String uploadFile(String bucket, String prefix, MultipartFile file) throws Exception {
        String useBucket = (bucket == null || bucket.isBlank()) ? defaultBucket : bucket;

        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(useBucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(useBucket).build());
        }

        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String objectName = (prefix == null ? "" : prefix + "/")
                + java.util.UUID.randomUUID().toString()
                + "_" + System.currentTimeMillis()
                + ext;

        try (InputStream is = file.getInputStream()) {
            PutObjectArgs putArgs = PutObjectArgs.builder()
                    .bucket(useBucket)
                    .object(objectName)
                    .stream(is, file.getSize(), PutObjectArgs.MIN_MULTIPART_SIZE)
                    .contentType(file.getContentType())
                    .build();

            minioClient.putObject(putArgs);
        }

        return objectName;
    }

    @Override
    public InputStream getFile(String bucket, String objectKey) throws Exception {
        String useBucket = (bucket == null || bucket.isBlank()) ? defaultBucket : bucket;
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(useBucket)
                        .object(objectKey)
                        .build()
        );
    }

    @Override
    public void putObject(InputStream inputStream, String bucket, String objectKey, String contentType, long size) throws Exception {
        String useBucket = (bucket == null || bucket.isBlank()) ? defaultBucket : bucket;
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(useBucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(useBucket).build());
        }
        PutObjectArgs put = PutObjectArgs.builder()
                .bucket(useBucket)
                .object(objectKey)
                .stream(inputStream, size, PutObjectArgs.MIN_MULTIPART_SIZE)
                .contentType(contentType)
                .build();
        minioClient.putObject(put);
    }

    @Override
    public String generatePresignedGet(String bucket, String objectKey, int ttlSeconds) throws Exception {
        String useBucket = (bucket == null || bucket.isBlank()) ? defaultBucket : bucket;
        if (ttlSeconds <= 0) ttlSeconds = 300;
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .method(Method.GET)
                .bucket(useBucket)
                .object(objectKey)
                .expiry(ttlSeconds)
                .build();
        return minioClient.getPresignedObjectUrl(args);
    }

    @Override
    public String generatePresignedPut(String bucket, String objectKey, int ttlSeconds) throws Exception {
        String useBucket = (bucket == null || bucket.isBlank()) ? defaultBucket : bucket;
        if (ttlSeconds <= 0) ttlSeconds = 900;
        GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                .method(Method.PUT)
                .bucket(useBucket)
                .object(objectKey)
                .expiry(ttlSeconds)
                .build();
        return minioClient.getPresignedObjectUrl(args);
    }

    @Override
    public void deleteObject(String bucket, String objectKey) throws Exception {
        String useBucket = (bucket == null || bucket.isBlank()) ? defaultBucket : bucket;
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(useBucket)
                .object(objectKey)
                .build());
    }
}
