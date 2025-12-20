package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.attachment.AttachmentDto;
import com.app_odontologia.diplomado_final.dto.attachment.AttachmentLinkRequest;
import com.app_odontologia.diplomado_final.dto.attachment.PresignedUploadRequest;
import com.app_odontologia.diplomado_final.dto.attachment.PresignedUploadResponse;
import com.app_odontologia.diplomado_final.dto.clinical.ClinicalRecordDetailDto;
import com.app_odontologia.diplomado_final.dto.clinical.ClinicalRecordUpsertRequest;
import com.app_odontologia.diplomado_final.service.ClinicalRecordService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        ClinicalRecordDetailDto closed = clinicalRecordService.closeClinicalRecord(existing.getId());
        return ResponseEntity.ok(closed);
    }

    // ---------------- attachments endpoints (presign/link/list/delete) ----------------

    @PostMapping("/attachments/presign")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<PresignedUploadResponse> presignAttachment(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @RequestBody PresignedUploadRequest req,
            Principal principal
    ) {
        try {
            String username = principal != null ? principal.getName() : null;
            PresignedUploadResponse resp = clinicalRecordService.generatePresignedUploadUrl(clinicId, patientId, req, username);
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar presigned URL: " + ex.getMessage(), ex);
        }
    }

    @PostMapping("/attachments/link")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<AttachmentDto> linkAttachment(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @RequestBody AttachmentLinkRequest req,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : null;
        AttachmentDto dto = clinicalRecordService.linkAttachment(clinicId, patientId, req, username);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/attachments")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<Page<AttachmentDto>> listAttachments(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AttachmentDto> result = clinicalRecordService.listAttachments(clinicId, patientId, pageable);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/attachments/{attachmentId}")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long attachmentId,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : null;
        try {
            clinicalRecordService.deleteAttachment(clinicId, patientId, attachmentId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        }
    }
}
