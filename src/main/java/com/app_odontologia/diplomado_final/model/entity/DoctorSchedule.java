package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.Instant;

@Entity
@Table(name = "doctor_schedules")
@Data
public class DoctorSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Perfil del doctor al que pertenece este bloque horario.
     * (Practitioner en términos FHIR)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_profile_id", nullable = false)
    private DoctorProfile doctor;

    /**
     * Consultorio donde atiende en este horario.
     * (Location en términos FHIR)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private ClinicRoom room;

    /**
     * Día de la semana (LUNES, MARTES, etc).
     * Usamos java.time.DayOfWeek para que sea estándar y legible.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;

    /**
     * Hora de inicio de atención (ej. 09:00).
     */
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    /**
     * Hora fin de atención (ej. 13:00).
     */
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    /**
     * Flag activo/inactivo por si necesitas desactivar un horario
     * sin borrarlo permanentemente.
     */
    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
