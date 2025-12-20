package com.app_odontologia.diplomado_final.metrics.stored_procedure;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MetricsServiceImpl implements MetricsService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<MonthlyAppointmentSummaryDto> getMonthlySummary(Long clinicId) {
        return jdbcTemplate.query(
                "SELECT * FROM sp_monthly_appointment_summary(?)",
                (rs, rowNum) -> new MonthlyAppointmentSummaryDto(
                        rs.getObject("year", Integer.class),
                        rs.getObject("month", Integer.class),
                        rs.getObject("total_appointments", Long.class),
                        rs.getObject("completed", Long.class),
                        rs.getObject("cancelled", Long.class),
                        rs.getObject("no_show", Long.class),
                        rs.getObject("no_show_rate", Double.class)
                ),
                clinicId
        );
    }

    @Override
    public List<TopNoShowPatientDto> getTopNoShowPatients(Long clinicId, int limit) {
        return jdbcTemplate.query(
                "SELECT * FROM sp_top_no_show_patients(?, ?)",
                (rs, rowNum) -> new TopNoShowPatientDto(
                        rs.getObject("patient_id", Long.class),
                        rs.getString("full_name"),
                        rs.getObject("total_no_shows", Integer.class),
                        rs.getObject("total_cancellations", Integer.class),
                        rs.getObject("consecutive_no_shows", Integer.class),
                        rs.getString("risk_level"),
                        rs.getTimestamp("last_no_show_at") != null
                                ? rs.getTimestamp("last_no_show_at").toInstant()
                                : null
                ),
                clinicId,
                limit
        );
    }

    @Override
    public List<DoctorAgendaImpactDto> getDoctorAgendaImpact(
            Long clinicId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return jdbcTemplate.query(
                "SELECT * FROM sp_doctor_agenda_impact(?, ?, ?)",
                (rs, rowNum) -> new DoctorAgendaImpactDto(
                        rs.getObject("doctor_id", Long.class),
                        rs.getObject("total_appointments", Long.class),
                        rs.getObject("completed", Long.class),
                        rs.getObject("no_show", Long.class),
                        rs.getObject("late_cancellations", Long.class),
                        rs.getObject("total_minutes_lost", Long.class),
                        rs.getObject("no_show_rate", Double.class)
                ),
                clinicId,
                startDate,
                endDate
        );
    }

    @Override
    public ClinicKpiDto getClinicKpis(
            Long clinicId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM sp_clinic_kpis(?, ?, ?)",
                (rs, rowNum) -> new ClinicKpiDto(
                        rs.getObject("total_appointments", Long.class),
                        rs.getObject("completed", Long.class),
                        rs.getObject("no_show", Long.class),
                        rs.getObject("late_cancellations", Long.class),
                        rs.getObject("no_show_rate", Double.class),
                        rs.getObject("total_minutes_lost", Long.class),
                        rs.getObject("blocked_patients", Long.class)
                ),
                clinicId,
                startDate,
                endDate
        );
    }
}
