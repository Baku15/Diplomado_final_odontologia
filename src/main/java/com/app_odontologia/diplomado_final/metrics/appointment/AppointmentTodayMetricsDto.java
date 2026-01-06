package com.app_odontologia.diplomado_final.metrics.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppointmentTodayMetricsDto {

    private long total;
    private long scheduled;
    private long completed;
    private long cancelled;
    private long noShow;

    private double completionRate;
    private double noShowRate;
}
