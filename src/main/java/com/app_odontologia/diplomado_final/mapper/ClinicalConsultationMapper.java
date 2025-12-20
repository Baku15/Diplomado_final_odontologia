package com.app_odontologia.diplomado_final.mapper;

import com.app_odontologia.diplomado_final.dto.consultation.ClinicalConsultationDto;
import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation;

public class ClinicalConsultationMapper {

    private ClinicalConsultationMapper() {}

    public static ClinicalConsultationDto toDto(ClinicalConsultation c) {
        return ClinicalConsultationDto.builder()
                .id(c.getId())
                .clinicId(c.getClinic().getId())
                .patientId(c.getPatient().getId())
                .dentistId(c.getDentist().getId())
                .dentistName(
                        c.getDentist().getNombres() + " " + c.getDentist().getApellidos()
                )
                .dentalChartId(c.getDentalChart().getId())
                .status(c.getStatus().name())
                .startedAt(c.getStartedAt())
                .endedAt(c.getEndedAt())
                .clinicalNotes(c.getClinicalNotes())
                .summary(c.getSummary())

                .build();
    }
}
