package com.app_odontologia.diplomado_final.dto;

import lombok.Data;

@Data
public class ApproveRegistrationDto {
    private String username;         // opcional; si no, se genera
    private String roleName;         // p.ej. "ROLE_PATIENT"
    private Boolean sendTempPassword = true;
}
