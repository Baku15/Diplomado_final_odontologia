package com.app_odontologia.diplomado_final.metrics.dashboard;

import lombok.Data;

@Data
public class DashboardMetricsDto {

    // 🧍‍♂️ Pacientes
    private long totalPatients;
    private long activePatients;

    // 🦷 Odontograma
    private long activeCharts;
    private long closedCharts;
    private long totalProcedures;

    // 🩺 Consultas
    private long totalConsultations;
    private long activeConsultations;
    private long closedConsultations;
    private double averageConsultationDurationMinutes;

    // 📅 Citas
    private long scheduledAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private long noShowAppointments;
    private double appointmentCompletionRate;
}
