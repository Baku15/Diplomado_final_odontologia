// src/main/java/com/app_odontologia/diplomado_final/mapper/AttachmentMapper.java
package com.app_odontologia.diplomado_final.mapper;

import com.app_odontologia.diplomado_final.dto.attachment.AttachmentDto;
import com.app_odontologia.diplomado_final.model.entity.Attachment;

public class AttachmentMapper {

    private AttachmentMapper() {}

    public static AttachmentDto toDto(Attachment a) {
        if (a == null) return null;
        return AttachmentDto.builder()
                .id(a.getId())
                .filename(a.getFilename())
                .storageKey(a.getStorageKey())
                .contentType(a.getContentType())
                .sizeBytes(a.getSizeBytes())
                .type(a.getType())
                .notes(a.getNotes())
                .toothReference(a.getToothReference())
                .uploaderId(a.getUploaderId())
                .clinicalRecordId(a.getClinicalRecordId())
                .patientId(a.getPatientId())
                .clinicId(a.getClinicId())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
