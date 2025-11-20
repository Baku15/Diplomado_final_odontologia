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
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_CLINIC_ADMIN')")
    public List<ClinicRoomDto> list(@PathVariable Long clinicId) {
        return clinicRoomService.listActiveByClinic(clinicId);
    }

    /**
     * Crea un nuevo consultorio para la clínica.
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
     * No se borra físicamente.
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
