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
import java.util.List;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import com.app_odontologia.diplomado_final.model.enums.AppointmentOrigin;

import com.app_odontologia.diplomado_final.model.enums.AppointmentOrigin;



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

    // =====================================================
    // CREAR CITA (CLINICAL / DIRECT)
    // =====================================================

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

        if (duration < 30 || duration % 15 != 0) {
            throw new IllegalStateException("Duración inválida");
        }

        LocalTime start = req.getStartTime();
        LocalTime end = start.plusMinutes(duration);

        boolean overlap = appointmentRepository
                .existsByDoctorIdAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndStatusIn(
                        doctorId,
                        req.getDate(),
                        end,
                        start,
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
                .startTime(start)
                .endTime(end)
                .durationMinutes(duration)
                .reason(req.getReason())
                .sendWhatsapp(req.getSendWhatsapp())
                .sendEmail(req.getSendEmail())
                .reminderMinutesBefore(req.getReminderMinutesBefore())
                .status(AppointmentStatus.SCHEDULED)
                .origin(req.getOrigin() != null ? req.getOrigin() : AppointmentOrigin.DIRECT)
                .build();

        // ===== CLINICAL =====
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

            // 🔗 Vincular correctamente
            appointment.setConsultation(consultation);
            appointment.setPatient(consultation.getPatient());

            // 🔄 La consulta pasa a IN_PROGRESS
            consultation.setStatus(ConsultationStatus.IN_PROGRESS);
            consultationRepository.save(consultation);

            // ✅ LA CITA QUEDA SCHEDULED (NO COMPLETED)
            appointment.setStatus(AppointmentStatus.SCHEDULED);
        }

// 🔥 UN SOLO SAVE, AL FINAL
        Appointment saved = appointmentRepository.save(appointment);

        return AppointmentMapper.toDto(saved);
    }

        // =====================================================
    // AGENDA
    // =====================================================

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> listDoctorAgenda(Long doctorId, LocalDate date) {
        return appointmentRepository
                .findByDoctorIdAndDate(doctorId, date)
                .stream()
                .map(AppointmentMapper::toDto)
                .toList();
    }

    // =====================================================
    // ACCIONES SOBRE CITA
    // =====================================================

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

        if (!accepted) {
            throw new IllegalStateException("Debe aceptar la advertencia");
        }

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment.setCancelledAt(Instant.now());
        appointment.setCancelledBy(Appointment.CancelledBy.PATIENT);
        appointment.setLateCancellation(true);

        return AppointmentMapper.toDto(appointment);
    }

    @Override
    public AppointmentDto markSpecialCase(Long appointmentId, String note) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Solo en citas programadas");
        }

        appointment.setSpecialCase(true);
        appointment.setSpecialCaseNote(note);

        return AppointmentMapper.toDto(appointment);
    }

    @Override
    public AppointmentDto completeAppointment(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Solo citas programadas");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        return AppointmentMapper.toDto(appointment);
    }

    // =====================================================
    // CREAR CITA DIRECTA (SIN PACIENTE)
    // =====================================================

    @Override
    public AppointmentDto createDirectAppointment(
            Long clinicId,
            Long doctorId,
            CreateAppointmentRequest request
    ) {

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Clínica no encontrada"));

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        int duration = request.getDurationMinutes();

        if (duration < 30 || duration % 15 != 0) {
            throw new IllegalStateException("Duración inválida");
        }

        LocalTime start = request.getStartTime();
        LocalTime end = start.plusMinutes(duration);

        boolean overlap = appointmentRepository
                .existsByDoctorIdAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndStatusIn(
                        doctorId,
                        request.getDate(),
                        end,
                        start,
                        List.of(AppointmentStatus.SCHEDULED)
                );

        if (overlap) {
            throw new IllegalStateException("Horario ya ocupado");
        }

        Appointment appointment = Appointment.builder()
                .clinic(clinic)
                .doctor(doctor)
                .patient(null)
                .consultation(null)
                .date(request.getDate())
                .startTime(start)
                .endTime(end)
                .durationMinutes(duration)
                .reason(request.getReason())
                .sendEmail(request.getSendEmail())
                .sendWhatsapp(request.getSendWhatsapp())
                .reminderMinutesBefore(request.getReminderMinutesBefore())
                .origin(AppointmentOrigin.DIRECT)
                .specialCase(true)
                .specialCaseNote("Paciente aún no registrado")
                .status(AppointmentStatus.SCHEDULED)
                .build();

        return AppointmentMapper.toDto(
                appointmentRepository.save(appointment)
        );
    }

    // =====================================================
    // EDITAR CITA (DESDE AGENDA O CLÍNICA)
    // =====================================================

    @Override
    public AppointmentDto updateAppointment(
            Long appointmentId,
            UpdateAppointmentRequest req
    ) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Solo se pueden editar citas programadas");
        }

        int duration = req.getDurationMinutes();

        if (duration < 30 || duration % 15 != 0) {
            throw new IllegalStateException("Duración inválida");
        }

        LocalTime start = req.getStartTime() != null
                ? req.getStartTime()
                : appointment.getStartTime();

        LocalTime end = start.plusMinutes(duration);

        boolean overlap = appointmentRepository
                .existsByDoctorIdAndDateAndStartTimeLessThanAndEndTimeGreaterThanAndStatusInAndIdNot(
                        appointment.getDoctor().getId(),
                        appointment.getDate(),
                        end,
                        start,
                        List.of(AppointmentStatus.SCHEDULED),
                        appointment.getId()
                );

        if (overlap) {
            throw new IllegalStateException("Horario ya ocupado");
        }

        appointment.setStartTime(start);
        appointment.setEndTime(end);
        appointment.setDurationMinutes(duration);
        appointment.setReason(req.getReason());
        appointment.setSendEmail(req.getSendEmail());
        appointment.setSendWhatsapp(req.getSendWhatsapp());
        appointment.setReminderMinutesBefore(req.getReminderMinutesBefore());

        return AppointmentMapper.toDto(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDto findByConsultationId(Long consultationId) {

        return appointmentRepository
                .findAllByConsultationId(consultationId)
                .stream()
                .findFirst()
                .map(AppointmentMapper::toDto)
                .orElse(null);
    }
    @Override
    public AppointmentDto completeDirectAppointment(Long appointmentId) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        // 🔒 Solo citas DIRECT
        if (appointment.getOrigin() != AppointmentOrigin.DIRECT) {
            throw new IllegalStateException(
                    "Solo las citas DIRECT pueden marcarse manualmente como completadas"
            );
        }

        // 🔒 Solo si está programada
        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException(
                    "Solo se pueden completar citas programadas"
            );
        }

        // ✅ Marcar como completada
        appointment.setStatus(AppointmentStatus.COMPLETED);

        return AppointmentMapper.toDto(
                appointmentRepository.save(appointment)
        );
    }


    @Override
    @Transactional
    public void completeDirectAppointment(
            Long clinicId,
            Long appointmentId,
            String dentistUsername
    ) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (!appointment.getClinic().getId().equals(clinicId)) {
            throw new IllegalArgumentException("La cita no pertenece a esta clínica");
        }

        if (appointment.getOrigin() != AppointmentOrigin.DIRECT) {
            throw new IllegalStateException("Solo las citas DIRECT pueden completarse manualmente");
        }

        if (appointment.getStatus() != Appointment.AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Solo se pueden completar citas SCHEDULED");
        }

        appointment.setStatus(Appointment.AppointmentStatus.COMPLETED);
        appointment.setCompletedAt(Instant.now());

        appointmentRepository.save(appointment);
    }


}
