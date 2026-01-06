package com.app_odontologia.diplomado_final.metrics.consultations;

import org.springframework.security.core.Authentication;

public interface ConsultationDashboardMetricsService {

    ConsultationDashboardMetricsDto getDashboardMetrics(
            ConsultationMetricPeriod period,
            Authentication authentication
    );
}
