package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.odontogram.*;
import com.app_odontologia.diplomado_final.mapper.DentalChartMapper;
import com.app_odontologia.diplomado_final.model.entity.*;
import com.app_odontologia.diplomado_final.repository.*;
import com.app_odontologia.diplomado_final.service.ClinicalConsultationService;
import com.app_odontologia.diplomado_final.service.DentalChartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DentalChartServiceImpl implements DentalChartService {

    private final DentalChartRepository dentalChartRepository;
    private final ToothRepository toothRepository;
    private final DentalProcedureRepository dentalProcedureRepository;
    private final PatientRepository patientRepository;
    private final ClinicRepository clinicRepository;
    private final ClinicalRecordRepository clinicalRecordRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository; // NUEVO
    private final ToothAttachmentRepository toothAttachmentRepository;
    private final ClinicalConsultationService clinicalConsultationService;




    @Override
    @Transactional(readOnly = true)
    public DentalChartDto getActiveChart(Long clinicId, Long patientId) {
        DentalChart chart = dentalChartRepository
                .findByClinicIdAndPatientIdAndStatus(clinicId, patientId, DentalChart.ChartStatus.ACTIVE)
                .orElse(null);

        return DentalChartMapper.toDto(chart);
    }

    @Override
    public DentalChartDto createChart(Long clinicId, Long patientId, Long clinicalRecordId, String username) {

        // 🔥 1) Si ya existe un odontograma activo → devolver ese, NO lanzar errores
        var existing = dentalChartRepository
                .findByClinicIdAndPatientIdAndStatus(clinicId, patientId, DentalChart.ChartStatus.ACTIVE);

        if (existing.isPresent()) {
            return DentalChartMapper.toDto(existing.get());
        }

        // 🔥 2) Crear uno nuevo si realmente no existe
        Patient patient = patientRepository.findByIdAndClinicId(patientId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para la clínica dada."));

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Clínica no encontrada."));

        ClinicalRecord cr = null;
        if (clinicalRecordId != null) {
            cr = clinicalRecordRepository.findById(clinicalRecordId).orElse(null);
        }

        DentalChart chart = new DentalChart();
        chart.setClinic(clinic);
        chart.setPatient(patient);
        chart.setClinicalRecord(cr);
        chart.setVersion(1);
        chart.setStatus(DentalChart.ChartStatus.ACTIVE);

        DentalChart saved = dentalChartRepository.save(chart);
        return DentalChartMapper.toDto(saved);
    }

    @Override
    public DentalChartDto upsertTooth(Long chartId, UpsertToothRequest req, String username) {
        DentalChart chart = dentalChartRepository.findById(chartId)
                .orElseThrow(() -> new IllegalArgumentException("Odontograma no encontrado."));

        Integer toothNumber = req.getToothNumber();
        if (toothNumber == null) {
            throw new IllegalArgumentException("toothNumber es requerido.");
        }

        Tooth tooth = toothRepository.findByChartIdAndToothNumber(chartId, toothNumber)
                .orElseGet(() -> {
                    Tooth t = new Tooth();
                    t.setChart(chart);
                    t.setToothNumber(toothNumber);
                    return t;
                });

        if (req.getToothStatus() != null) {
            tooth.setToothStatus(Tooth.ToothStatus.valueOf(req.getToothStatus()));
        }

        if (req.getNotes() != null) {
            tooth.setNotes(req.getNotes());
        }

        if (req.getSurfaceStates() != null) {
            tooth.getSurfaceStates().putAll(req.getSurfaceStates());
        }

        Tooth saved = toothRepository.save(tooth);

        if (!chart.getTeeth().contains(saved)) {
            chart.getTeeth().add(saved);
        }

        return DentalChartMapper.toDto(chart);
    }


    @Override
    public DentalProcedureDto addProcedure(Long chartId, AddProcedureRequest req) {

        DentalChart chart = dentalChartRepository.findById(chartId)
                .orElseThrow(() -> new IllegalArgumentException("Odontograma no encontrado"));

        var consultation = clinicalConsultationService
                .getActiveConsultation(chart.getClinic().getId(), chart.getPatient().getId());

        DentalProcedure p = new DentalProcedure();
        p.setChart(chart);
        p.setToothNumber(req.getToothNumber());
        p.setSurface(req.getSurface());
        p.setType(req.getType());
        p.setDescription(req.getDescription());
        p.setPerformedBy(req.getPerformedBy());
        p.setPerformedAt(req.getPerformedAt() != null ? req.getPerformedAt() : Instant.now());

        p.setProcedureCode(req.getProcedureCode());
        p.setEstimatedDurationMinutes(req.getEstimatedDurationMinutes());
        p.setEstimatedCostCents(req.getEstimatedCostCents());

        //  AQUÍ ESTÁ EL CAMBIO CLAVE
        p.setCreatedInConsultationId(
                consultation != null ? consultation.getId() : null
        );

        if (req.getAttachmentId() != null) {
            Attachment att = attachmentRepository
                    .findByIdAndClinicId(req.getAttachmentId(), chart.getClinic().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Attachment inválido"));
            p.setAttachment(att);
        }

        DentalProcedure saved = dentalProcedureRepository.save(p);
        chart.getProcedures().add(saved);

        return DentalChartMapper.toProcedureDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentalChartDto> getChartHistory(Long patientId) {
        return dentalChartRepository.findByPatientIdOrderByVersionDesc(patientId)
                .stream()
                .map(DentalChartMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public DentalChartDto closeChart(Long chartId) {
        DentalChart chart = dentalChartRepository.findById(chartId)
                .orElseThrow(() -> new IllegalArgumentException("Odontograma no encontrado."));

        if (chart.getStatus() == DentalChart.ChartStatus.CLOSED) {
            return DentalChartMapper.toDto(chart);
        }

        chart.setStatus(DentalChart.ChartStatus.CLOSED);
        chart.setArchivedAt(Instant.now());

        dentalChartRepository.save(chart);
        return DentalChartMapper.toDto(chart);
    }

    @Override
    public DentalProcedureDto completeProcedure(Long chartId, Long procedureId, String username) {

        DentalProcedure p = dentalProcedureRepository.findById(procedureId)
                .orElseThrow(() -> new IllegalArgumentException("Procedimiento no encontrado"));

        var consultation = clinicalConsultationService
                .getActiveConsultation(
                        p.getChart().getClinic().getId(),
                        p.getChart().getPatient().getId()
                );

        if (p.getStatus() == DentalProcedure.ProcedureStatus.COMPLETED) {
            return DentalChartMapper.toProcedureDto(p);
        }

        p.setStatus(DentalProcedure.ProcedureStatus.COMPLETED);
        p.setCompletedAt(Instant.now());
        p.setPerformedBy(username);

        // 🔴 CAMBIO CLAVE
        p.setCompletedInConsultationId(
                consultation != null ? consultation.getId() : null
        );

        return DentalChartMapper.toProcedureDto(
                dentalProcedureRepository.save(p)
        );
    }

    @Override
    public DentalProcedureDto updateProcedure(Long procedureId, AddProcedureRequest req, String username) {
        DentalProcedure p = dentalProcedureRepository.findById(procedureId)
                .orElseThrow(() -> new IllegalArgumentException("Procedimiento no encontrado."));

        // regla: no editar si ya está COMPLETED (cambia si quieres permitirlo)
        if (p.getStatus() == DentalProcedure.ProcedureStatus.COMPLETED) {
            throw new IllegalStateException("No se puede editar un procedimiento ya finalizado.");
        }

        // actualizar campos permitidos
        if (req.getToothNumber() != null) p.setToothNumber(req.getToothNumber());
        if (req.getSurface() != null) p.setSurface(req.getSurface());
        if (req.getType() != null) p.setType(req.getType());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getPerformedBy() != null) p.setPerformedBy(req.getPerformedBy());
        if (req.getPerformedAt() != null) {
            p.setPerformedAt(req.getPerformedAt());
            // si performedAt se actualiza y status es null/pending, puedes decidir marcar completed
        }

        if (req.getProcedureCode() != null) p.setProcedureCode(req.getProcedureCode());
        if (req.getEstimatedDurationMinutes() != null) p.setEstimatedDurationMinutes(req.getEstimatedDurationMinutes());
        if (req.getEstimatedCostCents() != null) p.setEstimatedCostCents(req.getEstimatedCostCents());

        // attachment (permitir set/update/nullify)
        if (req.getAttachmentId() != null) {
            // set new attachment after validation
            Long attId = req.getAttachmentId();
            Attachment att = attachmentRepository.findByIdAndClinicId(attId, p.getChart().getClinic().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Attachment no encontrado o no pertenece a la clínica."));
            p.setAttachment(att);
        }

        DentalProcedure saved = dentalProcedureRepository.save(p);
        return DentalChartMapper.toProcedureDto(saved);
    }

    @Override
    public ToothAttachmentDto addToothAttachment(Long chartId, Integer toothNumber, Long attachmentId, String username) {
        DentalChart chart = dentalChartRepository.findById(chartId)
                .orElseThrow(() -> new IllegalArgumentException("Odontograma no encontrado."));

        Tooth tooth = toothRepository.findByChartIdAndToothNumber(chartId, toothNumber)
                .orElseThrow(() -> new IllegalArgumentException("Diente no encontrado en odontograma."));

        // validar attachment y que pertenezca a la misma clínica
        Attachment att = attachmentRepository.findByIdAndClinicId(attachmentId, chart.getClinic().getId())
                .orElseThrow(() -> new IllegalArgumentException("Attachment no encontrado o no pertenece a la clínica."));

        // evitar duplicados
        if (toothAttachmentRepository.findByToothIdAndAttachmentId(tooth.getId(), att.getId()).isPresent()) {
            // ya existe: devolver DTO existente
            ToothAttachment existing = toothAttachmentRepository.findByToothIdAndAttachmentId(tooth.getId(), att.getId()).get();
            return ToothAttachmentDto.builder()
                    .id(existing.getId())
                    .attachmentId(att.getId())
                    .filename(att.getFilename())
                    .storageKey(att.getStorageKey())
                    .contentType(att.getContentType())
                    .sizeBytes(att.getSizeBytes())
                    .notes(att.getNotes())
                    .createdAt(existing.getCreatedAt())
                    .build();
        }

        ToothAttachment ta = new ToothAttachment();
        ta.setTooth(tooth);
        ta.setAttachment(att);

        ToothAttachment saved = toothAttachmentRepository.save(ta);

        // actualizar coleccion en Tooth (si necesario)
        tooth.getAttachments().add(saved);
        toothRepository.save(tooth);

        return ToothAttachmentDto.builder()
                .id(saved.getId())
                .attachmentId(att.getId())
                .filename(att.getFilename())
                .storageKey(att.getStorageKey())
                .contentType(att.getContentType())
                .sizeBytes(att.getSizeBytes())
                .notes(att.getNotes())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    @Override
    public void removeToothAttachment(Long chartId, Integer toothNumber, Long attachmentId, String username) {
        DentalChart chart = dentalChartRepository.findById(chartId)
                .orElseThrow(() -> new IllegalArgumentException("Odontograma no encontrado."));

        Tooth tooth = toothRepository.findByChartIdAndToothNumber(chartId, toothNumber)
                .orElseThrow(() -> new IllegalArgumentException("Diente no encontrado en odontograma."));

        ToothAttachment ta = toothAttachmentRepository.findByToothIdAndAttachmentId(tooth.getId(), attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment no asociado a ese diente."));

        tooth.getAttachments().removeIf(x -> x.getId().equals(ta.getId()));
        toothRepository.save(tooth);

        toothAttachmentRepository.delete(ta);
    }

    @Override
    public List<ToothAttachmentDto> listToothAttachments(Long chartId, Integer toothNumber) {
        DentalChart chart = dentalChartRepository.findById(chartId)
                .orElseThrow(() -> new IllegalArgumentException("Odontograma no encontrado."));

        Tooth tooth = toothRepository.findByChartIdAndToothNumber(chartId, toothNumber)
                .orElseThrow(() -> new IllegalArgumentException("Diente no encontrado."));

        return tooth.getAttachments().stream()
                .map(ta -> ToothAttachmentDto.builder()
                        .id(ta.getId())
                        .attachmentId(ta.getAttachment().getId())
                        .filename(ta.getAttachment().getFilename())
                        .storageKey(ta.getAttachment().getStorageKey())
                        .contentType(ta.getAttachment().getContentType())
                        .sizeBytes(ta.getAttachment().getSizeBytes())
                        .notes(ta.getAttachment().getNotes())
                        .createdAt(ta.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DentalProcedureDto> listProceduresByConsultation(Long consultationId) {
        return dentalProcedureRepository
                .findByCreatedInConsultationId(consultationId)
                .stream()
                .map(DentalChartMapper::toProcedureDto)
                .collect(Collectors.toList());
    }



}
