package com.app_odontologia.diplomado_final.dto.appointment;

import com.app_odontologia.diplomado_final.model.enums.AppointmentOrigin;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateAppointmentRequest {

    private LocalDate date;
    private LocalTime startTime;
    private Integer durationMinutes;

    private String reason;

    private Boolean sendWhatsapp;
    private Boolean sendEmail;

    // ej: 1440 = 24h antes
    private Integer reminderMinutesBefore;

    private Long consultationId; // opcional
    private AppointmentOrigin origin; // CLINICAL | DIRECT
}

