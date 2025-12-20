package com.app_odontologia.diplomado_final.metrics.consultations;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ConsultationMetricsDto {

    private long totalConsultations;
    private long activeConsultations;
    private long closedConsultations;

    private Double averageDurationMinutes;

    private List<MonthlyCount> consultationsByMonth;

    @Data
    @AllArgsConstructor
    public static class MonthlyCount {
        private String month; // YYYY-MM
        private long count;
    }
}
