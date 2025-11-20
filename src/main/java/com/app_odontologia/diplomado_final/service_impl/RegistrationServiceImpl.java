package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.ApproveRegistrationDto;
import com.app_odontologia.diplomado_final.dto.RegistrationRequestCreateDto;
import com.app_odontologia.diplomado_final.dto.RegistrationRequestViewDto;
import com.app_odontologia.diplomado_final.model.entity.Clinic;
import com.app_odontologia.diplomado_final.model.entity.DoctorProfile;
import com.app_odontologia.diplomado_final.model.entity.RegistrationRequest;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.model.enums.RegistrationStatus;
import com.app_odontologia.diplomado_final.model.enums.UserStatus;
import com.app_odontologia.diplomado_final.repository.ClinicRepository;
import com.app_odontologia.diplomado_final.repository.DoctorProfileRepository;
import com.app_odontologia.diplomado_final.repository.RegistrationRequestRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.ActivationService;
import com.app_odontologia.diplomado_final.service.MailService;
import com.app_odontologia.diplomado_final.service.RegistrationService;
import com.app_odontologia.diplomado_final.service.UserService;
import com.app_odontologia.diplomado_final.util.MappingUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRequestRepository rrRepo;
    private final UserService userService;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final ActivationService activationService;
    private final ClinicRepository clinicRepo;
    private final DoctorProfileRepository doctorProfileRepo;

    @Override
    public RegistrationRequest create(RegistrationRequestCreateDto dto) {
        rrRepo.findByEmail(dto.getEmail()).ifPresent(r -> {
            if (r.getStatus() == RegistrationStatus.PENDING_REVIEW) {
                throw new IllegalStateException("Ya existe una solicitud pendiente para este email.");
            }
        });

        RegistrationRequest rr = new RegistrationRequest();
        rr.setNombre(dto.getNombre());
        rr.setApellido(dto.getApellido());
        rr.setEmail(dto.getEmail());
        rr.setOcupacion(dto.getOcupacion());
        rr.setZona(dto.getZona());
        rr.setDireccion(dto.getDireccion());
        rr.setDentist(dto.isDentist());
        rr.setStatus(RegistrationStatus.PENDING_REVIEW);

        return rrRepo.save(rr);
    }

    @Override
    @Transactional
    public Page<RegistrationRequestViewDto> listPending(Pageable pageable) {
        return rrRepo.findByStatus(RegistrationStatus.PENDING_REVIEW, pageable)
                .map(MappingUtils::toViewDto);
    }

    @Override
    public void approve(Long id, ApproveRegistrationDto dto, String adminUsername) {
        var rr = rrRepo.findById(id).orElseThrow();
        if (rr.getStatus() != RegistrationStatus.PENDING_REVIEW) {
            throw new IllegalStateException("La solicitud no está pendiente.");
        }

        // Username sugerido o auto-generado
        String username = (dto.getUsername() != null && !dto.getUsername().isBlank())
                ? dto.getUsername()
                : generateUsername(rr.getNombre(), rr.getApellido());

        // Password temporal
        String dummyPassword = UUID.randomUUID().toString();

        // Roles: siempre CLINIC_ADMIN; si marcó "soy dentista", también DENTIST
        java.util.List<String> rolesToAssign = new java.util.ArrayList<>();
        rolesToAssign.add("ROLE_CLINIC_ADMIN");
        if (rr.isDentist()) {
            rolesToAssign.add("ROLE_DENTIST");
        }

        // Crear usuario base
        User user = userService.createUserFromRegistration(
                rr,
                username,
                dummyPassword,
                rolesToAssign
        );

        // Estado inicial
        user.setStatus(UserStatus.PENDING_ACTIVATION);
        user.setMustChangePassword(false);

        // ⭐ REGLA: solo si el dueño marcó "soy dentista" debe completar perfil
        user.setMustCompleteProfile(rr.isDentist());

        user = userRepository.save(user);

        // Crear clínica asociada
        Clinic clinic = new Clinic();
        clinic.setAdmin(user);
        clinic = clinicRepo.save(clinic);

        // Vincular clínica al usuario
        user.setClinic(clinic);
        userRepository.save(user);

        // Si es dentista + admin, crear DoctorProfile vacío para el wizard
        if (rr.isDentist()) {
            DoctorProfile dp = new DoctorProfile();
            dp.setUser(user);
            doctorProfileRepo.save(dp);
        }

        // Token de activación (24 horas)
        String token = activationService.createActivationToken(user.getId(), 24);
        String activationLink = "http://localhost:4200/activar?token=" + token;
        mailService.sendActivationEmail(rr.getEmail(), activationLink);

        // Marcar solicitud como aprobada
        rr.setStatus(RegistrationStatus.APPROVED);
        rr.setReviewedAt(Instant.now());
        rr.setReviewedBy(adminUsername);
        rrRepo.save(rr);
    }

    @Override
    public void reject(Long id, String adminUsername, String reason) {
        RegistrationRequest rr = rrRepo.findById(id).orElseThrow();
        rr.setStatus(RegistrationStatus.REJECTED);
        rr.setReviewedAt(Instant.now());
        rr.setReviewedBy(adminUsername);
        rrRepo.save(rr);
        // opcional: enviar email con reason
    }

    private String generateUsername(String nombre, String apellido) {
        String n = (nombre != null && !nombre.isBlank()) ? nombre.trim() : "user";
        String a = (apellido != null && !apellido.isBlank()) ? apellido.trim() : "app";

        String base = (n.substring(0, 1) + a)
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");

        String suffix = String.format("%04d",
                ThreadLocalRandom.current().nextInt(0, 10000));

        return base + suffix;
    }
}
