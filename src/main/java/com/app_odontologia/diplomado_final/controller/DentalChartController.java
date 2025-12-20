package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.attachment.AttachmentDto;
import com.app_odontologia.diplomado_final.dto.odontogram.*;
import com.app_odontologia.diplomado_final.service.AttachmentService;
import com.app_odontologia.diplomado_final.service.DentalChartService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clinic/{clinicId}/patients/{patientId}/odontogram")
public class DentalChartController {

    private final DentalChartService dentalChartService;
    private final AttachmentService attachmentService;




    public DentalChartController(
            DentalChartService dentalChartService,
            AttachmentService attachmentService
    ) {
        this.dentalChartService = dentalChartService;
        this.attachmentService = attachmentService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<DentalChartDto> getActiveChart(
            @PathVariable Long clinicId,
            @PathVariable Long patientId
    ) {
        DentalChartDto dto = dentalChartService.getActiveChart(clinicId, patientId);
        if (dto == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<DentalChartDto> createChart(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @RequestParam(required = false) Long clinicalRecordId,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : null;

        DentalChartDto dto = dentalChartService.createChart(clinicId, patientId, clinicalRecordId, username);

        URI location = URI.create(String.format("/api/clinic/%d/patients/%d/odontogram", clinicId, patientId));

        return ResponseEntity.ok().location(location).body(dto);
    }

    @PutMapping("/{chartId}/tooth")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<DentalChartDto> upsertTooth(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long chartId,
            @Valid @RequestBody UpsertToothRequest req,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : null;
        DentalChartDto dto = dentalChartService.upsertTooth(chartId, req, username);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/{chartId}/procedures")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<DentalProcedureDto> addProcedure(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long chartId,
            @Valid @RequestBody AddProcedureRequest req
    ) {
        DentalProcedureDto dto = dentalChartService.addProcedure(chartId, req);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<List<DentalChartDto>> history(@PathVariable Long patientId) {
        List<DentalChartDto> list = dentalChartService.getChartHistory(patientId);
        return ResponseEntity.ok(list);
    }

    @PostMapping("/{chartId}/close")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<DentalChartDto> closeChart(@PathVariable Long chartId) {
        DentalChartDto dto = dentalChartService.closeChart(chartId);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{chartId}/procedures/{procedureId}/complete")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<DentalProcedureDto> completeProcedure(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long chartId,
            @PathVariable Long procedureId,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : null;
        DentalProcedureDto dto = dentalChartService.completeProcedure(chartId, procedureId, username);
        return ResponseEntity.ok(dto);
    }
    @PutMapping("/{chartId}/procedures/{procedureId}")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<DentalProcedureDto> updateProcedure(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long chartId,
            @PathVariable Long procedureId,
            @Valid @RequestBody AddProcedureRequest req,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : null;

        // opcional: validar que procedure pertenece a chartId (mejor hacer en service)
        DentalProcedureDto dto = dentalChartService.updateProcedure(procedureId, req, username);
        return ResponseEntity.ok(dto);
    }

    // POST /{chartId}/tooth/{toothNumber}/attachments
    @PostMapping("/{chartId}/tooth/{toothNumber}/attachments")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<ToothAttachmentDto> addToothAttachment(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long chartId,
            @PathVariable Integer toothNumber,
            @RequestParam Long attachmentId,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : null;
        ToothAttachmentDto dto = dentalChartService.addToothAttachment(chartId, toothNumber, attachmentId, username);
        return ResponseEntity.ok(dto);
    }

    // GET /{chartId}/tooth/{toothNumber}/attachments
    @GetMapping("/{chartId}/tooth/{toothNumber}/attachments")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<List<AttachmentDto>> listToothAttachments(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Integer toothNumber
    ) {
        return ResponseEntity.ok(
                attachmentService.listAttachmentsByTooth(
                        clinicId,
                        patientId,
                        "TOOTH_" + toothNumber,
                        300
                )
        );
    }




    // DELETE /{chartId}/tooth/{toothNumber}/attachments/{attachmentId}
    @DeleteMapping("/{chartId}/tooth/{toothNumber}/attachments/{attachmentId}")
    @PreAuthorize("hasRole('ROLE_CLINIC_ADMIN') or hasRole('ROLE_DENTIST')")
    public ResponseEntity<Void> deleteToothAttachment(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long chartId,
            @PathVariable Integer toothNumber,
            @PathVariable Long attachmentId,
            Principal principal
    ) {
        String username = principal != null ? principal.getName() : null;
        dentalChartService.removeToothAttachment(chartId, toothNumber, attachmentId, username);
        return ResponseEntity.noContent().build();
    }



}
