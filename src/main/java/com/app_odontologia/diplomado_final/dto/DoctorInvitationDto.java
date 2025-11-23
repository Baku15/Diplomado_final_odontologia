package com.app_odontologia.diplomado_final.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class DoctorInvitationDto {

    private Long id;
    private Long clinicId;

    private String fullName;
    private String email;
    private String phone;
    private String specialty;
    private String notes;

    private String token;
    private String status;           // PENDING / ACCEPTED / EXPIRED / CANCELLED

    private String invitedBy;        // quién envió la invitación
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
}
