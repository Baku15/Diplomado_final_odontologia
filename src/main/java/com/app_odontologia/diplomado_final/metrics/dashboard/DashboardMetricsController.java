package com.app_odontologia.diplomado_final.metrics.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics/dashboard")
@RequiredArgsConstructor
public class DashboardMetricsController {

    private final DashboardMetricsService service;

    @GetMapping
    public DashboardMetricsDto getDashboardMetrics(
            @RequestAttribute("clinicId") Long clinicId
    ) {
        return service.getDashboardMetrics(clinicId);
    }
}
