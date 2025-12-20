package com.app_odontologia.diplomado_final.dto.clinical;

import com.app_odontologia.diplomado_final.dto.attachment.AttachmentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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

    // ===== Motivo =====
    private String chiefComplaint;
    private String currentIllness;

    // ===== Campos planos =====
    private String allergies;
    private String medications;
    private String systemicConditions;
    private String pregnancyStatus;
    private String riskBehaviors;

    // 👉👉 CAMPOS QUE FALTABAN 👈👈
    private String periodontalStatus;
    private String cariesRisk;

    // ===== Embeddables =====
    private MedicalHistoryDto medicalHistory;
    private DentalHistoryDto dentalHistory;
    private VitalSignsDto vitalSigns;
    private ExtraoralExamDto extraoralExam;
    private IntraoralExamDto intraoralExam;

    // ===== Diagnóstico =====
    private String initialDiagnosticSummary;
    private String initialTreatmentPlanSummary;
    private String initialPrognosis;

    // ===== Auditoría =====
    private Instant createdAt;
    private Instant updatedAt;

    // ===== Adjuntos =====
    private List<AttachmentDto> attachments;
}
