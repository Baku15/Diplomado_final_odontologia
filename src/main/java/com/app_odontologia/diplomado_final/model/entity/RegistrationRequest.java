// src/main/java/com/app_odontologia/diplomado_final/model/entity/RegistrationRequest.java
package com.app_odontologia.diplomado_final.model.entity;

import com.app_odontologia.diplomado_final.model.enums.RegistrationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "registration_requests")
@Data
public class RegistrationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    private String ocupacion;
    private String zona;
    private String direccion;

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.PENDING_REVIEW;

    private Instant createdAt = Instant.now();
    private Instant reviewedAt;
    private String reviewedBy;

    @Column(name = "is_dentist", nullable = false)
    private boolean dentist = false;
}
