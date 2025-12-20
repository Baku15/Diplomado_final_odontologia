package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Consulta Clínica Odontológica.
 *
 * Representa una sesión real de atención en una fecha específica.
 * NO reemplaza odontograma ni historia clínica.
 */
@Entity
@Table(
        name = "clinical_consultations",
        indexes = {
                @Index(name = "idx_consultation_clinic_patient", columnList = "clinic_id, patient_id"),
                @Index(name = "idx_consultation_status", columnList = "status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalConsultation {

    public enum ConsultationStatus {
        ACTIVE,        // sesión en curso
        COMPLETED,     // sesión finalizada hoy
        IN_PROGRESS,   // tratamiento continúa
        CLOSED         // tratamiento finalizado
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== Relaciones base =====

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_consultation_clinic"))
    private Clinic clinic;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_consultation_patient"))
    private Patient patient;

    // Odontólogo responsable
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dentist_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_consultation_dentist"))
    private User dentist;

    // Odontograma activo en esta consulta
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chart_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_consultation_chart"))
    private DentalChart dentalChart;

    // ===== Estado =====

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private ConsultationStatus status = ConsultationStatus.ACTIVE;

    // ===== Tiempos =====

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    // ===== Notas clínicas del día =====

    @Column(name = "clinical_notes", columnDefinition = "text")
    private String clinicalNotes;

    @Column(name = "summary", columnDefinition = "text")
    private String summary;



    // ===== Auditoría =====

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
