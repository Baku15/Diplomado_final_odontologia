package com.app_odontologia.diplomado_final.metrics.patients;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics/patients")
@RequiredArgsConstructor
public class PatientMetricsController {

    private final PatientMetricsService service;

    @GetMapping
    public PatientMetricsDto getMetrics(
            @RequestAttribute("clinicId") Long clinicId
    ) {
        return service.getPatientMetrics(clinicId);
    }
}
