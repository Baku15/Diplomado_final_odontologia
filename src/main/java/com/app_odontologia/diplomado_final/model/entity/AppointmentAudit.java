package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "appointment_audits",
        indexes = {
                @Index(name = "idx_audit_appointment", columnList = "appointment_id"),
                @Index(name = "idx_audit_patient", columnList = "patient_id"),
                @Index(name = "idx_audit_event", columnList = "event_type")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentAudit {

    public enum EventType {
        CONFIRMED,
        CANCELLED_EARLY,
        CANCELLED_LATE,
        NO_SHOW,
        COMPLETED,
        SPECIAL_CASE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private EventType eventType;

    @Column(name = "note", length = 255)
    private String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
