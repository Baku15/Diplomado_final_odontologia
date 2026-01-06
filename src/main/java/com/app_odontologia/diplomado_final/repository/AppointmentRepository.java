package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.dto.AppointmentEmailData;
import com.app_odontologia.diplomado_final.model.entity.Appointment;
import com.app_odontologia.diplomado_final.model.entity.Appointment.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // ======================================================
    // AGENDA / VALIDACIONES
    // ======================================================

    List<Appointment> findByDoctorIdAndDate(Long doctorId, LocalDate date);

    List<Appointment> findByDoctorIdAndDateOrderByStartTimeAsc(
            Long doctorId,
            LocalDate date
    );

    boolean existsByDoctorIdAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndStatusIn(
            Long doctorId,
            LocalDate date,
            LocalTime endTime,
            LocalTime startTime,
            List<AppointmentStatus> statuses
    );

    boolean existsByDoctorIdAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndStatusInAndIdNot(
            Long doctorId,
            LocalDate date,
            LocalTime endTime,
            LocalTime startTime,
            List<AppointmentStatus> statuses,
            Long excludeId
    );

    Optional<Appointment> findFirstByClinicIdAndPatientIdAndDoctorIdAndDateAndStatusOrderByStartTimeAsc(
            Long clinicId,
            Long patientId,
            Long doctorId,
            LocalDate date,
            AppointmentStatus status
    );

    List<Appointment> findAllByConsultationId(Long consultationId);

    // ======================================================
    // EMAIL REMINDERS
    // ======================================================

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
        UPDATE Appointment a
        SET a.emailReminderSentAt = :sentAt
        WHERE a.id = :appointmentId
    """)
    void markEmailSent(
            @Param("appointmentId") Long appointmentId,
            @Param("sentAt") Instant sentAt
    );

    // ======================================================
    // DASHBOARD - HOY (DOCTOR)
    // ======================================================

    long countByDoctorIdAndDate(Long doctorId, LocalDate date);

    long countByDoctorIdAndDateAndStatus(
            Long doctorId,
            LocalDate date,
            AppointmentStatus status
    );

    // ======================================================
    // DASHBOARD - HISTÓRICO
    // ======================================================

    @Query("""
        SELECT a.date, COUNT(a)
        FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.status = com.app_odontologia.diplomado_final.model.entity.Appointment.AppointmentStatus.COMPLETED
          AND a.date BETWEEN :start AND :end
        GROUP BY a.date
    """)
    List<Object[]> countCompletedByDoctorGroupedByDate(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("""
        SELECT a.date, COUNT(a)
        FROM Appointment a
        WHERE a.doctor.id = :doctorId
          AND a.status = com.app_odontologia.diplomado_final.model.entity.Appointment.AppointmentStatus.NO_SHOW
          AND a.date BETWEEN :start AND :end
        GROUP BY a.date
    """)
    List<Object[]> countNoShowByDoctorGroupedByDate(
            @Param("doctorId") Long doctorId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // ======================================================
    // DASHBOARD - FUTURO
    // ======================================================

    long countByDoctorIdAndDateBetweenAndStatus(
            Long doctorId,
            LocalDate start,
            LocalDate end,
            AppointmentStatus status
    );

    long countByDoctorIdAndDateAndStatusIn(
            Long doctorId,
            LocalDate date,
            List<AppointmentStatus> statuses
    );


    Page<Appointment> findByDoctorIdAndDateBetween(
            Long doctorId,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );

    Page<Appointment> findByDoctorIdAndStatusAndDateBetween(
            Long doctorId,
            AppointmentStatus status,
            LocalDate start,
            LocalDate end,
            Pageable pageable
    );



}
