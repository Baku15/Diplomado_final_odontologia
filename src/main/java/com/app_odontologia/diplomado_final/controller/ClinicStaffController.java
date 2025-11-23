package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.context.ClinicContext;
import com.app_odontologia.diplomado_final.dto.*;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.service.StaffService;
import com.app_odontologia.diplomado_final.service.UserService;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.repository.ClinicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clinic/{clinicId}/staff")
@RequiredArgsConstructor
public class ClinicStaffController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final StaffService staffService;

    // Crear odontólogo
    @PostMapping("/doctors")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<StaffViewDto> createDoctor(
            @PathVariable Long clinicId,
            @Valid @RequestBody CreateStaffDto dto,
            Authentication auth
    ) {
        // Validar que auth user es admin de la clínica
        var clinic = clinicRepository.findById(clinicId).orElseThrow(() -> new IllegalStateException("Clinica no encontrada"));
        if (clinic.getAdmin() == null || !clinic.getAdmin().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }

        List<String> roles = (dto.getRoleNames() == null || dto.getRoleNames().isEmpty())
                ? List.of("ROLE_DENTIST")
                : dto.getRoleNames();

        User u = userService.createUserForClinic(dto, roles, clinicId, auth.getName());
        return ResponseEntity.status(201).body(toView(u));
    }

    // Crear asistente
    @PostMapping("/assistants")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<StaffViewDto> createAssistant(
            @PathVariable Long clinicId,
            @Valid @RequestBody CreateStaffDto dto,
            Authentication auth
    ) {
        var clinic = clinicRepository.findById(clinicId).orElseThrow(() -> new IllegalStateException("Clinica no encontrada"));
        if (clinic.getAdmin() == null || !clinic.getAdmin().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }

        List<String> roles = (dto.getRoleNames() == null || dto.getRoleNames().isEmpty())
                ? List.of("ROLE_ASSISTANT")
                : dto.getRoleNames();

        User u = userService.createUserForClinic(dto, roles, clinicId, auth.getName());
        return ResponseEntity.status(201).body(toView(u));
    }

    // Listar staff (paginado)
    @GetMapping
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public Page<StaffViewDto> listStaff(
            @PathVariable Long clinicId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth
    ) {
        var clinic = clinicRepository.findById(clinicId).orElseThrow(() -> new IllegalStateException("Clinica no encontrada"));
        if (clinic.getAdmin() == null || !clinic.getAdmin().getUsername().equals(auth.getName())) {
            throw new SecurityException("No autorizado");
        }

        var pg = userRepository.findByClinic_Id(clinicId, PageRequest.of(page, size));
        return pg.map(this::toView);
    }

    private StaffViewDto toView(User u) {
        StaffViewDto dto = new StaffViewDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setNombre(u.getNombres());
        dto.setApellido(u.getApellidos());
        dto.setEmail(u.getEmail());
        dto.setPhone(u.getZona()); // si guardaste phone en otro campo, ajustar
        dto.setStatus(u.getStatus() != null ? u.getStatus().name() : null);
        dto.setRoles(u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet()));
        return dto;
    }

    // PUT /api/clinic/{clinicId}/staff/{userId}
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('CLINIC_ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<StaffViewDto> updateStaff(
            @PathVariable Long clinicId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateStaffDto dto,
            Authentication auth
    ) {
        var clinic = clinicRepository.findById(clinicId).orElseThrow(() -> new IllegalStateException("Clinica no encontrada"));
        boolean isSuper = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERUSER"));
        if (!isSuper) {
            if (clinic.getAdmin() == null || !clinic.getAdmin().getUsername().equals(auth.getName())) {
                return ResponseEntity.status(403).build();
            }
        }

        var updated = userService.updateUserForClinic(clinicId, userId, dto, auth.getName());
        return ResponseEntity.ok(toView(updated));
    }

    // POST /api/clinic/{clinicId}/staff/{userId}/activate
    @PostMapping("/{userId}/activate")
    @PreAuthorize("hasRole('CLINIC_ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<Void> activateStaff(
            @PathVariable Long clinicId,
            @PathVariable Long userId,
            Authentication auth
    ) {
        var clinic = clinicRepository.findById(clinicId).orElseThrow(() -> new IllegalStateException("Clinica no encontrada"));
        boolean isSuper = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERUSER"));
        if (!isSuper) {
            if (clinic.getAdmin() == null || !clinic.getAdmin().getUsername().equals(auth.getName())) {
                return ResponseEntity.status(403).build();
            }
        }

        userService.setUserStatus(userId, "ACTIVE", auth.getName());
        return ResponseEntity.ok().build();
    }

    // POST /api/clinic/{clinicId}/staff/{userId}/deactivate
    @PostMapping("/{userId}/deactivate")
    @PreAuthorize("hasRole('CLINIC_ADMIN') or hasRole('SUPERUSER')")
    public ResponseEntity<Void> deactivateStaff(
            @PathVariable Long clinicId,
            @PathVariable Long userId,
            Authentication auth
    ) {
        var clinic = clinicRepository.findById(clinicId).orElseThrow(() -> new IllegalStateException("Clinica no encontrada"));
        boolean isSuper = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPERUSER"));
        if (!isSuper) {
            if (clinic.getAdmin() == null || !clinic.getAdmin().getUsername().equals(auth.getName())) {
                return ResponseEntity.status(403).build();
            }
        }

        userService.setUserStatus(userId, "BLOCKED", auth.getName());
        return ResponseEntity.ok().build();
    }

    // Invitar doctor por correo (no crea usuario aún)
    @PostMapping("/doctors/invitations")
    @PreAuthorize("hasRole('CLINIC_ADMIN')")
    public ResponseEntity<DoctorInvitationDto> inviteDoctor(
            @PathVariable Long clinicId,
            @Valid @RequestBody InviteDoctorRequestDto request,
            Authentication auth
    ) {
        // 1) Validar que el usuario autenticado es admin de la clínica
        var clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalStateException("Clínica no encontrada"));

        if (clinic.getAdmin() == null
                || !clinic.getAdmin().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(403).build();
        }

        // 2) Validar que no exista ya un usuario con ese correo
        String email = request.getEmail();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese correo electrónico."
            );
        }

        // 3) Delegar lógica de invitación al servicio
        DoctorInvitationDto dto = staffService.inviteDoctor(
                clinicId,                   // clínica a la que se invita
                request,                    // datos del doctor
                auth.getName()              // quién hizo la invitación
        );

        return ResponseEntity.ok(dto);
    }
}