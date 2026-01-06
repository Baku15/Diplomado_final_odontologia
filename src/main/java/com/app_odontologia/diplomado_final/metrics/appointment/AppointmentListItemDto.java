package com.app_odontologia.diplomado_final.metrics.appointment;

import com.app_odontologia.diplomado_final.model.entity.Appointment.AppointmentStatus;
import com.app_odontologia.diplomado_final.model.enums.AppointmentOrigin;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentListItemDto(
        Long id,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentStatus status,
        AppointmentOrigin origin,
        Long patientId,
        String patientName
) {
}
