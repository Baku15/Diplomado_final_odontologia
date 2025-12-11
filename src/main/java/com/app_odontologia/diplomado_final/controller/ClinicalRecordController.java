package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.clinical.ClinicalRecordDetailDto;
import com.app_odontologia.diplomado_final.dto.clinical.ClinicalRecordUpsertRequest;
import com.app_odontologia.diplomado_final.model.entity.ClinicalRecord;
import com.app_odontologia.diplomado_final.service.ClinicalRecordService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.security.Principal;

@RestController
@RequestMapping("/api/clinic/{clinicId}/patients/{patientId}/clinical-record")
public class ClinicalRecordController {

    private final ClinicalRecordService clinicalRecordService;

    public ClinicalRecordController(ClinicalRecordService clinicalRecordService) {
        this.clinicalRecordService = clinicalRecordService;
    }

    /**
     * Obtener la historia clínica ACTIVA de un paciente.
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<ClinicalRecordDetailDto> getClinicalRecord(
            @PathVariable Long clinicId,
            @PathVariable Long patientId
    ) {
        ClinicalRecordDetailDto dto = clinicalRecordService.getByPatient(clinicId, patientId);
        return ResponseEntity.ok(dto);
    }

    /**
     * Crear una historia clínica para un paciente (si no existe una ACTIVA).
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<ClinicalRecordDetailDto> createClinicalRecord(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @Valid @RequestBody ClinicalRecordUpsertRequest request,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : null;

        ClinicalRecordDetailDto dto = clinicalRecordService.createForPatient(
                clinicId,
                patientId,
                request,
                username
        );

        URI location = URI.create(String.format(
                "/api/clinic/%d/patients/%d/clinical-record",
                clinicId, patientId
        ));

        return ResponseEntity.created(location).body(dto);
    }

    /**
     * Actualizar la historia clínica ACTIVA del paciente.
     */
    @PutMapping
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<ClinicalRecordDetailDto> updateClinicalRecord(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @Valid @RequestBody ClinicalRecordUpsertRequest request,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : null;

        ClinicalRecordDetailDto dto = clinicalRecordService.updateForPatient(
                clinicId,
                patientId,
                request,
                username
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * Exportar la historia clínica activa en formato FHIR (Bundle JSON).
     */
    @GetMapping(value = "/fhir", produces = "application/fhir+json")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<String> exportClinicalRecordFhir(
            @PathVariable Long clinicId,
            @PathVariable Long patientId
    ) {
        String json = clinicalRecordService.exportFhirJson(clinicId, patientId);
        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf("application/fhir+json"))
                .body(json);
    }

    @PostMapping("/close")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<ClinicalRecordDetailDto> closeClinicalRecordForPatient(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            Principal principal
    ) {
        // intentamos obtener la historia clínica ACTIVA de ese clinic+patient
        ClinicalRecordDetailDto existing;
        try {
            existing = clinicalRecordService.getByPatient(clinicId, patientId);
        } catch (IllegalStateException ex) {
            // la service lanza IllegalStateException cuando no hay historia activa
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }

        if (existing == null || existing.getId() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Historia clínica activa no encontrada para patientId=" + patientId + " clinicId=" + clinicId);
        }

        // opcional: aquí podrías validar que el usuario (principal) pertenece a la clínica
        // o tiene permiso para cerrar — lo dejo como mejora si lo necesitas.

        ClinicalRecordDetailDto closed = clinicalRecordService.closeClinicalRecord(existing.getId());
        return ResponseEntity.ok(closed);
    }
}
