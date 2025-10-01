package com.app_odontologia.diplomado_final.service_impl;
import org.springframework.transaction.annotation.Transactional;

import com.app_odontologia.diplomado_final.dto.ApproveRegistrationDto;
import com.app_odontologia.diplomado_final.dto.RegistrationRequestCreateDto;
import com.app_odontologia.diplomado_final.dto.RegistrationRequestViewDto;
import com.app_odontologia.diplomado_final.model.entity.RegistrationRequest;
import com.app_odontologia.diplomado_final.model.enums.RegistrationStatus;
import com.app_odontologia.diplomado_final.repository.RegistrationRequestRepository;
import com.app_odontologia.diplomado_final.service.MailService;
import com.app_odontologia.diplomado_final.service.RegistrationService;
import com.app_odontologia.diplomado_final.service.UserService;
import com.app_odontologia.diplomado_final.util.MappingUtils;
import com.app_odontologia.diplomado_final.util.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRequestRepository rrRepo;
    private final UserService userService;
    private final MailService mailService;

    @Override
    public RegistrationRequest create(RegistrationRequestCreateDto dto) {
        rrRepo.findByEmail(dto.getEmail()).ifPresent(r -> {
            if (r.getStatus() == RegistrationStatus.PENDIENTE) {
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
        rr.setStatus(RegistrationStatus.PENDIENTE);
        return rrRepo.save(rr);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegistrationRequestViewDto> listPending(Pageable pageable) {
        return rrRepo.findByStatus(RegistrationStatus.PENDIENTE, pageable)
                .map(MappingUtils::toViewDto);
    }

    @Override
    public void approve(Long id, ApproveRegistrationDto dto, String adminUsername) {
        RegistrationRequest rr = rrRepo.findById(id).orElseThrow();
        if (rr.getStatus() != RegistrationStatus.PENDIENTE) {
            throw new IllegalStateException("No está pendiente.");
        }

        String username = (dto.getUsername() != null && !dto.getUsername().isBlank())
                ? dto.getUsername()
                : generateUsername(rr.getNombre(), rr.getApellido());

        String tempPassword = PasswordGenerator.secureTemp(); // 12-16+ caracteres seguros

        userService.createUserFromRegistration(
                rr,
                username,
                tempPassword,
                dto.getRoleName() != null ? dto.getRoleName() : "ROLE_PATIENT"
        );

        rr.setStatus(RegistrationStatus.APROBADA);
        rr.setReviewedAt(Instant.now());
        rr.setReviewedBy(adminUsername);
        rrRepo.save(rr);

        if (Boolean.TRUE.equals(dto.getSendTempPassword())) {
            mailService.sendCredentialsEmail(rr.getEmail(), username, tempPassword);
        }
    }

    @Override
    public void reject(Long id, String adminUsername, String reason) {
        RegistrationRequest rr = rrRepo.findById(id).orElseThrow();
        rr.setStatus(RegistrationStatus.RECHAZADA);
        rr.setReviewedAt(Instant.now());
        rr.setReviewedBy(adminUsername);
        rrRepo.save(rr);
        // TODO: (Opcional) enviar email de rechazo con 'reason'
    }

    // --- helper privado ---
    private String generateUsername(String nombre, String apellido) {
        String n = (nombre != null && !nombre.isBlank()) ? nombre.trim() : "user";
        String a = (apellido != null && !apellido.isBlank()) ? apellido.trim() : "app";
        String base = (String.valueOf(n.charAt(0)) + a)
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "");
        String suffix = String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000));
        return base + suffix;
    }
}