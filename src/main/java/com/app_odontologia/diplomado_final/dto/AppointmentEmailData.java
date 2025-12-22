package com.app_odontologia.diplomado_final.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentEmailData(
        Long appointmentId,
        String patientFullName,
        String patientEmail,
        String doctorFullName,
        String clinicName,
        LocalDate date,
        LocalTime startTime
) {}