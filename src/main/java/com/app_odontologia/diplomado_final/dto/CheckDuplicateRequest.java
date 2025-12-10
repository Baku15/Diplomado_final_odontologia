package com.app_odontologia.diplomado_final.dto;// src/main/java/com/app_odontologia/diplomado_final/dto/patient/CheckDuplicateRequest.java

import lombok.Data;

/**
 * Request para verificar duplicados. Se envía uno de:
 *  - documentType + documentNumber
 *  - email
 *  - phoneMobile
 */
@Data
public class CheckDuplicateRequest {
    private String documentType;
    private String documentNumber;
    private String email;
    private String phoneMobile;
}
