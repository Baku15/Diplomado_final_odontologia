package com.app_odontologia.diplomado_final.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Historia Clínica Odontológica (base) de un paciente.
 *
 * Pensado para mapear a FHIR:
 *  - Antecedentes médicos / odontológicos -> Condition[]
 *  - Signos vitales / examen clínico      -> Observation[]
 *  - Diagnóstico inicial                  -> Condition
 *  - Plan de tratamiento inicial          -> Procedure / CarePlan
 */
@Entity
@Table(
        name = "clinical_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_clinical_record_patient_active",
                        columnNames = {"patient_id", "status"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClinicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Paciente al que pertenece la historia clínica
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clinical_record_patient"))
    private Patient patient;

    // Clínica (redundante pero útil para consultas rápidas)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_clinical_record_clinic"))
    private Clinic clinic;

    // Odontólogo responsable que abrió la historia
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dentist_id",
            foreignKey = @ForeignKey(name = "fk_clinical_record_dentist"))
    private User dentist;

    // Fecha de apertura de la historia clínica
    @Column(name = "opening_date")
    private LocalDate openingDate;

    // Primera visita odontológica (puede coincidir con openingDate)
    @Column(name = "first_visit_date")
    private LocalDate firstVisitDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private ClinicalRecordStatus status = ClinicalRecordStatus.ACTIVE;

    // ===== Motivo de consulta / enfermedad actual =====

    @Column(name = "chief_complaint", length = 1000)
    private String chiefComplaint; // motivo de consulta (texto libre)

    @Column(name = "current_illness", columnDefinition = "text")
    private String currentIllness; // enfermedad actual / historia del problema

    @Column(name = "mh_allergies", length = 2000)
    private String allergies;

    @Column(name = "mh_medications", length = 2000)
    private String medications;

    @Column(name = "mh_systemic_conditions", length = 2000)
    private String systemicConditions;

    @Column(name = "mh_pregnancy_status", length = 1000)
    private String pregnancyStatus;

    @Column(name = "mh_risk_behaviors", length = 2000)
    private String riskBehaviors;

    // ===== Antecedentes médicos generales =====

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "hasDiabetes", column = @Column(name = "mh_has_diabetes")),
            @AttributeOverride(name = "hasHypertension", column = @Column(name = "mh_has_hypertension")),
            @AttributeOverride(name = "hasCardioDisease", column = @Column(name = "mh_has_cardio_disease")),
            @AttributeOverride(name = "hasRespiratoryDisease", column = @Column(name = "mh_has_respiratory_disease")),
            @AttributeOverride(name = "hasCoagulationDisorder", column = @Column(name = "mh_has_coagulation_disorder")),
            @AttributeOverride(name = "hasKidneyDisease", column = @Column(name = "mh_has_kidney_disease")),
            @AttributeOverride(name = "hasLiverDisease", column = @Column(name = "mh_has_liver_disease")),
            @AttributeOverride(name = "hasNeurologicDisease", column = @Column(name = "mh_has_neurologic_disease")),
            @AttributeOverride(name = "hasThyroidDisease", column = @Column(name = "mh_has_thyroid_disease")),
            @AttributeOverride(name = "hasImmunosuppression", column = @Column(name = "mh_has_immunosuppression")),
            @AttributeOverride(name = "hasDrugAllergy", column = @Column(name = "mh_has_drug_allergy")),
            @AttributeOverride(name = "drugAllergyDetails", column = @Column(name = "mh_drug_allergy_details", length = 500)),
            @AttributeOverride(name = "hasAnestheticAllergy", column = @Column(name = "mh_has_anesthetic_allergy")),
            @AttributeOverride(name = "anestheticAllergyDetails", column = @Column(name = "mh_anesthetic_allergy_details", length = 500)),
            @AttributeOverride(name = "otherAllergies", column = @Column(name = "mh_other_allergies", length = 500)),
            @AttributeOverride(name = "previousSurgeries", column = @Column(name = "mh_previous_surgeries", length = 1000)),
            @AttributeOverride(name = "currentMedications", column = @Column(name = "mh_current_medications", length = 1000)),
            @AttributeOverride(name = "smokingStatus", column = @Column(name = "mh_smoking_status", length = 50)),
            @AttributeOverride(name = "alcoholUse", column = @Column(name = "mh_alcohol_use", length = 50)),
            @AttributeOverride(name = "drugUse", column = @Column(name = "mh_drug_use", length = 255)),
            @AttributeOverride(name = "isPregnant", column = @Column(name = "mh_is_pregnant")),
            @AttributeOverride(name = "pregnancyWeeks", column = @Column(name = "mh_pregnancy_weeks")),
            @AttributeOverride(name = "notes", column = @Column(name = "mh_notes", length = 2000))
    })
    private MedicalHistoryEmbeddable medicalHistory;

    // ===== Antecedentes odontológicos =====

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "lastDentalVisitDate", column = @Column(name = "dh_last_dental_visit_date")),
            @AttributeOverride(name = "visitReasonType", column = @Column(name = "dh_visit_reason_type", length = 100)),
            @AttributeOverride(name = "previousTreatments", column = @Column(name = "dh_previous_treatments", length = 2000)),
            @AttributeOverride(name = "hasBruxism", column = @Column(name = "dh_has_bruxism")),
            @AttributeOverride(name = "hasOnychophagia", column = @Column(name = "dh_has_onychophagia")),
            @AttributeOverride(name = "otherHabits", column = @Column(name = "dh_other_habits", length = 500)),
            @AttributeOverride(name = "brushingFrequencyPerDay", column = @Column(name = "dh_brushing_frequency_per_day")),
            @AttributeOverride(name = "usesFloss", column = @Column(name = "dh_uses_floss")),
            @AttributeOverride(name = "usesMouthwash", column = @Column(name = "dh_uses_mouthwash")),
            @AttributeOverride(name = "dentalFearLevel", column = @Column(name = "dh_dental_fear_level", length = 50)),
            @AttributeOverride(name = "orthodonticHistory", column = @Column(name = "dh_orthodontic_history", length = 1000)),
            @AttributeOverride(name = "notes", column = @Column(name = "dh_notes", length = 2000))
    })
    private DentalHistoryEmbeddable dentalHistory;

    // ===== Signos vitales iniciales =====

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "bloodPressureSystolic", column = @Column(name = "vs_bp_systolic")),
            @AttributeOverride(name = "bloodPressureDiastolic", column = @Column(name = "vs_bp_diastolic")),
            @AttributeOverride(name = "heartRate", column = @Column(name = "vs_heart_rate")),
            @AttributeOverride(name = "respiratoryRate", column = @Column(name = "vs_respiratory_rate")),
            @AttributeOverride(name = "temperatureCelsius", column = @Column(name = "vs_temperature_celsius")),
            @AttributeOverride(name = "oxygenSaturation", column = @Column(name = "vs_oxygen_saturation")),
            @AttributeOverride(name = "notes", column = @Column(name = "vs_notes", length = 1000))
    })
    private VitalSignsEmbeddable vitalSigns;

    // ===== Examen extraoral =====

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "faceSymmetry", column = @Column(name = "eo_face_symmetry", length = 255)),
            @AttributeOverride(name = "tmjFindings", column = @Column(name = "eo_tmj_findings", length = 1000)),
            @AttributeOverride(name = "lymphNodes", column = @Column(name = "eo_lymph_nodes", length = 1000)),
            @AttributeOverride(name = "otherFindings", column = @Column(name = "eo_other_findings", length = 2000))
    })
    private ExtraoralExamEmbeddable extraoralExam;

    // ===== Examen intraoral =====

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "oralMucosa", column = @Column(name = "io_oral_mucosa", length = 1000)),
            @AttributeOverride(name = "gingivalStatus", column = @Column(name = "io_gingival_status", length = 1000)),
            @AttributeOverride(name = "plaqueLevel", column = @Column(name = "io_plaque_level", length = 50)),
            @AttributeOverride(name = "calculusLevel", column = @Column(name = "io_calculus_level", length = 50)),
            @AttributeOverride(name = "tongueFindings", column = @Column(name = "io_tongue_findings", length = 1000)),
            @AttributeOverride(name = "palateFindings", column = @Column(name = "io_palate_findings", length = 1000)),
            @AttributeOverride(name = "floorOfMouthFindings", column = @Column(name = "io_floor_of_mouth_findings", length = 1000)),
            @AttributeOverride(name = "occlusionNotes", column = @Column(name = "io_occlusion_notes", length = 2000)),
            @AttributeOverride(name = "otherFindings", column = @Column(name = "io_other_findings", length = 2000))
    })
    private IntraoralExamEmbeddable intraoralExam;

    // ===== Diagnóstico y plan inicial (vista general) =====

    @Column(name = "initial_diagnostic_summary", columnDefinition = "text")
    private String initialDiagnosticSummary;

    @Column(name = "initial_treatment_plan_summary", columnDefinition = "text")
    private String initialTreatmentPlanSummary;

    @Column(name = "initial_prognosis", length = 100)
    private String initialPrognosis; // favorable / reservado / malo / etc.

    // ===== Auditoría =====

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    // --- Enum de estado ---
    public enum ClinicalRecordStatus {
        ACTIVE,
        CLOSED
    }

    // ===== Embeddables =====

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MedicalHistoryEmbeddable {
        private Boolean hasDiabetes;
        private Boolean hasHypertension;
        private Boolean hasCardioDisease;
        private Boolean hasRespiratoryDisease;
        private Boolean hasCoagulationDisorder;
        private Boolean hasKidneyDisease;
        private Boolean hasLiverDisease;
        private Boolean hasNeurologicDisease;
        private Boolean hasThyroidDisease;
        private Boolean hasImmunosuppression;

        private Boolean hasDrugAllergy;
        private String drugAllergyDetails;

        private Boolean hasAnestheticAllergy;
        private String anestheticAllergyDetails;

        private String otherAllergies;

        private String previousSurgeries;
        private String currentMedications;

        private String smokingStatus; // never / ex / current / cantidad
        private String alcoholUse;    // none / ocasional / frecuente
        private String drugUse;       // texto libre

        private Boolean isPregnant;
        private Integer pregnancyWeeks;

        private String notes;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DentalHistoryEmbeddable {
        private LocalDate lastDentalVisitDate;
        private String visitReasonType; // urgencias / control / estética / etc.

        private String previousTreatments; // resumen de endo, prótesis, ortodoncia, etc.

        private Boolean hasBruxism;
        private Boolean hasOnychophagia;
        private String otherHabits;

        private Integer brushingFrequencyPerDay;
        private Boolean usesFloss;
        private Boolean usesMouthwash;

        private String dentalFearLevel; // none / leve / moderado / severo

        private String orthodonticHistory;
        private String notes;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VitalSignsEmbeddable {
        private Integer bloodPressureSystolic;
        private Integer bloodPressureDiastolic;
        private Integer heartRate;
        private Integer respiratoryRate;
        private Double temperatureCelsius;
        private Integer oxygenSaturation;

        private String notes;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExtraoralExamEmbeddable {
        private String faceSymmetry;
        private String tmjFindings;
        private String lymphNodes;
        private String otherFindings;
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class IntraoralExamEmbeddable {
        private String oralMucosa;
        private String gingivalStatus;
        private String plaqueLevel;
        private String calculusLevel;
        private String tongueFindings;
        private String palateFindings;
        private String floorOfMouthFindings;
        private String occlusionNotes;
        private String otherFindings;
    }
}
