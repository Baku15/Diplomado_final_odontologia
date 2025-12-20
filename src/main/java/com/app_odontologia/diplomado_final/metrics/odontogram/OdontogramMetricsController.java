package com.app_odontologia.diplomado_final.metrics.odontogram;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metrics/odontogram")
@RequiredArgsConstructor
public class OdontogramMetricsController {

    private final OdontogramMetricsService service;

    @GetMapping
    public OdontogramMetricsDto getMetrics(
            @RequestAttribute("clinicId") Long clinicId
    ) {
        return service.getMetrics(clinicId);
    }
}
