package com.app_odontologia.diplomado_final.metrics.patients;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientDashboardMetricsDto {

    private long totalPatients;
    private long newPatients;
    private long activePatients;
    private long inactivePatients;

    // 🔥 nuevos
    private long patientsInTreatment;
    private long patientsAtRisk;

    private List<PatientsByDateDto> patientsByDate;
    private List<TopPatientDto> topPatients;
}
