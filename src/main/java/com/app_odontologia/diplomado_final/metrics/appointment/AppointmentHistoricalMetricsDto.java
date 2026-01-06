package com.app_odontologia.diplomado_final.metrics.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@AllArgsConstructor
public class AppointmentHistoricalMetricsDto {

    // Ej: 2025-05-01 -> 12 citas completadas
    private Map<LocalDate, Long> completedByDate;

    // Ej: 2025-05-01 -> 3 no-shows
    private Map<LocalDate, Long> noShowByDate;
}
