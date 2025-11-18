package com.app_odontologia.diplomado_final.dto;

import com.app_odontologia.diplomado_final.model.enums.RegistrationStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class RegistrationRequestViewDto {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String ocupacion;
    private String zona;
    private String direccion;
    private RegistrationStatus status;
    private Instant createdAt;

    private boolean dentist;

}