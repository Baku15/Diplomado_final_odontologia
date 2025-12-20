package com.app_odontologia.diplomado_final.metrics.appointment;

import com.app_odontologia.diplomado_final.model.entity.Appointment.AppointmentStatus;
import com.app_odontologia.diplomado_final.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentMetricsServiceImpl
        implements AppointmentMetricsService {

    private final AppointmentRepository appointmentRepository;

    @Override
    public AppointmentMetricsDto getMetrics(Long clinicId) {

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

        long total =
                scheduled + completed + cancelled + noShow;

        double completionRate =
                total == 0
                        ? 0
                        : (completed * 100.0) / total;

        return new AppointmentMetricsDto(
                scheduled,
                completed,
                cancelled,
                noShow,
                Math.round(completionRate * 100.0) / 100.0
        );
    }
}
