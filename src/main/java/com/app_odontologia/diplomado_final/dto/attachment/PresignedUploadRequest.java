// src/main/java/com/app_odontologia/diplomado_final/dto/attachment/PresignedUploadRequest.java
package com.app_odontologia.diplomado_final.dto.attachment;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresignedUploadRequest {
    private String filename;
    private String contentType;
    private Long clinicalRecordId;   // opcional: se puede asociar al clinical record
    private String toothReference;   // opcional: "tooth-18" u "18"
}
