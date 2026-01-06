package com.app_odontologia.diplomado_final.metrics.consultations;

public record ConsultationRiskMetricsDto(
        long openOver2Hours,
        long openOver1Day,
        double avgOpenDurationMinutes
) {}
