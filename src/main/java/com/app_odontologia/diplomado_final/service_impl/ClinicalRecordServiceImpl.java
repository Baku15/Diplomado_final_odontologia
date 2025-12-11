package com.app_odontologia.diplomado_final.service_impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.app_odontologia.diplomado_final.dto.clinical.ClinicalRecordDetailDto;
import com.app_odontologia.diplomado_final.dto.clinical.ClinicalRecordUpsertRequest;
import com.app_odontologia.diplomado_final.fhir.ClinicalRecordToFhirMapper;
import com.app_odontologia.diplomado_final.mapper.ClinicalRecordMapper;
import com.app_odontologia.diplomado_final.model.entity.Clinic;
import com.app_odontologia.diplomado_final.model.entity.ClinicalRecord;
import com.app_odontologia.diplomado_final.model.entity.ClinicalRecord.ClinicalRecordStatus;
import com.app_odontologia.diplomado_final.model.entity.Patient;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.repository.ClinicRepository;
import com.app_odontologia.diplomado_final.repository.ClinicalRecordRepository;
import com.app_odontologia.diplomado_final.repository.PatientRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.ClinicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;


import java.time.LocalDate;

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

        // 🎯 NUEVOS CAMPOS PLANOS
        cr.setAllergies(req.getAllergies());
        cr.setMedications(req.getMedications());
        cr.setSystemicConditions(req.getSystemicConditions());
        cr.setPregnancyStatus(req.getPregnancyStatus());
        cr.setRiskBehaviors(req.getRiskBehaviors());
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

}
