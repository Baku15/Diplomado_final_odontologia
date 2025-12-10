package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.doctor.DoctorProfileMeDto;
import com.app_odontologia.diplomado_final.dto.doctor.DoctorProfileUpdateDto;
import com.app_odontologia.diplomado_final.model.entity.Clinic;
import com.app_odontologia.diplomado_final.model.entity.ClinicRoom;
import com.app_odontologia.diplomado_final.model.entity.DoctorProfile;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.repository.ClinicRoomRepository;
import com.app_odontologia.diplomado_final.repository.DoctorProfileRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.DoctorProfileSelfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class DoctorProfileController {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final ClinicRoomRepository clinicRoomRepository;
    private final DoctorProfileSelfService doctorProfileSelfService;


    @PostMapping("/doctor-profile")
    public ResponseEntity<?> saveMyDoctorProfile(
            @RequestBody DoctorProfileUpdateDto dto,
            Authentication auth
    ) {
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + username));

        Clinic clinic = user.getClinic();
        if (clinic == null) {
            throw new IllegalStateException("El usuario no tiene clínica asociada.");
        }

        // Buscar o crear perfil
        DoctorProfile profile = doctorProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    DoctorProfile p = new DoctorProfile();
                    p.setUser(user);
                    return p;
                });

        // Datos básicos
        profile.setLicenseNumber(dto.getLicenseNumber());
        profile.setSpecialty(dto.getSpecialty());
        profile.setPhone(dto.getPhone());
        profile.setAddress(dto.getAddress());
        profile.setBio(dto.getBio());

        // Consultorio principal
        if (dto.getPrimaryRoomId() != null) {
            ClinicRoom room = clinicRoomRepository
                    .findByIdAndClinicId(dto.getPrimaryRoomId(), clinic.getId())
                    .orElseThrow(() ->
                            new IllegalStateException("El consultorio no pertenece a tu clínica.")
                    );
            profile.setPrimaryRoom(room);
        } else {
            throw new IllegalStateException("Debes seleccionar un consultorio principal.");
        }

        doctorProfileRepository.save(profile);

        // Marcar que ya completó perfil
        user.setMustCompleteProfile(false);
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/doctor-profile")
    @PreAuthorize("hasAuthority('ROLE_DENTIST')")
    public DoctorProfileMeDto getMyProfile(Authentication auth) {
        String username = auth.getName();
        return doctorProfileSelfService.getMyProfile(username);
    }

    @PutMapping("/doctor-profile")
    @PreAuthorize("hasAuthority('ROLE_DENTIST')")
    public DoctorProfileMeDto updateMyProfile(
            @Valid @RequestBody DoctorProfileMeDto dto,
            Authentication auth
    ) {
        String username = auth.getName();
        return doctorProfileSelfService.updateMyProfile(username, dto);
    }
}
