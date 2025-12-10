package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.doctor.DentistProfileDto;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.repository.ClinicRepository;
import com.app_odontologia.diplomado_final.repository.DentistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/clinic/wizard")
@RequiredArgsConstructor
public class ClinicWizardController {

    private final ClinicRepository clinicRepo;
    private final DentistRepository dentistRepo;

    @GetMapping("/check")
    public ResponseEntity<?> check(@AuthenticationPrincipal User me) {
        var clinic = clinicRepo.findByAdmin(me).orElse(null);
        boolean hasPrincipal = (clinic != null) && dentistRepo.existsByClinicId(clinic.getId());
        return ResponseEntity.ok(new WizardCheckResponse(hasPrincipal));
    }

    @PostMapping("/i-am-the-dentist")
    public ResponseEntity<?> iAmTheDentist(@AuthenticationPrincipal User me,
                                           @RequestBody DentistProfileDto dto) {
        var clinic = clinicRepo.findByAdmin(me)
                .orElseThrow(() -> new IllegalStateException("Clinic not found for admin"));

        var d = new com.app_odontologia.diplomado_final.model.entity.Dentist();
        d.setUser(me);
        d.setClinic(clinic);
        d.setLicenseNumber(dto.getLicenseNumber());
        d.setSpecialty(dto.getSpecialty());
        d.setPhone(dto.getPhone());
        d.setAddress(dto.getAddress());

        dentistRepo.save(d);
        return ResponseEntity.ok().build();
    }

    public record WizardCheckResponse(boolean hasPrincipalDentist) {}
}
