package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.dto.AppointmentEmailData;
import com.app_odontologia.diplomado_final.model.entity.Appointment;
import com.app_odontologia.diplomado_final.model.entity.Appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
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

    @Query("""
SELECT new com.app_odontologia.diplomado_final.dto.AppointmentEmailData(
    a.id,
    CONCAT(p.givenName, ' ', p.familyName),
    p.email,
    CONCAT(u.nombres, ' ', u.apellidos),
    'odontoweb',
    a.date,
    a.startTime
)
FROM Appointment a
JOIN a.patient p
JOIN a.doctor u
WHERE a.status = com.app_odontologia.diplomado_final.model.entity.Appointment.AppointmentStatus.SCHEDULED
  AND a.sendEmail = true
  AND a.emailReminderSentAt IS NULL
  AND p.email IS NOT NULL
""")
    List<AppointmentEmailData> findPendingEmailReminders();

    @Modifying
    @Query("""
    update Appointment a
    set a.emailReminderSentAt = :sentAt
    where a.id = :appointmentId
""")
    void markEmailSent(
            @Param("appointmentId") Long appointmentId,
            @Param("sentAt") Instant sentAt
    );


}
