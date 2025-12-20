package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.metrics.ClinicalConsultationMetricsDto;
import com.app_odontologia.diplomado_final.service.ClinicalMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinic/{clinicId}/metrics")
@RequiredArgsConstructor
public class ClinicalMetricsController {

    private final ClinicalMetricsService metricsService;

    @GetMapping("/consultations")
    public ResponseEntity<ClinicalConsultationMetricsDto> getConsultationMetrics(
            @PathVariable Long clinicId
    ) {
        return ResponseEntity.ok(
                metricsService.getConsultationMetrics(clinicId)
        );
    }
}
