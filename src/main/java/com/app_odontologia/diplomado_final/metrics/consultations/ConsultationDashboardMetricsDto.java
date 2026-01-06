package com.app_odontologia.diplomado_final.metrics.consultations;

public record ConsultationDashboardMetricsDto(
        ConsultationTodayMetricsDto today,
        ConsultationHistoricalMetricsDto historical,
        ConsultationRiskMetricsDto risk,
           ConsultationTimeMetricsDto timeMetrics
) {}
