package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "clinicas")
@Data
public class Clinic {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Opcional: puedes dejar null por ahora y completarlo en el wizard
    @Column(name = "nombre_comercial")
    private String nombreComercial;

    // Odontólogo administrador local (ROLE_ADMIN_CLINICA)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", nullable = false, unique = true)
    private User admin;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
