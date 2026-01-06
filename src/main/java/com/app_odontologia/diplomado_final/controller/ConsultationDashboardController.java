package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.metrics.consultations.ConsultationDashboardMetricsDto;
import com.app_odontologia.diplomado_final.metrics.consultations.ConsultationDashboardMetricsService;
import com.app_odontologia.diplomado_final.metrics.consultations.ConsultationMetricPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard/doctor/consultations")
@RequiredArgsConstructor
public class ConsultationDashboardController {

    private final ConsultationDashboardMetricsService service;

    @GetMapping
    public ConsultationDashboardMetricsDto getMetrics(
            @RequestParam ConsultationMetricPeriod period,
            Authentication authentication
    ) {
        return service.getDashboardMetrics(period, authentication);
    }
}
