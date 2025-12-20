package com.app_odontologia.diplomado_final.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClinicalConsultationMetricsDto {

    // Consultas
    private long totalConsultations;
    private long activeConsultations;
    private long closedConsultations;

    // Tiempo
    private double averageDurationMinutes;
}
