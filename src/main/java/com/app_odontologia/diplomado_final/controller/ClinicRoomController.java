package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.ClinicRoomDto;
import com.app_odontologia.diplomado_final.dto.ClinicRoomRequestDto;
import com.app_odontologia.diplomado_final.service.ClinicRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clinic/{clinicId}/rooms")
public class ClinicRoomController {

    private final ClinicRoomService clinicRoomService;

    /**
     * Lista todos los consultorios ACTIVOS de la clínica.
     * Accesible para:
     *  - ROLE_CLINIC_ADMIN
     *  - ROLE_DENTIST
     */
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_CLINIC_ADMIN', 'ROLE_DENTIST')")
    public List<ClinicRoomDto> list(@PathVariable Long clinicId) {
        return clinicRoomService.listActiveByClinic(clinicId);
    }

    /**
     * Crea un nuevo consultorio para la clínica.
     * Solo admin de clínica.
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_CLINIC_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ClinicRoomDto create(
            @PathVariable Long clinicId,
            @Valid @RequestBody ClinicRoomRequestDto dto
    ) {
        return clinicRoomService.create(clinicId, dto);
    }

    /**
     * Actualiza los datos de un consultorio existente.
     * Solo admin de clínica.
     */
    @PutMapping("/{roomId}")
    @PreAuthorize("hasAuthority('ROLE_CLINIC_ADMIN')")
    public ClinicRoomDto update(
            @PathVariable Long clinicId,
            @PathVariable Long roomId,
            @Valid @RequestBody ClinicRoomRequestDto dto
    ) {
        return clinicRoomService.update(clinicId, roomId, dto);
    }

    /**
     * Desactiva un consultorio (active = false).
     * Solo admin de clínica.
     */
    @DeleteMapping("/{roomId}")
    @PreAuthorize("hasAuthority('ROLE_CLINIC_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(
            @PathVariable Long clinicId,
            @PathVariable Long roomId
    ) {
        clinicRoomService.deactivate(clinicId, roomId);
    }
}

