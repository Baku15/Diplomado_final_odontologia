package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Odontograma (DentalChart) asociado a un paciente (y opcionalmente a una ClinicalRecord).
 * Diseñado para versionado simple y para poder tener varios odontogramas históricos por paciente.
 */
@Entity
@Table(name = "dental_charts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DentalChart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación con Clinic (redundante para consultas por clínica)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dental_chart_clinic"))
    private Clinic clinic;

    // Paciente
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_dental_chart_patient"))
    private Patient patient;

    // Opcional: referencia a ClinicalRecord (si lo vamos a asociar)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinical_record_id",
            foreignKey = @ForeignKey(name = "fk_dental_chart_clinical_record"))
    private ClinicalRecord clinicalRecord;

    // Versión simple para poder seguir evolución
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private ChartStatus status = ChartStatus.ACTIVE;

    // Relación con dientes
    @OneToMany(mappedBy = "chart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Tooth> teeth = new ArrayList<>();

    // Procedimientos/notes globales asociados al chart
    @OneToMany(mappedBy = "chart", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DentalProcedure> procedures = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // 🔥 NUEVO: archivado real (NO sesión)
    @Column(name = "archived_at")
    private Instant archivedAt;

    public enum ChartStatus {
        ACTIVE,
        CLOSED
    }
}
