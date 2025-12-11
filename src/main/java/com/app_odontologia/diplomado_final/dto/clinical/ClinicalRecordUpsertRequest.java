package com.app_odontologia.diplomado_final.dto.clinical;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para crear o actualizar la historia clínica.
 * No incluye id ni timestamps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalRecordUpsertRequest {

    private LocalDate openingDate;     // opcional; si es null usamos hoy
    private LocalDate firstVisitDate;  // opcional

    private String chiefComplaint;
    private String currentIllness;

    private String allergies;          // textarea "Alergias"
    private String medications;        // textarea "Medicación actual"
    private String systemicConditions; // textarea "Enfermedades sistémicas"
    private String pregnancyStatus;    // textarea "Embarazo (si aplica)"
    private String riskBehaviors;      // textarea "Conductas de riesgo"

    private MedicalHistoryDto medicalHistory;
    private DentalHistoryDto dentalHistory;
    private VitalSignsDto vitalSigns;
    private ExtraoralExamDto extraoralExam;
    private IntraoralExamDto intraoralExam;

    private String initialDiagnosticSummary;
    private String initialTreatmentPlanSummary;
    private String initialPrognosis;
}
