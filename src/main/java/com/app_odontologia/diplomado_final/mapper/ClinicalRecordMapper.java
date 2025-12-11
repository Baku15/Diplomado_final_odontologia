package com.app_odontologia.diplomado_final.mapper;

import com.app_odontologia.diplomado_final.dto.clinical.*;
import com.app_odontologia.diplomado_final.model.entity.ClinicalRecord;
import com.app_odontologia.diplomado_final.model.entity.ClinicalRecord.*;

public class ClinicalRecordMapper {

    private ClinicalRecordMapper() {
        // util
    }

    // ===== entity -> dto =====

    public static ClinicalRecordDetailDto toDetailDto(ClinicalRecord cr) {
        if (cr == null) return null;

        return ClinicalRecordDetailDto.builder()
                .id(cr.getId())
                .clinicId(cr.getClinic() != null ? cr.getClinic().getId() : null)
                .patientId(cr.getPatient() != null ? cr.getPatient().getId() : null)
                .dentistId(cr.getDentist() != null ? cr.getDentist().getId() : null)
                .openingDate(cr.getOpeningDate())
                .firstVisitDate(cr.getFirstVisitDate())
                .status(cr.getStatus() != null ? cr.getStatus().name() : null)
                .chiefComplaint(cr.getChiefComplaint())
                .currentIllness(cr.getCurrentIllness())

                // 🎯 NUEVOS CAMPOS
                .allergies(cr.getAllergies())
                .medications(cr.getMedications())
                .systemicConditions(cr.getSystemicConditions())
                .pregnancyStatus(cr.getPregnancyStatus())
                .riskBehaviors(cr.getRiskBehaviors())

                .medicalHistory(toMedicalHistoryDto(cr.getMedicalHistory()))
                .dentalHistory(toDentalHistoryDto(cr.getDentalHistory()))
                .vitalSigns(toVitalSignsDto(cr.getVitalSigns()))
                .extraoralExam(toExtraoralExamDto(cr.getExtraoralExam()))
                .intraoralExam(toIntraoralExamDto(cr.getIntraoralExam()))
                .initialDiagnosticSummary(cr.getInitialDiagnosticSummary())
                .initialTreatmentPlanSummary(cr.getInitialTreatmentPlanSummary())
                .initialPrognosis(cr.getInitialPrognosis())
                .createdAt(cr.getCreatedAt())
                .updatedAt(cr.getUpdatedAt())
                .build();
    }

    private static MedicalHistoryDto toMedicalHistoryDto(MedicalHistoryEmbeddable m) {
        if (m == null) return null;
        return MedicalHistoryDto.builder()
                .hasDiabetes(m.getHasDiabetes())
                .hasHypertension(m.getHasHypertension())
                .hasCardioDisease(m.getHasCardioDisease())
                .hasRespiratoryDisease(m.getHasRespiratoryDisease())
                .hasCoagulationDisorder(m.getHasCoagulationDisorder())
                .hasKidneyDisease(m.getHasKidneyDisease())
                .hasLiverDisease(m.getHasLiverDisease())
                .hasNeurologicDisease(m.getHasNeurologicDisease())
                .hasThyroidDisease(m.getHasThyroidDisease())
                .hasImmunosuppression(m.getHasImmunosuppression())
                .hasDrugAllergy(m.getHasDrugAllergy())
                .drugAllergyDetails(m.getDrugAllergyDetails())
                .hasAnestheticAllergy(m.getHasAnestheticAllergy())
                .anestheticAllergyDetails(m.getAnestheticAllergyDetails())
                .otherAllergies(m.getOtherAllergies())
                .previousSurgeries(m.getPreviousSurgeries())
                .currentMedications(m.getCurrentMedications())
                .smokingStatus(m.getSmokingStatus())
                .alcoholUse(m.getAlcoholUse())
                .drugUse(m.getDrugUse())
                .isPregnant(m.getIsPregnant())
                .pregnancyWeeks(m.getPregnancyWeeks())
                .notes(m.getNotes())
                .build();
    }

    private static DentalHistoryDto toDentalHistoryDto(DentalHistoryEmbeddable d) {
        if (d == null) return null;
        return DentalHistoryDto.builder()
                .lastDentalVisitDate(d.getLastDentalVisitDate())
                .visitReasonType(d.getVisitReasonType())
                .previousTreatments(d.getPreviousTreatments())
                .hasBruxism(d.getHasBruxism())
                .hasOnychophagia(d.getHasOnychophagia())
                .otherHabits(d.getOtherHabits())
                .brushingFrequencyPerDay(d.getBrushingFrequencyPerDay())
                .usesFloss(d.getUsesFloss())
                .usesMouthwash(d.getUsesMouthwash())
                .dentalFearLevel(d.getDentalFearLevel())
                .orthodonticHistory(d.getOrthodonticHistory())
                .notes(d.getNotes())
                .build();
    }

    private static VitalSignsDto toVitalSignsDto(VitalSignsEmbeddable v) {
        if (v == null) return null;
        return VitalSignsDto.builder()
                .bloodPressureSystolic(v.getBloodPressureSystolic())
                .bloodPressureDiastolic(v.getBloodPressureDiastolic())
                .heartRate(v.getHeartRate())
                .respiratoryRate(v.getRespiratoryRate())
                .temperatureCelsius(v.getTemperatureCelsius())
                .oxygenSaturation(v.getOxygenSaturation())
                .notes(v.getNotes())
                .build();
    }

