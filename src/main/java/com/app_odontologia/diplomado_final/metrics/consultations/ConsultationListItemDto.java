package com.app_odontologia.diplomado_final.metrics.consultations;

import java.time.Instant;

public record ConsultationListItemDto(
        Long id,
        Long patientId,
        String patientName,
        Instant startedAt,
        Instant endedAt,
        String status
) {}
