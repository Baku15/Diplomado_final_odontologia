package com.app_odontologia.diplomado_final.metrics.consultations;

public record ConsultationTimeMetricsDto(
        long averageDurationMinutes,
        long longestDurationMinutes,
        long longConsultationsCount
) {}
