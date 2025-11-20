package com.app_odontologia.diplomado_final.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class CreateStaffDto {
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellido;

    @Email
    @NotBlank
    private String email;

    /**
     * Optional: si quieres forzar un username.
     * Si es null, el back genera uno automáticamente.
     */
    private String username;

    private String phone;

    /**
     * Lista de roles a asignar (por ejemplo ["ROLE_DENTIST"]).
     * Si se proporciona, se utilizará en preferencia. Si es null, el controller
     * asigna el rol por endpoint.
     */
    private List<String> roleNames;
}
