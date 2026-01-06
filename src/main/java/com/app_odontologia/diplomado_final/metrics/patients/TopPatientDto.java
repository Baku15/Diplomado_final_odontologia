package com.app_odontologia.diplomado_final.metrics.patients;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TopPatientDto {
    private Long patientId;
    private String patientName;
    private long consultations;
}
