package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.consultation.*;
import com.app_odontologia.diplomado_final.dto.odontogram.DentalProcedureDto;
import com.app_odontologia.diplomado_final.service.ClinicalConsultationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clinic/{clinicId}/patients/{patientId}/consultations")
@RequiredArgsConstructor
public class ClinicalConsultationController {

    private final ClinicalConsultationService consultationService;

    // ============================
    // LISTADO
    // ============================

    @GetMapping
    public ResponseEntity<List<ClinicalConsultationDto>> list(
            @PathVariable Long clinicId,
            @PathVariable Long patientId
    ) {
        return ResponseEntity.ok(
                consultationService.listConsultations(clinicId, patientId)
        );
    }

    // ============================
    // CONSULTA ACTIVA
    // ============================

    @GetMapping("/active")
    public ResponseEntity<ClinicalConsultationDto> getActive(
            @PathVariable Long clinicId,
            @PathVariable Long patientId
    ) {
        ClinicalConsultationDto dto =
                consultationService.getActiveConsultation(clinicId, patientId);

        return dto != null
                ? ResponseEntity.ok(dto)
                : ResponseEntity.noContent().build();
    }

    // ============================
    // ACTIVE o IN_PROGRESS
    // ============================

    @GetMapping("/active-or-in-progress")
    public ResponseEntity<ClinicalConsultationDto> getActiveOrInProgress(
            @PathVariable Long clinicId,
            @PathVariable Long patientId
    ) {
        ClinicalConsultationDto dto =
                consultationService.getActiveOrInProgress(clinicId, patientId);

        return dto != null
                ? ResponseEntity.ok(dto)
                : ResponseEntity.noContent().build();
    }

    // ============================
    // CONSULTA POR ID
    // ============================

    @GetMapping("/{consultationId}")
    public ResponseEntity<ClinicalConsultationDto> getById(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long consultationId
    ) {
        return ResponseEntity.ok(
                consultationService.getById(clinicId, patientId, consultationId)
        );
    }



    // ============================
    // CERRAR CONSULTA (SIN CAMBIAR ESTADO A IN_PROGRESS)
    // ============================

    @PostMapping("/{id}/close")
    public ResponseEntity<ClinicalConsultationDto> close(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            @PathVariable Long id,
            @RequestBody CloseConsultationRequest req,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                consultationService.closeConsultation(
                        id,
                        req,
                        auth.getName()
                )
        );
    }

    // ============================
    // PROCEDIMIENTOS
    // ============================

    @GetMapping("/{consultationId}/procedures")
    public ResponseEntity<List<DentalProcedureDto>> listProcedures(
            @PathVariable Long consultationId
    ) {
        return ResponseEntity.ok(
                consultationService.listProceduresByConsultation(consultationId)
        );
    }

    @PostMapping("/enter-odontogram")
    public ResponseEntity<ClinicalConsultationDto> enterOdontogram(
            @PathVariable Long clinicId,
            @PathVariable Long patientId,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                consultationService.enterOdontogram(
                        clinicId,
                        patientId,
                        auth.getName()
                )
        );
    }

    @PostMapping("/{id}/leave-odontogram")
    public ResponseEntity<Void> leaveOdontogram(
            @PathVariable Long id,
            @RequestParam boolean hasClinicalChanges
    ) {
        consultationService.leaveOdontogram(id, hasClinicalChanges);
        return ResponseEntity.noContent().build();
    }

}
