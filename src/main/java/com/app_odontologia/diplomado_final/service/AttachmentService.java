package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.attachment.AttachmentDto;
import com.app_odontologia.diplomado_final.model.enums.AttachmentType;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    // ================= MULTIPART DIRECTO =================
    AttachmentDto uploadAttachmentMultipart(
            Long clinicId,
            Long patientId,
            MultipartFile file,
            Long clinicalRecordId,
            Long procedureId,
            String toothReference,
            AttachmentType type,
            String notes,
            Long uploaderId
    ) throws Exception;

    // ================= PRESIGNED =================
    PresignedUploadInfo generatePresignedUpload(
            Long clinicId,
            Long patientId,
            String filename,
            String contentType,
            Long sizeBytes,
            Long clinicalRecordId,
            Long procedureId,
            String toothReference,
            AttachmentType type,
            String notes,
            Long uploaderId
    ) throws Exception;

    AttachmentDto linkPresignedUpload(
            Long clinicId,
            Long patientId,
            String storageKey,
            String filename,
            String contentType,
            Long sizeBytes,
            Long clinicalRecordId,
            Long procedureId,
            String toothReference,
            AttachmentType type,
            String notes,
            Long uploaderId
    ) throws Exception;

    // ================= QUERY =================
    AttachmentDto getAttachment(Long id, Long clinicId, int urlTtlSeconds) throws Exception;

    Page<AttachmentDto> listAttachments(Long clinicId, Long patientId, int page, int size);

    Page<AttachmentDto> listGallery(Long clinicId, Long patientId, int page, int size);

    void deleteAttachment(Long id, Long clinicId) throws Exception;

    // ================= ⭐ IMÁGENES POR DIENTE (CLAVE) =================
    List<AttachmentDto> listAttachmentsByTooth(
            Long clinicId,
            Long patientId,
            String toothReference,
            int urlTtlSeconds
    );

    // ================= PRESIGN DTO =================
    class PresignedUploadInfo {
        private String uploadUrl;
        private String storageKey;
        private int expiresIn;
        private String storageBucket;

        public String getUploadUrl() { return uploadUrl; }
        public void setUploadUrl(String uploadUrl) { this.uploadUrl = uploadUrl; }

        public String getStorageKey() { return storageKey; }
        public void setStorageKey(String storageKey) { this.storageKey = storageKey; }

        public int getExpiresIn() { return expiresIn; }
        public void setExpiresIn(int expiresIn) { this.expiresIn = expiresIn; }

        public String getStorageBucket() { return storageBucket; }
        public void setStorageBucket(String storageBucket) { this.storageBucket = storageBucket; }
    }
}
