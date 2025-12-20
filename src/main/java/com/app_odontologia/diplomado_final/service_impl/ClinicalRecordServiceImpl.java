package com.app_odontologia.diplomado_final.service_impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.app_odontologia.diplomado_final.dto.attachment.AttachmentDto;
import com.app_odontologia.diplomado_final.dto.attachment.AttachmentLinkRequest;
import com.app_odontologia.diplomado_final.dto.attachment.PresignedUploadRequest;
import com.app_odontologia.diplomado_final.dto.attachment.PresignedUploadResponse;
import com.app_odontologia.diplomado_final.dto.clinical.ClinicalRecordDetailDto;
import com.app_odontologia.diplomado_final.dto.clinical.ClinicalRecordUpsertRequest;
import com.app_odontologia.diplomado_final.fhir.ClinicalRecordToFhirMapper;
import com.app_odontologia.diplomado_final.mapper.AttachmentMapper;
import com.app_odontologia.diplomado_final.mapper.ClinicalRecordMapper;
import com.app_odontologia.diplomado_final.model.entity.Attachment;
import com.app_odontologia.diplomado_final.model.entity.Clinic;
import com.app_odontologia.diplomado_final.model.entity.ClinicalRecord;
import com.app_odontologia.diplomado_final.model.entity.ClinicalRecord.ClinicalRecordStatus;
import com.app_odontologia.diplomado_final.model.entity.Patient;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.model.enums.AttachmentType;
import com.app_odontologia.diplomado_final.repository.AttachmentRepository;
import com.app_odontologia.diplomado_final.repository.ClinicRepository;
import com.app_odontologia.diplomado_final.repository.ClinicalRecordRepository;
import com.app_odontologia.diplomado_final.repository.PatientRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.ClinicalRecordService;
import com.app_odontologia.diplomado_final.service.MinioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.app_odontologia.diplomado_final.mapper.ClinicalRecordMapper.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ClinicalRecordServiceImpl implements ClinicalRecordService {

    private final ClinicalRecordRepository clinicalRecordRepository;
    private final PatientRepository patientRepository;
    private final ClinicRepository clinicRepository;
    private final UserRepository userRepository;
    private final ClinicalRecordToFhirMapper clinicalRecordToFhirMapper;

    // attachments
    private final AttachmentRepository attachmentRepository;
    private final MinioService minioService;

    // ---------------- existing clinical-record methods ----------------

    @Override
    @Transactional(readOnly = true)
    public ClinicalRecordDetailDto getByPatient(Long clinicId, Long patientId) {
        ClinicalRecord cr = clinicalRecordRepository
                .findByClinicIdAndPatientIdAndStatus(clinicId, patientId, ClinicalRecordStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("El paciente no tiene historia clínica activa."));

        return ClinicalRecordMapper.toDetailDto(cr);
    }

    @Override
    public ClinicalRecordDetailDto createForPatient(Long clinicId,
                                                    Long patientId,
                                                    ClinicalRecordUpsertRequest request,
                                                    String dentistUsername) {

        // evitar duplicados
        clinicalRecordRepository
                .findByClinicIdAndPatientIdAndStatus(clinicId, patientId, ClinicalRecordStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new IllegalStateException("El paciente ya tiene una historia clínica activa.");
                });

        Patient patient = patientRepository.findByIdAndClinicId(patientId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para la clínica dada."));

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Clínica no encontrada."));

        User dentist = null;
        if (dentistUsername != null) {
            dentist = userRepository.findByUsername(dentistUsername)
                    .orElse(null); // si no lo encuentra, dejamos null
        }

        ClinicalRecord cr = new ClinicalRecord();
        cr.setPatient(patient);
        cr.setClinic(clinic);
        cr.setDentist(dentist);
        cr.setStatus(ClinicalRecordStatus.ACTIVE);

        LocalDate now = LocalDate.now();
        cr.setOpeningDate(request.getOpeningDate() != null ? request.getOpeningDate() : now);
        cr.setFirstVisitDate(request.getFirstVisitDate() != null ? request.getFirstVisitDate() : now);

        applyUpsert(cr, request);

        ClinicalRecord saved = clinicalRecordRepository.save(cr);
        return ClinicalRecordMapper.toDetailDto(saved);
    }

    @Override
    public ClinicalRecordDetailDto updateForPatient(Long clinicId,
                                                    Long patientId,
                                                    ClinicalRecordUpsertRequest request,
                                                    String dentistUsername) {

        ClinicalRecord cr = clinicalRecordRepository
                .findByClinicIdAndPatientIdAndStatus(clinicId, patientId, ClinicalRecordStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("El paciente no tiene historia clínica activa."));

        // opcional: actualizar dentist si está vacío
        if (cr.getDentist() == null && dentistUsername != null) {
            User dentist = userRepository.findByUsername(dentistUsername).orElse(null);
            cr.setDentist(dentist);
        }

        if (request.getOpeningDate() != null) {
            cr.setOpeningDate(request.getOpeningDate());
        }
        if (request.getFirstVisitDate() != null) {
            cr.setFirstVisitDate(request.getFirstVisitDate());
        }

        applyUpsert(cr, request);

        ClinicalRecord saved = clinicalRecordRepository.save(cr);
        return ClinicalRecordMapper.toDetailDto(saved);
    }

    // ===== helper para aplicar request sobre entidad =====

    private void applyUpsert(ClinicalRecord cr, ClinicalRecordUpsertRequest req) {
        cr.setChiefComplaint(req.getChiefComplaint());
        cr.setCurrentIllness(req.getCurrentIllness());

        cr.setMedicalHistory(toMedicalHistoryEmbeddable(req.getMedicalHistory()));
        cr.setDentalHistory(toDentalHistoryEmbeddable(req.getDentalHistory()));
        cr.setVitalSigns(toVitalSignsEmbeddable(req.getVitalSigns()));
        cr.setExtraoralExam(toExtraoralExamEmbeddable(req.getExtraoralExam()));
        cr.setIntraoralExam(toIntraoralExamEmbeddable(req.getIntraoralExam()));

        cr.setInitialDiagnosticSummary(req.getInitialDiagnosticSummary());
        cr.setInitialTreatmentPlanSummary(req.getInitialTreatmentPlanSummary());
        cr.setInitialPrognosis(req.getInitialPrognosis());

        // ===== Campos planos =====
        cr.setAllergies(req.getAllergies());
        cr.setMedications(req.getMedications());
        cr.setSystemicConditions(req.getSystemicConditions());
        cr.setPregnancyStatus(req.getPregnancyStatus());
        cr.setRiskBehaviors(req.getRiskBehaviors());

        // ✅ LO QUE FALTABA
        cr.setPeriodontalStatus(req.getPeriodontalStatus());
        cr.setCariesRisk(req.getCariesRisk());
    }


    @Override
    @Transactional(readOnly = true)
    public String exportFhirJson(Long clinicId, Long patientId) {
        ClinicalRecord cr = clinicalRecordRepository
                .findByClinicIdAndPatientIdAndStatus(clinicId, patientId, ClinicalRecordStatus.ACTIVE)
                .orElseThrow(() -> new IllegalStateException("El paciente no tiene historia clínica activa."));

        return clinicalRecordToFhirMapper.toFhirBundleJson(cr);
    }


    @Override
    @Transactional
    public ClinicalRecordDetailDto closeClinicalRecord(Long id) {
        ClinicalRecord cr = clinicalRecordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ClinicalRecord not found with id: " + id));

        if (cr.getStatus() == ClinicalRecord.ClinicalRecordStatus.CLOSED) {
            // ya está cerrado — devolvemos el DTO actual
            return ClinicalRecordMapper.toDetailDto(cr);
        }

        // cambiar estado a CLOSED
        cr.setStatus(ClinicalRecord.ClinicalRecordStatus.CLOSED);
        clinicalRecordRepository.save(cr);

        return ClinicalRecordMapper.toDetailDto(cr);
    }

    // ---------------- attachments helpers ----------------

    @Override
    @Transactional(readOnly = true)
    public PresignedUploadResponse generatePresignedUploadUrl(Long clinicId, Long patientId, PresignedUploadRequest req, String username) throws Exception {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Clínica no encontrada."));
        Patient patient = patientRepository.findByIdAndClinicId(patientId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para la clínica dada."));

        String original = req.getFilename() == null ? "file" : req.getFilename();
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String baseFolder = String.format("clinic-%d/patients/%d/clinical-records/%s",
                clinicId, patientId,
                req.getClinicalRecordId() != null ? String.valueOf(req.getClinicalRecordId()) : "unlinked");

        String toothPart = (req.getToothReference() != null && !req.getToothReference().isBlank())
                ? "/tooth-" + req.getToothReference()
                : "";

        String uuid = UUID.randomUUID().toString();
        String storageKey = String.format("%s%s/%s%s", baseFolder, toothPart, uuid, ext);

        String bucket = "clinical-records";
        int ttl = 900; // 15 minutos

        String uploadUrl = minioService.generatePresignedPut(bucket, storageKey, ttl);

        return PresignedUploadResponse.builder()
                .uploadUrl(uploadUrl)
                .storageKey(storageKey)
                .expiresIn(ttl)
                .build();
    }

    @Override
    public AttachmentDto linkAttachment(Long clinicId, Long patientId, AttachmentLinkRequest req, String username) {
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Clínica no encontrada."));
        Patient patient = patientRepository.findByIdAndClinicId(patientId, clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado para la clínica dada."));

        if (req.getStorageKey() == null || req.getStorageKey().isBlank()) {
            throw new IllegalArgumentException("storageKey es requerido.");
        }

        Attachment att = new Attachment();
        att.setClinicId(clinicId);
        att.setPatientId(patientId);
        att.setClinicalRecordId(req.getClinicalRecordId());
        att.setToothReference(req.getToothReference());
        att.setFilename(req.getFilename() != null ? req.getFilename() : req.getStorageKey());
        att.setStorageKey(req.getStorageKey());
        att.setContentType(req.getContentType());
        att.setSizeBytes(req.getSizeBytes());
        att.setNotes(req.getNotes());

        if (req.getType() != null) {
            try {
                att.setType(AttachmentType.valueOf(req.getType()));
            } catch (Exception ignored) {
                // si el tipo no coincide, lo dejamos nulo
            }
        }

        if (username != null) {
            userRepository.findByUsername(username).ifPresent(u -> att.setUploaderId(u.getId()));
        }

        Attachment saved = attachmentRepository.save(att);
        return AttachmentMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttachmentDto> listAttachments(Long clinicId, Long patientId, Pageable pageable) {
        Page<Attachment> page = attachmentRepository.findByClinicIdAndPatientId(clinicId, patientId, pageable);
        List<AttachmentDto> dtos = page.getContent().stream()
                .map(AttachmentMapper::toDto)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Override
    public void deleteAttachment(Long clinicId, Long patientId, Long attachmentId, String username) throws Exception {
        Attachment att = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new IllegalArgumentException("Attachment no encontrado."));
        if (!att.getClinicId().equals(clinicId) || (att.getPatientId() != null && !att.getPatientId().equals(patientId))) {
            throw new IllegalArgumentException("Attachment no pertenece a clinic/patient especificados.");
        }

        String bucket = "clinical-records";
        try {
            minioService.deleteObject(bucket, att.getStorageKey());
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo eliminar el archivo del storage: " + ex.getMessage(), ex);
        }

        attachmentRepository.delete(att);
    }
}
