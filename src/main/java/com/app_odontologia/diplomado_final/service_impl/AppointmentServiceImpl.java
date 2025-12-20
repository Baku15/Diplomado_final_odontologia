package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.appointment.*;
import com.app_odontologia.diplomado_final.mapper.AppointmentMapper;
import com.app_odontologia.diplomado_final.model.entity.*;
import com.app_odontologia.diplomado_final.model.entity.Appointment.AppointmentStatus;
import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation.ConsultationStatus;
import com.app_odontologia.diplomado_final.model.enums.AppointmentOrigin;
import com.app_odontologia.diplomado_final.repository.*;
import com.app_odontologia.diplomado_final.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClinicRepository clinicRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentAuditRepository auditRepository;
    private final SystemAlertRepository systemAlertRepository;
    private final ClinicalConsultationRepository consultationRepository;

    @Override
    public AppointmentDto createAppointment(
            Long clinicId,
            Long patientId,
            Long doctorId,
            CreateAppointmentRequest req
    ) {

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Clínica no encontrada"));

        Patient patient = patientRepository.findByIdAndClinicId(patientId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        int duration = req.getDurationMinutes() != null
                ? req.getDurationMinutes()
                : clinic.getDefaultAppointmentDurationMinutes();

        LocalTime endTime = req.getStartTime().plusMinutes(duration);

        boolean overlap = appointmentRepository
                .existsByDoctorIdAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndStatusIn(
                        doctorId,
                        req.getDate(),
                        endTime,
                        req.getStartTime(),
                        List.of(AppointmentStatus.SCHEDULED)
                );

        if (overlap) {
            throw new IllegalStateException("El horario seleccionado no está disponible");
        }

        Appointment appointment = Appointment.builder()
                .clinic(clinic)
                .patient(patient)
                .doctor(doctor)
                .date(req.getDate())
                .startTime(req.getStartTime())
                .endTime(endTime)
                .durationMinutes(duration)
                .reason(req.getReason())
                .sendWhatsapp(req.getSendWhatsapp())
                .sendEmail(req.getSendEmail())
                .reminderMinutesBefore(req.getReminderMinutesBefore())
                .status(AppointmentStatus.SCHEDULED)
                .origin(
                        req.getOrigin() != null
                                ? req.getOrigin()
                                : AppointmentOrigin.DIRECT
                )

                .build();

        Appointment saved = appointmentRepository.save(appointment);

        // =====================================================
        // 🔥 FLUJO CLÍNICO CORRECTO
        // =====================================================
        // La consulta pasa a IN_PROGRESS SOLO al agendar la cita
        if (req.getOrigin() == AppointmentOrigin.CLINICAL) {

            if (req.getConsultationId() == null) {
                throw new IllegalStateException("Una cita clínica requiere consultationId");
            }

            ClinicalConsultation consultation =
                    consultationRepository.findById(req.getConsultationId())
                            .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));

            if (consultation.getStatus() != ConsultationStatus.ACTIVE) {
                throw new IllegalStateException("La consulta no está ACTIVE");
            }

            // 🔥 CONSOLIDAR CONSULTA AQUÍ
            consultation.setStatus(ConsultationStatus.IN_PROGRESS);
            consultation.setSummary(consultation.getSummary());
            consultation.setClinicalNotes(consultation.getClinicalNotes());

            consultationRepository.save(consultation);
            saved.setConsultation(consultation);
        }



        return AppointmentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> listDoctorAgenda(Long doctorId, LocalDate date) {
        return appointmentRepository
                .findByDoctorIdAndDate(doctorId, date)
                .stream()
                .map(AppointmentMapper::toDto)
                .toList();
    }

    // =========================
    // 🔥 LÓGICA DE CITAS
    // =========================

    @Override
    public AppointmentDto cancelAppointment(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Solo se pueden cancelar citas programadas");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);

        return AppointmentMapper.toDto(appointment);
    }

    @Override
    public AppointmentDto markNoShow(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Solo se puede marcar NO_SHOW en citas programadas");
        }

        appointment.setStatus(AppointmentStatus.NO_SHOW);

        return AppointmentMapper.toDto(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDto findByConsultationId(Long consultationId) {
        return appointmentRepository
                .findByConsultationId(consultationId)
                .map(AppointmentMapper::toDto)
                .orElse(null);
    }

    @Override
    public AppointmentDto confirmAttendance(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        appointment.setAttendanceConfirmed(true);
        appointment.setAttendanceConfirmedAt(Instant.now());

        auditRepository.save(
                AppointmentAudit.builder()
                        .appointment(appointment)
                        .patient(appointment.getPatient())
                        .eventType(AppointmentAudit.EventType.CONFIRMED)
                        .build()
        );

        return AppointmentMapper.toDto(appointment);
    }

    @Override
    public AppointmentDto cancelLate(Long appointmentId, boolean accepted) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (!accepted) {
            throw new IllegalStateException("Debe aceptar la advertencia");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(Instant.now());
        appointment.setCancelledBy(Appointment.CancelledBy.PATIENT);
        appointment.setLateCancellation(true);

        Patient patient = appointment.getPatient();
        patient.setTotalCancellations(patient.getTotalCancellations() + 1);

        auditRepository.save(
                AppointmentAudit.builder()
                        .appointment(appointment)
                        .patient(patient)
                        .eventType(AppointmentAudit.EventType.CANCELLED_LATE)
                        .build()
        );

        return AppointmentMapper.toDto(appointment);
    }

    @Override
    public AppointmentDto markSpecialCase(Long appointmentId, String note) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Solo se puede marcar caso especial en citas programadas");
        }

        appointment.setSpecialCase(true);
        appointment.setSpecialCaseNote(note);

        auditRepository.save(
                AppointmentAudit.builder()
                        .appointment(appointment)
                        .patient(appointment.getPatient())
                        .eventType(AppointmentAudit.EventType.SPECIAL_CASE)
                        .note(note)
                        .build()
        );

        return AppointmentMapper.toDto(appointment);
    }

    @Override
    public AppointmentDto completeAppointment(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Solo se pueden completar citas programadas");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);

        Patient patient = appointment.getPatient();
        patient.setConsecutiveNoShows(0);

        auditRepository.save(
                AppointmentAudit.builder()
                        .appointment(appointment)
                        .patient(patient)
                        .eventType(AppointmentAudit.EventType.COMPLETED)
                        .build()
        );

        return AppointmentMapper.toDto(appointment);
    }
}
