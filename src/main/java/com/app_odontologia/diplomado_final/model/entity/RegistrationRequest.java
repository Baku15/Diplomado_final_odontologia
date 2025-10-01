package com.app_odontologia.diplomado_final.model.entity;

import com.app_odontologia.diplomado_final.model.enums.RegistrationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

// RegistrationRequest.java
@Entity
@Table(name = "registration_requests")
@Data
public class RegistrationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nombre;
    @NotBlank private String apellido;
    @Email
    @NotBlank @Column(unique = true) private String email;
    private String ocupacion;
    private String zona;
    private String direccion;
    // otros campos...

    @Enumerated(EnumType.STRING)
    private RegistrationStatus status = RegistrationStatus.PENDIENTE;

    private Instant createdAt = Instant.now();
    private Instant reviewedAt;
    private String reviewedBy; // username del superusuario
}
