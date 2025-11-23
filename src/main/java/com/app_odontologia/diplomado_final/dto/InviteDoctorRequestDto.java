package com.app_odontologia.diplomado_final.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteDoctorRequestDto {

    @NotBlank
    private String fullName;   // Nombre y apellido del doctor invitado

    @NotBlank
    @Email
    private String email;      // Correo al que enviaremos la invitación

    private String phone;      // Opcional

    private String specialty;  // Opcional, por si ya la sabes

    private String notes;      // Comentarios internos del admin (opcional)
}
