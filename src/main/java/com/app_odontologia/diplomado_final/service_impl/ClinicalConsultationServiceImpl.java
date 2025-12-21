package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.consultation.*;
import com.app_odontologia.diplomado_final.dto.odontogram.DentalProcedureDto;
import com.app_odontologia.diplomado_final.mapper.ClinicalConsultationMapper;
import com.app_odontologia.diplomado_final.mapper.DentalChartMapper;
import com.app_odontologia.diplomado_final.model.entity.*;
import com.app_odontologia.diplomado_final.model.entity.Appointment.AppointmentStatus;
import com.app_odontologia.diplomado_final.model.entity.ClinicalConsultation.ConsultationStatus;
import com.app_odontologia.diplomado_final.repository.*;
import com.app_odontologia.diplomado_final.service.ClinicalConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClinicalConsultationServiceImpl implements ClinicalConsultationService {

    private final ClinicalConsultationRepository consultationRepository;
    private final ClinicRepository clinicRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final DentalChartRepository dentalChartRepository;
    private final AppointmentRepository appointmentRepository;
    private final DentalProcedureRepository dentalProcedureRepository;

    // ============================
    // CONSULTA ACTIVA
    // ============================

    @Override
    @Transactional(readOnly = true)
    public ClinicalConsultationDto getActiveConsultation(Long clinicId, Long patientId) {
        return consultationRepository
                .findOpenConsultation(clinicId, patientId)
                .map(ClinicalConsultationMapper::toDto)
                .orElse(null);
    }

    // ============================
    // INICIAR CONSULTA (ACTIVE)
    // ============================

    @Override
    public ClinicalConsultationDto enterOdontogram(
            Long clinicId,
            Long patientId,
            String dentistUsername
    ) {

        ClinicalConsultation consultation =
                consultationRepository.findOpenConsultation(clinicId, patientId)
                        .orElse(null);

        if (consultation != null) {

            if (consultation.getStatus() == ConsultationStatus.IN_PROGRESS) {
                consultation.setStatus(ConsultationStatus.ACTIVE);
                return ClinicalConsultationMapper.toDto(
                        consultationRepository.save(consultation)
                );
            }

            return ClinicalConsultationMapper.toDto(consultation);
        }

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Clínica no encontrada"));

        Patient patient = patientRepository.findByIdAndClinicId(patientId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        User dentist = userRepository.findByUsername(dentistUsername)
                .orElseThrow(() -> new IllegalArgumentException("Odontólogo no encontrado"));

        DentalChart chart = dentalChartRepository
                .findByClinicIdAndPatientIdAndStatus(
                        clinicId,
                        patientId,
                        DentalChart.ChartStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new IllegalStateException("El paciente no tiene odontograma activo")
                );

        ClinicalConsultation newConsultation = ClinicalConsultation.builder()
                .clinic(clinic)
                .patient(patient)
                .dentist(dentist)
                .dentalChart(chart)
                .status(ConsultationStatus.ACTIVE)
                .startedAt(Instant.now())
                .build();

        return ClinicalConsultationMapper.toDto(
                consultationRepository.save(newConsultation)
        );
    }

    // ============================
    // CERRAR / CONTINUAR CONSULTA
    // ============================

    @Override
    public ClinicalConsultationDto closeConsultation(
            Long consultationId,
            CloseConsultationRequest request,
            String dentistUsername
    ) {

        ClinicalConsultation c = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));

        if (c.getStatus() == ConsultationStatus.CLOSED) {
            return ClinicalConsultationMapper.toDto(c);
        }

        boolean requireNext =
                Boolean.TRUE.equals(request.getRequireNextAppointment());

        // =====================================================
        // CASO A: TRATAMIENTO FINALIZADO (CLOSED)
        // =====================================================
        if (!requireNext) {

            c.setClinicalNotes(request.getClinicalNotes());
            c.setSummary(request.getSummary());
            c.setEndedAt(Instant.now());
            c.setStatus(ConsultationStatus.CLOSED);

            // 🔥 CORRECCIÓN CLAVE:
            // una consulta puede tener MÚLTIPLES citas
            List<Appointment> appointments =
                    appointmentRepository.findAllByConsultationId(c.getId());

            appointments.stream()
                    .filter(a -> a.getStatus() == AppointmentStatus.SCHEDULED)
                    .max(Comparator.comparing(Appointment::getDate)
                            .thenComparing(Appointment::getStartTime))
                    .ifPresent(a -> {
                        a.setStatus(AppointmentStatus.COMPLETED);
                    });

            return ClinicalConsultationMapper.toDto(
                    consultationRepository.save(c)
            );
        }

        // =====================================================
        // CASO B: CONTINÚA TRATAMIENTO
        // =====================================================
        // 🔹 NO se cierra la consulta
        // 🔹 NO se cambia el estado aquí
        // 🔹 La transición a IN_PROGRESS ocurre
        //    SOLO cuando se crea la cita

        return ClinicalConsultationMapper.toDto(
                consultationRepository.save(c)
        );
    }

    // ============================
    // LISTADOS / CONSULTAS
    // ============================

    @Override
    @Transactional(readOnly = true)
    public List<ClinicalConsultationDto> listConsultations(Long clinicId, Long patientId) {
        return consultationRepository
                .findByClinicIdAndPatientIdOrderByStartedAtDesc(clinicId, patientId)
                .stream()
                .map(ClinicalConsultationMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClinicalConsultationDto getById(
            Long clinicId,
            Long patientId,
            Long consultationId
    ) {
        ClinicalConsultation c = consultationRepository
                .findById(consultationId)
                .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));

        if (!c.getClinic().getId().equals(clinicId)
                || !c.getPatient().getId().equals(patientId)) {
            throw new IllegalArgumentException("Consulta no pertenece al paciente o clínica");
        }

        return ClinicalConsultationMapper.toDto(c);
    }

    // ============================
    // PROCEDIMIENTOS
    // ============================

    @Override
    @Transactional(readOnly = true)
    public List<DentalProcedureDto> listProceduresByConsultation(Long consultationId) {
        return dentalProcedureRepository
                .findByCreatedInConsultationId(consultationId)
                .stream()
                .map(DentalChartMapper::toProcedureDto)
                .toList();
    }

    // ============================
    // ACTIVE o IN_PROGRESS
    // ============================

    @Override
    @Transactional(readOnly = true)
    public ClinicalConsultationDto getActiveOrInProgress(Long clinicId, Long patientId) {
        return consultationRepository
                .findOpenConsultation(clinicId, patientId)
                .map(ClinicalConsultationMapper::toDto)
                .orElse(null);
    }

    @Override
    public void leaveOdontogram(
            Long consultationId,
            boolean hasClinicalChanges
    ) {

        ClinicalConsultation consultation =
                consultationRepository.findById(consultationId)
                        .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));

        if (consultation.getStatus() != ConsultationStatus.ACTIVE) {
            return;
        }

        if (hasClinicalChanges) {
            consultation.setStatus(ConsultationStatus.IN_PROGRESS);
        } else {
            consultation.setStatus(ConsultationStatus.CLOSED);
            consultation.setEndedAt(Instant.now());
        }

        consultationRepository.save(consultation);
    }

    // ============================
    // DESDE CITA (DIRECT)
    // ============================

    @Override
    public ClinicalConsultationDto startFromAppointment(
            Long appointmentId,
            String dentistUsername
    ) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Cita no encontrada"));

        if (appointment.getConsultation() != null) {
            ClinicalConsultation c = appointment.getConsultation();
            c.setStatus(ConsultationStatus.ACTIVE);
            return ClinicalConsultationMapper.toDto(
                    consultationRepository.save(c)
            );
        }

        Clinic clinic = appointment.getClinic();
        User dentist = userRepository.findByUsername(dentistUsername)
                .orElseThrow(() -> new IllegalArgumentException("Odontólogo no encontrado"));

        Patient patient = appointment.getPatient();
        if (patient == null) {
            throw new IllegalStateException("La cita no tiene paciente asociado");
        }

        DentalChart chart = dentalChartRepository
                .findByClinicIdAndPatientIdAndStatus(
                        clinic.getId(),
                        patient.getId(),
                        DentalChart.ChartStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new IllegalStateException("El paciente no tiene odontograma activo")
                );

        ClinicalConsultation consultation = ClinicalConsultation.builder()
                .clinic(clinic)
                .patient(patient)
                .dentist(dentist)
                .dentalChart(chart)
                .status(ConsultationStatus.ACTIVE)
                .startedAt(Instant.now())
                .build();

        ClinicalConsultation saved = consultationRepository.save(consultation);

        appointment.setConsultation(saved);
        appointmentRepository.save(appointment);

        return ClinicalConsultationMapper.toDto(saved);
    }
}
