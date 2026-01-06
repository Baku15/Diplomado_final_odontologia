package com.app_odontologia.diplomado_final.metrics.patients;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard/doctor")
@RequiredArgsConstructor
public class PatientDashboardController {

    private final PatientDashboardService service;

    @GetMapping("/patients")
    public ResponseEntity<PatientDashboardMetricsDto> getMetrics(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long clinicId = jwt.getClaim("clinic_id");

        return ResponseEntity.ok(
                service.getMetrics(
                        clinicId,
                        from.atStartOfDay().toInstant(ZoneOffset.UTC),
                        to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                )
        );
    }

    @GetMapping("/patients/list")
    public ResponseEntity<List<PatientListItemDto>> getPatientsByCategory(
            @RequestParam("category") String category,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long clinicId = jwt.getClaim("clinic_id");

        return ResponseEntity.ok(
                service.getPatientsByCategory(
                        clinicId,
                        category,
                        from.atStartOfDay().toInstant(ZoneOffset.UTC),
                        to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                )
        );
    }

}
