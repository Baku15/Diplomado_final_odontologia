package com.app_odontologia.diplomado_final.dto;

import lombok.Data;

import java.util.List;

@Data
public class UserMeDto {

    // ---- LO QUE YA TENÍAS ----
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private Long clinicId;
    private Boolean mustCompleteProfile = false;

    // ---- NUEVOS CAMPOS FHIR/UX-FRIENDLY ----
    /**
     * Nombres del usuario (para mapear fácil a FHIR: HumanName.given)
     * Copia directa de User.nombres
     */
    private String givenName;

    /**
     * Apellidos del usuario (FHIR: HumanName.family)
     * Copia directa de User.apellidos
     */
    private String familyName;

    /**
     * Nombre completo "Nombres Apellidos"
     * Útil para mostrar en navbar, encabezados, etc.
     */
    private String fullName;

    /**
     * Flag derivado de roles: true si tiene ROLE_DENTIST
     */
    private Boolean dentist;

    /**
     * Flag derivado de roles: true si tiene ROLE_CLINIC_ADMIN
     */
    private Boolean clinicAdmin;
}
