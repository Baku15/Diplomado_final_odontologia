package com.app_odontologia.diplomado_final.dto.odontogram;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToothAttachmentDto {
    private Long id; // id del ToothAttachment
    private Long attachmentId; // referencia al Attachment real
    private String filename;
    private String storageKey;
    private String contentType;
    private Long sizeBytes;
    private String notes;
    private Instant createdAt;
}
