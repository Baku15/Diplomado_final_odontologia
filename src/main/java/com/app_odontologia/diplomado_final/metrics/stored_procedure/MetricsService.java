package com.app_odontologia.diplomado_final.metrics.stored_procedure;

import java.time.LocalDate;
import java.util.List;

public interface MetricsService {

    List<MonthlyAppointmentSummaryDto> getMonthlySummary(Long clinicId);

    List<TopNoShowPatientDto> getTopNoShowPatients(Long clinicId, int limit);

    List<DoctorAgendaImpactDto> getDoctorAgendaImpact(
            Long clinicId,
            LocalDate startDate,
            LocalDate endDate
    );

    ClinicKpiDto getClinicKpis(
            Long clinicId,
            LocalDate startDate,
            LocalDate endDate
    );
}
