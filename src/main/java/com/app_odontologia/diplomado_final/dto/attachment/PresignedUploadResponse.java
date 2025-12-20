// src/main/java/com/app_odontologia/diplomado_final/dto/attachment/PresignedUploadResponse.java
package com.app_odontologia.diplomado_final.dto.attachment;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresignedUploadResponse {
    private String uploadUrl;
    private String storageKey;
    private int expiresIn; // seconds
}
