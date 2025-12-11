package com.app_odontologia.diplomado_final.dto.clinical;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalRecordDetailDto {

    private Long id;

    private Long clinicId;
    private Long patientId;
    private Long dentistId;

    private LocalDate openingDate;
    private LocalDate firstVisitDate;
    private String status; // ACTIVE / CLOSED

    private String chiefComplaint;
    private String currentIllness;

    private String allergies;
    private String medications;
    private String systemicConditions;
    private String pregnancyStatus;
    private String riskBehaviors;

    private MedicalHistoryDto medicalHistory;
    private DentalHistoryDto dentalHistory;
    private VitalSignsDto vitalSigns;
    private ExtraoralExamDto extraoralExam;
    private IntraoralExamDto intraoralExam;

    private String initialDiagnosticSummary;
    private String initialTreatmentPlanSummary;
    private String initialPrognosis;

    private Instant createdAt;
    private Instant updatedAt;
}
