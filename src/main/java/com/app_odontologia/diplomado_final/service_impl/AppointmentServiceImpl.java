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
    private final ClinicalConsultationRepository consultationRepository;

    // =====================================================
    // CREAR CITA CLÍNICA
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
        // ===============================
// VALIDACIÓN DE RECORDATORIO EMAIL
// ===============================
        if (Boolean.TRUE.equals(req.getSendEmail())) {

            if (patient.getEmail() == null || patient.getEmail().isBlank()) {
                throw new IllegalStateException(
                        "El paciente no tiene correo electrónico registrado"
                );
            }

            if (Boolean.FALSE.equals(patient.getAllowEmailReminders())) {
                throw new IllegalStateException(
                        "El paciente no permite recordatorios por correo electrónico"
                );
            }
        }


        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        int duration = req.getDurationMinutes();

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
            throw new IllegalStateException("Horario no disponible");
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
                .sendEmail(req.getSendEmail())
                .sendWhatsapp(req.getSendWhatsapp())
                .reminderMinutesBefore(req.getReminderMinutesBefore())
                .origin(AppointmentOrigin.CLINICAL)
                .status(AppointmentStatus.SCHEDULED)
                .build();

        if (req.getConsultationId() == null) {
            throw new IllegalStateException("Cita clínica requiere consultationId");
        }

        ClinicalConsultation consultation =
                consultationRepository.findById(req.getConsultationId())
                        .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));

        if (consultation.getStatus() != ConsultationStatus.ACTIVE) {
            throw new IllegalStateException("Consulta no está ACTIVE");
        }

        appointment.setConsultation(consultation);
        consultation.setStatus(ConsultationStatus.IN_PROGRESS);

        consultationRepository.save(consultation);

        return AppointmentMapper.toDto(
                appointmentRepository.save(appointment)
        );
    }

    // =====================================================
    // CREAR CITA DIRECTA
    // =====================================================

    @Override
    public AppointmentDto createDirectAppointment(
            Long clinicId,
            Long doctorId,
            CreateAppointmentRequest req
    ) {

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Clínica no encontrada"));

        User doctor = userRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor no encontrado"));

        int duration = req.getDurationMinutes();

        LocalTime start = req.getStartTime();
        LocalTime end = start.plusMinutes(duration);

        Appointment appointment = Appointment.builder()
                .clinic(clinic)
                .doctor(doctor)
                .date(req.getDate())
                .startTime(start)
                .endTime(end)
                .durationMinutes(duration)
                .reason(req.getReason())
                .origin(AppointmentOrigin.DIRECT)
                .status(AppointmentStatus.SCHEDULED)
                .specialCase(true)
                .specialCaseNote("Paciente no registrado")
                .build();

        return AppointmentMapper.toDto(
                appointmentRepository.save(appointment)
        );
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
    // COMPLETAR CITA CLÍNICA (MANUAL)
    // =====================================================

    @Override
    public AppointmentDto completeClinicalAppointment(
            Long clinicId,
            Long patientId,
            Long appointmentId
    ) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (!appointment.getClinic().getId().equals(clinicId)) {
            throw new IllegalArgumentException("Clínica incorrecta");
        }

        if (appointment.getOrigin() != AppointmentOrigin.CLINICAL) {
            throw new IllegalStateException("No es cita clínica");
        }

        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new IllegalArgumentException("Paciente incorrecto");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Solo SCHEDULED");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setCompletedAt(Instant.now());

        return AppointmentMapper.toDto(
                appointmentRepository.save(appointment)
        );
    }

    // =====================================================
    // COMPLETAR CITA DIRECTA (MANUAL)
    // =====================================================

    @Override
    public void completeDirectAppointment(
            Long clinicId,
            Long appointmentId,
            String dentistUsername
    ) {

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (!appointment.getClinic().getId().equals(clinicId)) {
            throw new IllegalArgumentException("Clínica incorrecta");
        }

        if (appointment.getOrigin() != AppointmentOrigin.DIRECT) {
            throw new IllegalStateException("No es DIRECT");
        }

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new IllegalStateException("Solo SCHEDULED");
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setCompletedAt(Instant.now());

        appointmentRepository.save(appointment);
    }

    // =====================================================
    // OTROS
    // =====================================================

    @Override
    public AppointmentDto cancelAppointment(Long appointmentId) {
        Appointment ap = appointmentRepository.findById(appointmentId)
                .orElseThrow();
        ap.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentMapper.toDto(ap);
    }

    @Override
    public AppointmentDto markNoShow(Long appointmentId) {
        Appointment ap = appointmentRepository.findById(appointmentId)
                .orElseThrow();
        ap.setStatus(AppointmentStatus.NO_SHOW);
        return AppointmentMapper.toDto(ap);
    }

    @Override
    public AppointmentDto confirmAttendance(Long appointmentId) {
        Appointment ap = appointmentRepository.findById(appointmentId)
                .orElseThrow();
        ap.setAttendanceConfirmed(true);
        return AppointmentMapper.toDto(ap);
    }

    @Override
    public AppointmentDto cancelLate(Long appointmentId, boolean accepted) {
        Appointment ap = appointmentRepository.findById(appointmentId)
                .orElseThrow();
        ap.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentMapper.toDto(ap);
    }

    @Override
    public AppointmentDto markSpecialCase(Long appointmentId, String note) {
        Appointment ap = appointmentRepository.findById(appointmentId)
                .orElseThrow();
        ap.setSpecialCase(true);
        ap.setSpecialCaseNote(note);
        return AppointmentMapper.toDto(ap);
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

        if (Boolean.TRUE.equals(req.getSendEmail())) {

            Patient patient = appointment.getPatient();

            if (patient == null ||
                    patient.getEmail() == null ||
                    patient.getEmail().isBlank()) {

                throw new IllegalStateException(
                        "No se puede enviar recordatorio: el paciente no tiene email"
                );
            }

            if (Boolean.FALSE.equals(patient.getAllowEmailReminders())) {
                throw new IllegalStateException(
                        "El paciente no permite recordatorios por correo electrónico"
                );
            }

            // 🔄 Si se vuelve a activar, limpiar flag de envío
            appointment.setEmailReminderSentAt(null);
        }


        appointment.setStartTime(start);
        appointment.setEndTime(end);
        appointment.setDurationMinutes(duration);
        appointment.setReason(req.getReason());
        appointment.setSendEmail(req.getSendEmail());
        appointment.setSendWhatsapp(req.getSendWhatsapp());
        appointment.setReminderMinutesBefore(req.getReminderMinutesBefore());

        return AppointmentMapper.toDto(
                appointmentRepository.save(appointment)
        );
    }

}
