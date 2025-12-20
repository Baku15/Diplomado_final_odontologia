package com.app_odontologia.diplomado_final.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface MinioService {

    String uploadFile(String bucket, String prefix, MultipartFile file) throws Exception;

    InputStream getFile(String bucket, String objectKey) throws Exception;

    /**
     * Put an object from an InputStream (used by service implementations).
     */
    void putObject(InputStream inputStream, String bucket, String objectKey, String contentType, long size) throws Exception;

    /**
     * Generate presigned GET URL for object (ttl seconds).
     */
    String generatePresignedGet(String bucket, String objectKey, int ttlSeconds) throws Exception;

    /**
     * Generate presigned PUT URL for object (ttl seconds).
     */
    String generatePresignedPut(String bucket, String objectKey, int ttlSeconds) throws Exception;

    /**
     * Delete object.
     */
    void deleteObject(String bucket, String objectKey) throws Exception;
}
