package com.app_odontologia.diplomado_final.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class DoctorInvitationStatusDto {

    /** Nombre de la clínica que invita (Ej. "Clínica Sonrisas") */
    private String clinicName;

    /** Correo del doctor invitado */
    private String doctorEmail;

    /** Nombre completo del doctor invitado (si se capturó) */
    private String doctorFullName;

    /**
     * Estado interno de la invitación, por ejemplo:
     *  - PENDING
     *  - USED
     *  - EXPIRED
     */
    private String status;

    /** Flag calculado: true si ya expiró según la fecha de expiración */
    private boolean expired;

    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
}
