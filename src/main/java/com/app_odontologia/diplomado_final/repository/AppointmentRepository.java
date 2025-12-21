package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.Appointment;
import com.app_odontologia.diplomado_final.model.entity.Appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorIdAndDate(Long doctorId, LocalDate date);


    boolean existsByDoctorIdAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndStatusIn(
            Long doctorId,
            LocalDate date,
            LocalTime endTime,
            LocalTime startTime,
            List<AppointmentStatus> statuses
    );

    // Buscar cita activa del día (inicio de consulta)
    Optional<Appointment> findFirstByClinicIdAndPatientIdAndDoctorIdAndDateAndStatusOrderByStartTimeAsc(
            Long clinicId,
            Long patientId,
            Long doctorId,
            LocalDate date,
            AppointmentStatus status
    );

    // 🔥 NUEVO (limpio): buscar cita asociada a una consulta
    List<Appointment> findAllByConsultationId(Long consultationId);

    long countByClinicId(Long clinicId);

    long countByClinicIdAndStatus(
            Long clinicId,
            Appointment.AppointmentStatus status
    );

    boolean existsByDoctorIdAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndStatusInAndIdNot(
            Long doctorId,
            LocalDate date,
            LocalTime endTime,
            LocalTime startTime,
            List<AppointmentStatus> statuses,
            Long excludeId
    );

}
