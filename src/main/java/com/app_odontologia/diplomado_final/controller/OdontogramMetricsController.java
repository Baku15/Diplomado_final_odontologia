package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.odontogram.metrics.OdontogramMetricsDto;
import com.app_odontologia.diplomado_final.service.OdontogramMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinic/{clinicId}/patients/{patientId}/odontogram/metrics")
@RequiredArgsConstructor
public class OdontogramMetricsController {

    private final OdontogramMetricsService odontogramMetricsService;

    @GetMapping
    @PreAuthorize("hasRole('ROLE_DENTIST') or hasRole('ROLE_CLINIC_ADMIN')")
    public ResponseEntity<OdontogramMetricsDto> getMetrics(
            @PathVariable Long clinicId,
            @PathVariable Long patientId
    ) {
        return ResponseEntity.ok(
                odontogramMetricsService.getMetrics(clinicId, patientId)
        );
    }
}
