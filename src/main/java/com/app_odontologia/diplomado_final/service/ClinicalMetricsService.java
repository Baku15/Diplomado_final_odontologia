package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.metrics.ClinicalConsultationMetricsDto;

public interface ClinicalMetricsService {

    ClinicalConsultationMetricsDto getConsultationMetrics(Long clinicId);
}
