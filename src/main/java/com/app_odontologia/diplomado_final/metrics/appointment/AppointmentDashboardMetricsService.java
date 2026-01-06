package com.app_odontologia.diplomado_final.metrics.appointment;

public interface AppointmentDashboardMetricsService {

    AppointmentDashboardDto getDashboardMetrics(
            Long clinicId,
            AppointmentMetricPeriod period,
            AppointmentMetricRange range
    );
    Long resolveDoctorIdByUsername(String username);
}
