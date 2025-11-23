package com.app_odontologia.diplomado_final.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorInvitationRegisterRequestDto {

    @NotBlank
    private String firstName;   // nombres

    @NotBlank
    private String lastName;    // apellidos

    private String phone;       // opcional

    /**
     * Puedes decidir si usas username separado del email.
     * Si no quieres username separado, luego simplemente ignoras este campo
     * y usas el email de la invitación como username.
     */
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;
}
