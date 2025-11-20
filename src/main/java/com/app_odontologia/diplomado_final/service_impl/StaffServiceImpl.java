package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.CreateAssistantDto;
import com.app_odontologia.diplomado_final.dto.CreateDoctorDto;
import com.app_odontologia.diplomado_final.model.entity.Clinic;
import com.app_odontologia.diplomado_final.model.entity.Role;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.model.entity.DoctorProfile;
import com.app_odontologia.diplomado_final.repository.ClinicRepository;
import com.app_odontologia.diplomado_final.repository.DoctorProfileRepository;
import com.app_odontologia.diplomado_final.repository.RoleRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.StaffService;
import com.app_odontologia.diplomado_final.service.ActivationService;
import com.app_odontologia.diplomado_final.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffServiceImpl implements StaffService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final ClinicRepository clinicRepo;
    private final PasswordEncoder passwordEncoder;
    private final ActivationService activationService;
    private final MailService mailService;
    private final DoctorProfileRepository doctorProfileRepo;

    @Override
    public User createDoctor(Long clinicId, CreateDoctorDto dto, String createdByUsername) {
        Clinic clinic = clinicRepo.findById(clinicId).orElseThrow(() -> new IllegalArgumentException("Clinic not found"));

        // uniqueness checks
        userRepo.findByEmail(dto.getEmail()).ifPresent(u -> { throw new IllegalStateException("Email ya en uso"); });

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            userRepo.findByUsername(dto.getUsername()).ifPresent(u -> { throw new IllegalStateException("Username ya en uso"); });
        }

        // prepare user
        User user = new User();
        String username = (dto.getUsername() != null && !dto.getUsername().isBlank()) ? dto.getUsername() : generateUsername(dto.getNombre(), dto.getApellido());
        String randomPass = UUID.randomUUID().toString();

        user.setUsername(username);
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(randomPass));
        user.setNombres(dto.getNombre());
        user.setApellidos(dto.getApellido());
        user.setStatus(com.app_odontologia.diplomado_final.model.enums.UserStatus.PENDING_ACTIVATION);
        user.setClinic(clinic);

        // add ROLE_DENTIST
        Role dentistRole = roleRepo.findByName("ROLE_DENTIST")
                .orElseThrow(() -> new IllegalStateException("ROLE_DENTIST no existe"));
        user.getRoles().add(dentistRole);

        // persist user
        user = userRepo.save(user);

        // (optionally) create doctor profile if provided
        if (dto.getLicenseNumber() != null && !dto.getLicenseNumber().isBlank()) {
            DoctorProfile dp = new DoctorProfile();
            dp.setUser(user);
            dp.setLicenseNumber(dto.getLicenseNumber());
            dp.setSpecialty(dto.getSpecialty());
            dp.setPhone(dto.getPhone());
            dp.setAddress(dto.getAddress());
            doctorProfileRepo.save(dp);
        }

        // generate activation token + send email
        String token = activationService.createActivationToken(user.getId(), 24);
        String activationLink = "http://localhost:4200/activar?token=" + token;
        mailService.sendActivationEmail(dto.getEmail(), activationLink);

        return user;
    }

    @Override
    public User createAssistant(Long clinicId, CreateAssistantDto dto, String createdByUsername) {
        Clinic clinic = clinicRepo.findById(clinicId).orElseThrow(() -> new IllegalArgumentException("Clinic not found"));

        userRepo.findByEmail(dto.getEmail()).ifPresent(u -> { throw new IllegalStateException("Email ya en uso"); });

        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            userRepo.findByUsername(dto.getUsername()).ifPresent(u -> { throw new IllegalStateException("Username ya en uso"); });
        }

        User user = new User();
        String username = (dto.getUsername() != null && !dto.getUsername().isBlank()) ? dto.getUsername() : generateUsername(dto.getNombre(), dto.getApellido());
        String randomPass = UUID.randomUUID().toString();

        user.setUsername(username);
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(randomPass));
        user.setNombres(dto.getNombre());
        user.setApellidos(dto.getApellido());
        user.setStatus(com.app_odontologia.diplomado_final.model.enums.UserStatus.PENDING_ACTIVATION);
        user.setClinic(clinic);

        Role assistantRole = roleRepo.findByName("ROLE_ASSISTANT")
                .orElseThrow(() -> new IllegalStateException("ROLE_ASSISTANT no existe"));
        user.getRoles().add(assistantRole);

        user = userRepo.save(user);

        // activation
        String token = activationService.createActivationToken(user.getId(), 24);
        String activationLink = "http://localhost:4200/activar?token=" + token;
        mailService.sendActivationEmail(dto.getEmail(), activationLink);

        return user;
    }

    private String generateUsername(String nombre, String apellido) {
        String n = (nombre != null && !nombre.isBlank()) ? nombre.trim() : "user";
        String a = (apellido != null && !apellido.isBlank()) ? apellido.trim() : "app";
        String base = (n.substring(0, 1) + a).toLowerCase().replaceAll("[^a-z0-9]", "");
        String suffix = String.format("%04d", (int)(Math.random() * 10000));
        return base + suffix;
    }
}
