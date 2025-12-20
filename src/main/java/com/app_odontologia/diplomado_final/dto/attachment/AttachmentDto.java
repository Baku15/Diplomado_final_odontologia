package com.app_odontologia.diplomado_final.dto.attachment;

import com.app_odontologia.diplomado_final.model.enums.AttachmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {
    private Long id;
    private Long clinicId;
    private Long patientId;
    private Long clinicalRecordId;
    private Long procedureId;
    private String toothReference;
    private Long uploaderId;
    private String filename;
    private String contentType;
    private Long sizeBytes;
    private String storageKey;
    private String thumbnailKey;
    private AttachmentType type;
    private String notes;
    private Instant createdAt;

    // convenience field: signed URLs (filled by service when requested)
    private String downloadUrl;
    private String thumbnailUrl;

    private Long consultationId;


}
