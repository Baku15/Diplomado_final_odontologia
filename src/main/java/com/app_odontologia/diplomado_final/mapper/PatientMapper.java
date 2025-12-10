package com.app_odontologia.diplomado_final.mapper;

import com.app_odontologia.diplomado_final.dto.patient.PatientSummaryDto;
import com.app_odontologia.diplomado_final.model.entity.Patient;

public class PatientMapper {

    public static PatientSummaryDto toSummary(Patient p) {
        return PatientSummaryDto.builder()
                .id(p.getId())
                .givenName(p.getGivenName())
                .familyName(p.getFamilyName())
                .fullName(p.getGivenName() + " " + p.getFamilyName())
                .documentType(p.getDocumentType())
                .documentNumber(p.getDocumentNumber())
                .birthDate(p.getBirthDate())
                .phoneMobile(p.getPhoneMobile())
                .email(p.getEmail())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
