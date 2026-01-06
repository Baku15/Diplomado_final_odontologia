package com.app_odontologia.diplomado_final.metrics.patients;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PatientListItemDto {
    private Long id;
    private String fullName;
    private String riskLevel;
    private Long consultations;
}
