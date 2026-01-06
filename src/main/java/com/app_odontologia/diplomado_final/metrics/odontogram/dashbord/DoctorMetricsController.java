package com.app_odontologia.diplomado_final.metrics.odontogram.dashbord;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard/doctor")
@RequiredArgsConstructor
public class DoctorMetricsController {

    private final DoctorDashboardService service;

    // =========================
    // MÉTRICAS GENERALES
    // =========================
    @GetMapping("/metrics")
    public ResponseEntity<DoctorDashboardMetricsDto> getMetrics(
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @AuthenticationPrincipal Jwt jwt
    ) {
        Long clinicId = jwt.getClaim("clinic_id");
        Long dentistId = jwt.getClaim("user_id"); // ajusta si tu claim se llama distinto

        return ResponseEntity.ok(
                service.getDoctorMetrics(
                        clinicId,
                        dentistId,
                        from.atStartOfDay().toInstant(ZoneOffset.UTC),
                        to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                )
        );
    }

    // =========================
    // DETALLE DE DIENTES INTERVENIDOS
    // =========================
    @GetMapping("/odontogram/teeth-detail")
    public ResponseEntity<List<ToothInterventionDetailDto>> getTeethInterventionDetail(
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate from,

            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate to,

            @AuthenticationPrincipal Jwt jwt
    ) {
        Long clinicId = jwt.getClaim("clinic_id");

        return ResponseEntity.ok(
                service.getTeethInterventionDetail(
                        clinicId,
                        from.atStartOfDay().toInstant(ZoneOffset.UTC),
                        to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                )
        );
    }
}
