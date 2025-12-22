package com.app_odontologia.diplomado_final.model.entity;

import com.app_odontologia.diplomado_final.model.enums.AppointmentOrigin;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "appointments",
        indexes = {
                @Index(name = "idx_appointment_clinic_date", columnList = "clinic_id, date"),
                @Index(name = "idx_appointment_doctor_date", columnList = "doctor_id, date"),
                @Index(name = "idx_appointment_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    public enum AppointmentStatus {
        SCHEDULED,
        COMPLETED,
        CANCELLED,
        NO_SHOW
    }

    public enum CancelledBy {
        PATIENT,
        DOCTOR,
        SYSTEM
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== Relaciones =====

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false)
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "patient_id")
    private Patient patient;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private User doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultation_id")
    private ClinicalConsultation consultation;

    // ===== Fecha / hora =====

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    // ===== Estado =====

    // ===== Confirmación de asistencia =====
    @Column(name = "attendance_confirmed")
    private Boolean attendanceConfirmed = false;

    @Column(name = "attendance_confirmed_at")
    private Instant attendanceConfirmedAt;

    // ===== Cancelación =====
    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancelled_by", length = 20)
    private CancelledBy cancelledBy;

    @Column(name = "late_cancellation")
    private Boolean lateCancellation = false;

    // ===== Caso especial =====
    @Column(name = "special_case")
    private Boolean specialCase = false;

    @Column(name = "special_case_note", length = 255)
    private String specialCaseNote;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    // ===== Información clínica =====

    @Column(name = "reason", length = 255)
    private String reason;

    // ===== Recordatorios =====

    @Column(name = "send_whatsapp")
    private Boolean sendWhatsapp;

    @Column(name = "send_email")
    private Boolean sendEmail;

    @Column(name = "reminder_minutes_before")
    private Integer reminderMinutesBefore;

    // ===== Auditoría =====

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "origin", nullable = false, length = 20)
    private AppointmentOrigin origin = AppointmentOrigin.DIRECT;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "email_reminder_sent_at")
    private Instant emailReminderSentAt;

    // ===== Recordatorios por correo (auditoría) =====

    @Column(name = "email_reminder_failed_at")
    private Instant emailReminderFailedAt;

    @Column(name = "email_reminder_error", length = 255)
    private String emailReminderError;



}
