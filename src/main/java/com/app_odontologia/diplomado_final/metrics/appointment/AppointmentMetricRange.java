package com.app_odontologia.diplomado_final.metrics.appointment;

import java.time.LocalDate;

public record AppointmentMetricRange(
        LocalDate start,
        LocalDate end
) {
}
