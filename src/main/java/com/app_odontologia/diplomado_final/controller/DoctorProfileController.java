package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.DoctorProfileDto;
import com.app_odontologia.diplomado_final.model.entity.DoctorProfile;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.repository.DoctorProfileRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class DoctorProfileController {

    private final DoctorProfileRepository profileRepo;
    private final UserRepository userRepo;

    @GetMapping("/me/doctor-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyProfile(Authentication auth) {
        Object principal = auth.getPrincipal();
        if (!(principal instanceof User)) return ResponseEntity.status(403).body("Principal no válido");
        User me = (User) principal;

        return profileRepo.findByUser(me)
                .map(p -> ResponseEntity.ok(p))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/me/doctor-profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> upsertMyProfile(@RequestBody DoctorProfileDto dto, Authentication auth) {
        Object principal = auth.getPrincipal();
        if (!(principal instanceof User)) return ResponseEntity.status(403).body("Principal no válido");
        User me = (User) principal;

        DoctorProfile profile = profileRepo.findByUser(me).orElseGet(DoctorProfile::new);
        profile.setUser(me);
        profile.setLicenseNumber(dto.getLicenseNumber());
        profile.setSpecialty(dto.getSpecialty());
        profile.setPhone(dto.getPhone());
        profile.setAddress(dto.getAddress());
        profile.setBio(dto.getBio());
        profileRepo.save(profile);

        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{userId}/doctor-profile")
    @PreAuthorize("hasRole('SUPERUSER') or hasRole('ROLE_CLINIC_ADMIN')") // clinic admin can read — additional checks possible
    public ResponseEntity<?> getProfileByUser(@PathVariable Long userId) {
        var user = userRepo.findById(userId).orElseThrow();
        return profileRepo.findByUser(user).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
