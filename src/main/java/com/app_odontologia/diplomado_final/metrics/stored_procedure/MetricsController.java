package com.app_odontologia.diplomado_final.metrics.stored_procedure;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/clinic/{clinicId}/metrics")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN','ROLE_DENTIST')")
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/monthly-summary")
    public List<MonthlyAppointmentSummaryDto> monthlySummary(
            @PathVariable Long clinicId
    ) {
        return metricsService.getMonthlySummary(clinicId);
    }

    @GetMapping("/top-no-show-patients")
    public List<TopNoShowPatientDto> topNoShowPatients(
            @PathVariable Long clinicId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return metricsService.getTopNoShowPatients(clinicId, limit);
    }

    @GetMapping("/doctor-impact")
    public List<DoctorAgendaImpactDto> doctorImpact(
            @PathVariable Long clinicId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return metricsService.getDoctorAgendaImpact(
                clinicId,
                startDate,
                endDate
        );
    }

    @GetMapping("/kpis")
    public ClinicKpiDto clinicKpis(
            @PathVariable Long clinicId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate
    ) {
        return metricsService.getClinicKpis(
                clinicId,
                startDate,
                endDate
        );
    }
}
