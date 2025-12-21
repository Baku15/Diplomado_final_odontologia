package com.app_odontologia.diplomado_final.dto.appointment;

import lombok.Data;

import java.time.LocalTime;

@Data
public class UpdateAppointmentRequest {

    private LocalTime startTime;

    /**
     * Duración en minutos.
     * Reglas:
     * - mínimo 30
     * - múltiplos de 15 (30, 45, 60, 90, 120…)
     */
    private Integer durationMinutes;

    private String reason;

    private Boolean sendWhatsapp;
    private Boolean sendEmail;

    private Integer reminderMinutesBefore;
}
