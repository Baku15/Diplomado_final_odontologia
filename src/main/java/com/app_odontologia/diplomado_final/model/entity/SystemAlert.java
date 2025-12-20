package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "system_alerts",
        indexes = {
                @Index(name = "idx_alert_clinic", columnList = "clinic_id"),
                @Index(name = "idx_alert_resolved", columnList = "resolved"),
                @Index(name = "idx_alert_type", columnList = "type")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemAlert {

    public enum AlertType {
        CONSULTATION,
        APPOINTMENT,
        ODONTOGRAM,
        PATIENT,
        SYSTEM
    }

    public enum AlertSeverity {
        INFO,
        WARNING,
        CRITICAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== Contexto =====

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Column(name = "consultation_id")
    private Long consultationId;

    @Column(name = "appointment_id")
    private Long appointmentId;

    // ===== Tipo =====

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private AlertSeverity severity;

    // ===== Contenido =====

    @Column(name = "message", nullable = false, columnDefinition = "text")
    private String message;

    // ===== Estado =====

    @Column(name = "resolved", nullable = false)
    private Boolean resolved = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
