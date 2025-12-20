package com.app_odontologia.diplomado_final.metrics.appointment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AppointmentMetricsDto {

    private long scheduled;
    private long completed;
    private long cancelled;
    private long noShow;

    private double completionRate; // %
}
