package com.app_odontologia.diplomado_final.metrics.dashboard;

import com.app_odontologia.diplomado_final.model.entity.*;
import com.app_odontologia.diplomado_final.model.entity.Appointment.AppointmentStatus;
import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation.ConsultationStatus;
import com.app_odontologia.diplomado_final.model.entity.DentalChart.ChartStatus;
import com.app_odontologia.diplomado_final.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardMetricsServiceImpl
        implements DashboardMetricsService {

    private final PatientRepository patientRepository;
    private final DentalChartRepository dentalChartRepository;
    private final DentalProcedureRepository dentalProcedureRepository;
    private final ClinicalConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;

    @Override
    public DashboardMetricsDto getDashboardMetrics(Long clinicId) {

        DashboardMetricsDto dto = new DashboardMetricsDto();

        // 🧍‍♂️ Pacientes
        dto.setTotalPatients(
                patientRepository.countByClinicId(clinicId)
        );

        dto.setActivePatients(
                consultationRepository.countDistinctPatientIdByClinicId(clinicId)
        );

        // 🦷 Odontograma
        dto.setActiveCharts(
                dentalChartRepository.countByClinicIdAndStatus(
                        clinicId,
                        ChartStatus.ACTIVE
                )
        );

        dto.setClosedCharts(
                dentalChartRepository.countByClinicIdAndStatus(
                        clinicId,
                        ChartStatus.CLOSED
                )
        );

        dto.setTotalProcedures(
                dentalProcedureRepository.count()
        );

        // 🩺 Consultas
        dto.setTotalConsultations(
                consultationRepository.countByClinicId(clinicId)
        );

        dto.setActiveConsultations(
                consultationRepository.countByClinicIdAndStatus(
                        clinicId,
                        ConsultationStatus.ACTIVE
                )
        );

        dto.setClosedConsultations(
                consultationRepository.countByClinicIdAndStatus(
                        clinicId,
                        ConsultationStatus.CLOSED
                )
        );

        Double avg =
                consultationRepository.findAverageDurationMinutes(clinicId);

        dto.setAverageConsultationDurationMinutes(
                avg != null ? avg : 0
        );

        // 📅 Citas
        long scheduled =
                appointmentRepository.countByClinicIdAndStatus(
                        clinicId,
                        AppointmentStatus.SCHEDULED
                );

        long completed =
                appointmentRepository.countByClinicIdAndStatus(
                        clinicId,
                        AppointmentStatus.COMPLETED
                );

        long cancelled =
                appointmentRepository.countByClinicIdAndStatus(
                        clinicId,
                        AppointmentStatus.CANCELLED
                );

        long noShow =
                appointmentRepository.countByClinicIdAndStatus(
                        clinicId,
                        AppointmentStatus.NO_SHOW
                );

        dto.setScheduledAppointments(scheduled);
        dto.setCompletedAppointments(completed);
        dto.setCancelledAppointments(cancelled);
        dto.setNoShowAppointments(noShow);

        long totalAppointments =
                scheduled + completed + cancelled + noShow;

        double completionRate =
                totalAppointments == 0
                        ? 0
                        : (completed * 100.0) / totalAppointments;

        dto.setAppointmentCompletionRate(
                Math.round(completionRate * 100.0) / 100.0
        );

        return dto;
    }
}
