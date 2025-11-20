// controller/DoctorProfileController.java
package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.DoctorProfileUpdateDto;
import com.app_odontologia.diplomado_final.model.entity.ClinicRoom;
import com.app_odontologia.diplomado_final.model.entity.DoctorProfile;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.repository.ClinicRoomRepository;
import com.app_odontologia.diplomado_final.repository.DoctorProfileRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/users/me/doctor-profile")
@RequiredArgsConstructor
public class DoctorProfileController {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final ClinicRoomRepository clinicRoomRepository;

    @PostMapping
    public ResponseEntity<?> upsertMyDoctorProfile(
            @Valid @RequestBody DoctorProfileUpdateDto dto,
            Authentication auth
    ) {
        String username = auth.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (user.getClinic() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El usuario no está asociado a ninguna clínica");
        }

        // crear o actualizar perfil
        DoctorProfile profile = doctorProfileRepository
                .findByUserId(user.getId())
                .orElseGet(() -> {
                    DoctorProfile p = new DoctorProfile();
                    p.setUser(user);
                    return p;
                });

        profile.setLicenseNumber(dto.getLicenseNumber());
        profile.setSpecialty(dto.getSpecialty());
        profile.setPhone(dto.getPhone());
        profile.setAddress(dto.getAddress());
        profile.setBio(dto.getBio());

        // 🔹 asignar consultorio si viene roomId
        if (dto.getRoomId() != null) {
            ClinicRoom room = clinicRoomRepository.findById(dto.getRoomId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Consultorio no encontrado"));

            // seguridad: que el consultorio pertenezca a MI clínica
            if (!room.getClinic().getId().equals(user.getClinic().getId())) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "El consultorio no pertenece a tu clínica");
            }

            profile.setPrimaryRoom(room);
        } else {
            profile.setPrimaryRoom(null);
        }

        doctorProfileRepository.save(profile);

        // marcar que ya completó su perfil
        user.setMustCompleteProfile(false);
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
}
