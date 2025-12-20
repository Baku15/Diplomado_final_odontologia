package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Procedimiento clínico asociado a un diente del odontograma.
 */
@Entity
@Table(name = "dental_procedures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DentalProcedure {

    public enum ProcedureStatus {
        OPEN,
        COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chart al que pertenece
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chart_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_procedure_chart"))
    private DentalChart chart;

    @Column(name = "tooth_number")
    private Integer toothNumber;

    @Column(name = "surface", length = 20)
    private String surface;

    // Código estandar / interno del procedimiento (opcional)
    @Column(name = "procedure_code", length = 100)
    private String procedureCode;

    @Column(name = "type", length = 100, nullable = false)
    private String type;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "performed_by", length = 120)
    private String performedBy;

    @Column(name = "performed_at")
    private Instant performedAt;

    // Nuevo estado del procedimiento
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProcedureStatus status = ProcedureStatus.OPEN;

    // Cuándo se completó
    @Column(name = "completed_at")
    private Instant completedAt;

    // Datos estimados (metadatos para planificación)
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    // Costos en centavos (solo referencia; no procesamos pagos aquí)
    @Column(name = "estimated_cost_cents")
    private Long estimatedCostCents;

    // Attachment opcional (imagen/rx relacionado con el procedimiento)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_id", foreignKey = @ForeignKey(name = "fk_procedure_attachment"))
    private Attachment attachment;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    // Consulta en la que se creó el procedimiento
    @Column(name = "created_in_consultation_id")
    private Long createdInConsultationId;

    // Consulta en la que se finalizó (opcional)
    @Column(name = "completed_in_consultation_id")
    private Long completedInConsultationId;
}
