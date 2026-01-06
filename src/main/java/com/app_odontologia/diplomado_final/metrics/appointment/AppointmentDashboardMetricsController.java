package com.app_odontologia.diplomado_final.metrics.appointment;

import com.app_odontologia.diplomado_final.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/dashboard/doctor/appointments")
@RequiredArgsConstructor
public class AppointmentDashboardMetricsController {

    private final AppointmentDashboardMetricsService service;

    @GetMapping
    public AppointmentDashboardDto getDashboardMetrics(
            @RequestParam AppointmentMetricPeriod period,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            Authentication authentication
    ) {

        Long doctorId;

        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            doctorId = user.getId();
        } else if (principal instanceof Jwt jwt) {
            doctorId = service.resolveDoctorIdByUsername(jwt.getSubject());
        } else {
            throw new IllegalStateException("Principal no soportado");
        }

        ZoneId zone = ZoneId.of("America/La_Paz");
        LocalDate today = LocalDate.now(zone);

        // =========================
        // 🎯 DEFINICIÓN CORRECTA DE RANGOS
        // =========================
        LocalDate from;
        LocalDate to;

        switch (period) {
            case TODAY -> {
                from = today;
                to = today;
            }
            case WEEK -> {
                from = today;
                to = today.plusDays(6);
            }
            case MONTH -> {
                from = today.withDayOfMonth(1);
                to = today.withDayOfMonth(today.lengthOfMonth());
            }
            case CUSTOM -> {
                if (start == null || end == null) {
                    throw new IllegalArgumentException("CUSTOM requiere start y end");
                }
                from = start;
                to = end;
            }
            default -> throw new IllegalStateException("Periodo no soportado");
        }

        return service.getDashboardMetrics(
                doctorId,
                period,
                new AppointmentMetricRange(from, to)
        );
    }
}
