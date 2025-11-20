package com.app_odontologia.diplomado_final.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CompleteProfileDto {
    // campos clínicos mínimos
    @NotBlank
    private String especialidad;

    @NotBlank
    private String matricula;

    // opcional: otros datos clínicos
    private String bio;
    private String telefono;
}