    private static ExtraoralExamDto toExtraoralExamDto(ExtraoralExamEmbeddable e) {
        if (e == null) return null;
        return ExtraoralExamDto.builder()
                .faceSymmetry(e.getFaceSymmetry())
                .tmjFindings(e.getTmjFindings())
                .lymphNodes(e.getLymphNodes())
                .otherFindings(e.getOtherFindings())
                .build();
    }

    private static IntraoralExamDto toIntraoralExamDto(IntraoralExamEmbeddable i) {
        if (i == null) return null;
        return IntraoralExamDto.builder()
                .oralMucosa(i.getOralMucosa())
                .gingivalStatus(i.getGingivalStatus())
                .plaqueLevel(i.getPlaqueLevel())
                .calculusLevel(i.getCalculusLevel())
                .tongueFindings(i.getTongueFindings())
                .palateFindings(i.getPalateFindings())
                .floorOfMouthFindings(i.getFloorOfMouthFindings())
                .occlusionNotes(i.getOcclusionNotes())
                .otherFindings(i.getOtherFindings())
                .build();
    }

    // ===== dto -> embeddables para create/update =====

    public static MedicalHistoryEmbeddable toMedicalHistoryEmbeddable(MedicalHistoryDto d) {
        if (d == null) return null;
        return MedicalHistoryEmbeddable.builder()
                .hasDiabetes(d.getHasDiabetes())
                .hasHypertension(d.getHasHypertension())
                .hasCardioDisease(d.getHasCardioDisease())
                .hasRespiratoryDisease(d.getHasRespiratoryDisease())
                .hasCoagulationDisorder(d.getHasCoagulationDisorder())
                .hasKidneyDisease(d.getHasKidneyDisease())
                .hasLiverDisease(d.getHasLiverDisease())
                .hasNeurologicDisease(d.getHasNeurologicDisease())
                .hasThyroidDisease(d.getHasThyroidDisease())
                .hasImmunosuppression(d.getHasImmunosuppression())
                .hasDrugAllergy(d.getHasDrugAllergy())
                .drugAllergyDetails(d.getDrugAllergyDetails())
                .hasAnestheticAllergy(d.getHasAnestheticAllergy())
                .anestheticAllergyDetails(d.getAnestheticAllergyDetails())
                .otherAllergies(d.getOtherAllergies())
                .previousSurgeries(d.getPreviousSurgeries())
                .currentMedications(d.getCurrentMedications())
                .smokingStatus(d.getSmokingStatus())
                .alcoholUse(d.getAlcoholUse())
                .drugUse(d.getDrugUse())
                .isPregnant(d.getIsPregnant())
                .pregnancyWeeks(d.getPregnancyWeeks())
                .notes(d.getNotes())
                .build();
    }

    public static DentalHistoryEmbeddable toDentalHistoryEmbeddable(DentalHistoryDto d) {
        if (d == null) return null;
        return DentalHistoryEmbeddable.builder()
                .lastDentalVisitDate(d.getLastDentalVisitDate())
                .visitReasonType(d.getVisitReasonType())
                .previousTreatments(d.getPreviousTreatments())
                .hasBruxism(d.getHasBruxism())
                .hasOnychophagia(d.getHasOnychophagia())
                .otherHabits(d.getOtherHabits())
                .brushingFrequencyPerDay(d.getBrushingFrequencyPerDay())
                .usesFloss(d.getUsesFloss())
                .usesMouthwash(d.getUsesMouthwash())
                .dentalFearLevel(d.getDentalFearLevel())
                .orthodonticHistory(d.getOrthodonticHistory())
                .notes(d.getNotes())
                .build();
    }

    public static VitalSignsEmbeddable toVitalSignsEmbeddable(VitalSignsDto v) {
        if (v == null) return null;
        return VitalSignsEmbeddable.builder()
                .bloodPressureSystolic(v.getBloodPressureSystolic())
                .bloodPressureDiastolic(v.getBloodPressureDiastolic())
                .heartRate(v.getHeartRate())
                .respiratoryRate(v.getRespiratoryRate())
                .temperatureCelsius(v.getTemperatureCelsius())
                .oxygenSaturation(v.getOxygenSaturation())
                .notes(v.getNotes())
                .build();
    }

    public static ExtraoralExamEmbeddable toExtraoralExamEmbeddable(ExtraoralExamDto e) {
        if (e == null) return null;
        return ExtraoralExamEmbeddable.builder()
                .faceSymmetry(e.getFaceSymmetry())
                .tmjFindings(e.getTmjFindings())
                .lymphNodes(e.getLymphNodes())
                .otherFindings(e.getOtherFindings())
                .build();
    }

    public static IntraoralExamEmbeddable toIntraoralExamEmbeddable(IntraoralExamDto i) {
        if (i == null) return null;
        return IntraoralExamEmbeddable.builder()
                .oralMucosa(i.getOralMucosa())
                .gingivalStatus(i.getGingivalStatus())
                .plaqueLevel(i.getPlaqueLevel())
                .calculusLevel(i.getCalculusLevel())
                .tongueFindings(i.getTongueFindings())
                .palateFindings(i.getPalateFindings())
                .floorOfMouthFindings(i.getFloorOfMouthFindings())
                .occlusionNotes(i.getOcclusionNotes())
                .otherFindings(i.getOtherFindings())
                .build();
    }
}
