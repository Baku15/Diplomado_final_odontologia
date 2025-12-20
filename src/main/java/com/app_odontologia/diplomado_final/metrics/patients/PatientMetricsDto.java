package com.app_odontologia.diplomado_final.metrics.patients;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PatientMetricsDto {

    private long totalPatients;
    private long activePatients;
    private List<MonthlyCount> registeredByMonth;

    @Data
    @AllArgsConstructor
    public static class MonthlyCount {
        private String month; // YYYY-MM
        private long count;
    }
}
