// src/main/java/com/app_odontologia/diplomado_final/dto/attachment/AttachmentLinkRequest.java
package com.app_odontologia.diplomado_final.dto.attachment;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentLinkRequest {
    private String storageKey;       // required (la misma que la presigned response)
    private String filename;
    private String contentType;
    private Long sizeBytes;
    private Long clinicalRecordId;   // opcional
    private String toothReference;   // opcional
    private String notes;            // opcional
    private String type;             // opcional, ej. RADIOGRAPH, PHOTO
}
