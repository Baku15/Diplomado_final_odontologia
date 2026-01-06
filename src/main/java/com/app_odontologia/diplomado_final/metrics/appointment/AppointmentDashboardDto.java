package com.app_odontologia.diplomado_final.metrics.appointment;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppointmentDashboardDto {

    private AppointmentTodayMetricsDto today;
    private AppointmentHistoricalMetricsDto historical;
    private AppointmentFutureMetricsDto future;
}
