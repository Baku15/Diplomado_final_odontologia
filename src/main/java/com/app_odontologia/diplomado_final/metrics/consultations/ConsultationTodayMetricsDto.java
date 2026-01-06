package com.app_odontologia.diplomado_final.metrics.consultations;

public record ConsultationTodayMetricsDto(
        long total,
        long inProgress,
        long closed,
        double avgDurationMinutes,
        Long longestActiveMinutes,
        ConsultationCurrentDto current
) {}
