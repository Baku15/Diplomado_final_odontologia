
package com.app_odontologia.diplomado_final.dto;// src/main/java/com/app_odontologia/diplomado_final/dto/patient/CheckDuplicateResponse.java

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta simple:
 *  exists: true/false
 *  field: "document" | "email" | "phone" | null
 *  value: valor detectado (ej. "CI:1234567" o "juan@x.com" o "+59176543210")
 *  message: texto legible
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckDuplicateResponse {
    private boolean exists;
    private String field;
    private String value;
    private String message;
}
