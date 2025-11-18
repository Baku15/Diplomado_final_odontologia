package com.app_odontologia.diplomado_final.service_impl;
import com.app_odontologia.diplomado_final.dto.ApproveRegistrationDto;
import com.app_odontologia.diplomado_final.dto.RegistrationRequestCreateDto;
import com.app_odontologia.diplomado_final.dto.RegistrationRequestViewDto;
import com.app_odontologia.diplomado_final.model.entity.Clinic;
import com.app_odontologia.diplomado_final.model.entity.RegistrationRequest;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.model.enums.RegistrationStatus;
import com.app_odontologia.diplomado_final.model.enums.UserStatus;
import com.app_odontologia.diplomado_final.repository.ClinicRepository;
import com.app_odontologia.diplomado_final.repository.RegistrationRequestRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.ActivationService;
import com.app_odontologia.diplomado_final.service.MailService;
import com.app_odontologia.diplomado_final.service.RegistrationService;
import com.app_odontologia.diplomado_final.service.UserService;
import com.app_odontologia.diplomado_final.util.MappingUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

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

        // Password temporal (no se usará directamente; el usuario la cambiará al activar)
        String dummyPassword = UUID.randomUUID().toString();

        // ⬇️ IMPORTANTE: ahora el usuario aprobado es un CLINIC_ADMIN
        //    (dueño/administrador de SU clínica, puede o no ser dentista)
        User user = userService.createUserFromRegistration(
                rr,
                username,
                dummyPassword,
                "ROLE_CLINIC_ADMIN"   // antes: "ROLE_DENTIST"
        );

        // El usuario queda en estado PENDING_ACTIVATION hasta que use el enlace
        user.setStatus(UserStatus.PENDING_ACTIVATION);
        user.setMustChangePassword(false);
        userRepository.save(user);

        // Crear la clínica asociada a este usuario admin
        Clinic clinic = new Clinic();
        clinic.setAdmin(user);
        clinicRepo.save(clinic);

        // Generar token y enviar enlace de activación (24 horas de vigencia)
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
        // opcional: enviar email informando rechazo (usando reason)
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
