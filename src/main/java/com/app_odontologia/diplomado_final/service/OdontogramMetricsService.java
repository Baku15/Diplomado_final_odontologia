package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.odontogram.metrics.OdontogramMetricsDto;

public interface OdontogramMetricsService {

    OdontogramMetricsDto getMetrics(
            Long clinicId,
            Long patientId
    );
}
