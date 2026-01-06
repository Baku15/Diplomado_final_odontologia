package com.app_odontologia.diplomado_final.metrics.consultations;

import java.time.LocalDate;
import java.util.Map;

public record ConsultationHistoricalMetricsDto(
        Map<String, Long> closedByDate
) {}
