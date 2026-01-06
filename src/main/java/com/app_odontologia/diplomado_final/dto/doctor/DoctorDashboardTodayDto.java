package com.app_odontologia.diplomado_final.dto.doctor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class DoctorDashboardTodayDto {

    private LocalDate date;

    private long total;
    private long completed;
    private long scheduled;
    private long noShow;
    private long cancelled;

    private AppointmentMiniDto currentAppointment;
    private AppointmentMiniDto nextAppointment;

    private List<AlertMiniDto> alertsToday;

    // =========================
    // SUB DTOs
    // =========================

    @Data
    @Builder
    @AllArgsConstructor
    public static class AppointmentMiniDto {
        private Long id;
        private String patientName;
        private LocalTime startTime;
        private LocalTime endTime;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class AlertMiniDto {
        private Long id;
        private String type;
        private String severity;
        private String message;
    }
}
