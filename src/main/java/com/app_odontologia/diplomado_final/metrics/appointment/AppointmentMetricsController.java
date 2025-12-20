package com.app_odontologia.diplomado_final.metrics.appointment;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics/appointments")
@RequiredArgsConstructor
public class AppointmentMetricsController {

    private final AppointmentMetricsService service;

    @GetMapping
    public AppointmentMetricsDto getMetrics(
            @RequestAttribute("clinicId") Long clinicId
    ) {
        return service.getMetrics(clinicId);
    }
}
