package com.app_odontologia.diplomado_final.metrics.consultations;

import java.time.Instant;


public record ConsultationCurrentDto(
        Long id,
        Long patientId,
        String patientName,
        Instant startedAt
) {}
