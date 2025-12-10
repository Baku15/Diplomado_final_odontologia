// src/main/java/com/app_odontologia/diplomado_final/service/MinioService.java
package com.app_odontologia.diplomado_final.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * Interfaz para operaciones básicas con MinIO.
 */
public interface MinioService {

    String uploadFile(String bucket, String prefix, MultipartFile file) throws Exception;

    InputStream getFile(String bucket, String objectKey) throws Exception;

}
