package com.app_odontologia.diplomado_final.metrics.consultations;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics/consultations")
@RequiredArgsConstructor
public class ConsultationMetricsController {

    private final ConsultationMetricsService service;

    @GetMapping
    public ConsultationMetricsDto getMetrics(
            @RequestAttribute("clinicId") Long clinicId
    ) {
        return service.getConsultationMetrics(clinicId);
    }
}
