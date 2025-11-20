package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clinicas")
@Data
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Opcional: puedes dejar null por ahora y completarlo en el wizard
    @Column(name = "nombre_comercial")
    private String nombreComercial;

    // Odontólogo administrador local (ROLE_CLINIC_ADMIN)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", nullable = false, unique = true)
    private User admin;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    /**
     * Consultorios / salas de esta clínica.
     * No pongo cascade = ALL a lo bestia, pero sí ALL+orphanRemoval = true
     * para que si borras un room desde la clínica se sincronice.
     */
    @OneToMany(
            mappedBy = "clinic",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ClinicRoom> rooms = new HashSet<>();
}
