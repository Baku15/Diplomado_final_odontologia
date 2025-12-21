package com.app_odontologia.diplomado_final.dto.appointment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class AppointmentDto {

    private Long id;

    private Long clinicId;
    private Long patientId;
    private Long doctorId;
    private String doctorName;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer durationMinutes;

    private Long consultationId;
    private String origin;
    private Boolean specialCase;


    private String status;
    private String reason;

    private Boolean sendWhatsapp;
    private Boolean sendEmail;
    private Integer reminderMinutesBefore;
}
