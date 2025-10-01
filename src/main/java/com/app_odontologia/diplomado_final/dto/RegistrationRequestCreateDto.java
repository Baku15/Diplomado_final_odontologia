package com.app_odontologia.diplomado_final.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegistrationRequestCreateDto {
    @NotBlank
    private String nombre;
    @NotBlank private String apellido;
    @Email
    @NotBlank private String email;
    private String ocupacion;
    private String zona;
    private String direccion;
    // otros...
}
