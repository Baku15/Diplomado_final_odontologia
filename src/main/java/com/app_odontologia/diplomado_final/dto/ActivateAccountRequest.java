package com.app_odontologia.diplomado_final.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivateAccountRequest {
    @NotBlank private String token;
    @NotBlank private String newPassword;
}