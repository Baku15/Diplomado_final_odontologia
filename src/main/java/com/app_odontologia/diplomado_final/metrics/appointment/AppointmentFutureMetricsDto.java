package com.app_odontologia.diplomado_final.metrics.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppointmentFutureMetricsDto {

    private long nextWeekScheduled;
    private long nextMonthScheduled;

    private double nextWeekOccupancyRate;
    private double nextMonthOccupancyRate;
}
