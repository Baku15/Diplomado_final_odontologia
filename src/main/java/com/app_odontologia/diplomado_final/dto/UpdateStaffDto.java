package com.app_odontologia.diplomado_final.dto;

import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@Data
public class UpdateStaffDto {
    @NotBlank
    private String nombre;
    @NotBlank
    private String apellido;

    @Email
    @NotBlank
    private String email;

    /**
     * Optional username (may change)
     */
    private String username;

    private String phone;

    /**
     * Nuevos roles (por ejemplo ["ROLE_DENTIST","ROLE_CLINIC_ADMIN"]) — si null, no cambiar roles.
     */
    private List<String> roleNames;

    /**
     * Estado deseado: "ACTIVE", "PENDING_ACTIVATION", "BLOCKED" — opcional
     */
    private String status;
}
