package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.*;
import com.app_odontologia.diplomado_final.model.entity.*;
import com.app_odontologia.diplomado_final.model.enums.InvitationStatus;
import com.app_odontologia.diplomado_final.model.enums.UserStatus;
import com.app_odontologia.diplomado_final.repository.*;
import com.app_odontologia.diplomado_final.service.StaffService;
import com.app_odontologia.diplomado_final.service.ActivationService;
import com.app_odontologia.diplomado_final.service.MailService;
import com.app_odontologia.diplomado_final.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
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
    private final ClinicRepository clinicRepository;
    private final DoctorInvitationRepository doctorInvitationRepository;
    private final UserRepository userRepository;
    private final UserService userService;



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
    @Transactional
    public DoctorInvitationDto inviteDoctor(Long clinicId,
                                            InviteDoctorRequestDto request,
                                            String invitedBy) {

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new IllegalArgumentException("Clínica no encontrada"));

        // ¿Ya existe una invitación reciente para ese correo en esta clínica?
        doctorInvitationRepository
                .findTopByClinic_IdAndEmailIgnoreCaseOrderByCreatedAtDesc(clinicId, request.getEmail())
                .ifPresent(inv -> {
                    if (inv.getStatus() == InvitationStatus.PENDING) {
                        throw new IllegalStateException(
                                "Ya existe una invitación pendiente para este correo en esta clínica."
                        );
                    }
                });

        DoctorInvitation invitation = new DoctorInvitation();
        invitation.setClinic(clinic);
        invitation.setFullName(request.getFullName());
        invitation.setEmail(request.getEmail());
        invitation.setPhone(request.getPhone());
        invitation.setSpecialty(request.getSpecialty());
        invitation.setNotes(request.getNotes());

        invitation.setInvitedBy(invitedBy);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setToken(UUID.randomUUID().toString());
        invitation.setCreatedAt(OffsetDateTime.now());
        invitation.setExpiresAt(OffsetDateTime.now().plusDays(7)); // 7 días de vigencia

        DoctorInvitation saved = doctorInvitationRepository.save(invitation);

        // 🔹 Enviar correo de invitación al doctor
        mailService.sendDoctorInvitationEmail(saved);

        return toDto(saved);
    }


    private DoctorInvitationDto toDto(DoctorInvitation inv) {
        DoctorInvitationDto dto = new DoctorInvitationDto();
        dto.setId(inv.getId());
        dto.setClinicId(inv.getClinic().getId());
        dto.setFullName(inv.getFullName());
        dto.setEmail(inv.getEmail());
        dto.setPhone(inv.getPhone());
        dto.setSpecialty(inv.getSpecialty());
        dto.setNotes(inv.getNotes());
        dto.setToken(inv.getToken());
        dto.setStatus(inv.getStatus().name());
        dto.setInvitedBy(inv.getInvitedBy());
        dto.setCreatedAt(inv.getCreatedAt());
        dto.setExpiresAt(inv.getExpiresAt());
        return dto;
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

    @Override
    public DoctorInvitationDto getInvitationByToken(String token) {
        var inv = doctorInvitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitación no encontrada."));

        // si quieres, aquí mismo puedes validar expiración / estado
        if (inv.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("La invitación ya fue utilizada o no está disponible.");
        }

        if (inv.getExpiresAt() != null &&
                inv.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalStateException("La invitación ha expirado.");
        }

        return toDto(inv);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorInvitationStatusDto getDoctorInvitationStatus(String token) {
        var inv = doctorInvitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitación no encontrada."));

        boolean expired = inv.getExpiresAt() != null
                && inv.getExpiresAt().isBefore(java.time.OffsetDateTime.now());

        var dto = new DoctorInvitationStatusDto();

        // 🔹 Nombre de la clínica
        if (inv.getClinic() != null) {
            String nombreComercial = inv.getClinic().getNombreComercial();
            if (nombreComercial == null || nombreComercial.isBlank()) {
                nombreComercial = "Clínica #" + inv.getClinic().getId();
            }
            dto.setClinicName(nombreComercial);
        } else {
            dto.setClinicName(null);
        }

        // 🔹 Email del doctor (obligatorio en tu entidad)
        dto.setDoctorEmail(inv.getEmail());

        // 🔹 Nombre completo del doctor (fullName)
        String fullName = inv.getFullName();
        dto.setDoctorFullName(
                (fullName != null && !fullName.isBlank())
                        ? fullName
                        : null
        );

        // 🔹 Estado + expiración
        dto.setStatus(inv.getStatus() != null ? inv.getStatus().name() : "PENDING");
        dto.setExpired(expired);
        dto.setCreatedAt(inv.getCreatedAt());
        dto.setExpiresAt(inv.getExpiresAt());

        return dto;
    }
    @Override
    @Transactional
    public void registerDoctorFromInvitation(String token,
                                             DoctorInvitationRegisterRequestDto request) {

        DoctorInvitation inv = doctorInvitationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitación no encontrada."));

        // 1) Validar estado y expiración
        if (inv.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException(
                    "La invitación no está en estado PENDING (actual: " + inv.getStatus() + ")."
            );
        }

        boolean expired = inv.getExpiresAt() != null
                && inv.getExpiresAt().isBefore(OffsetDateTime.now());

        if (expired) {
            throw new IllegalStateException("La invitación ha expirado.");
        }

        // 2) Email oficial del doctor (no se permite cambiar)
        String email = inv.getEmail();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException(
                    "Ya existe un usuario registrado con ese correo electrónico."
            );
        }

        // 3) Username: usar el que vino en la invitación o derivar del email
        String username;
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            username = request.getUsername().trim();
        } else {
            // fallback sencillo: parte antes de la @
            int atIndex = email.indexOf('@');
            username = (atIndex > 0) ? email.substring(0, atIndex) : email;
        }

        // Verificar que el username no esté en uso
        userRepository.findByUsername(username).ifPresent(u -> {
            throw new IllegalStateException("Ya existe una cuenta con ese nombre de usuario.");
        });

        // 4) Rol de dentista
        Role dentistRole = roleRepo.findByName("ROLE_DENTIST")
                .orElseThrow(() -> new IllegalStateException("No se encontró el rol ROLE_DENTIST"));

        // 5) Crear el usuario del doctor
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // nombres / apellidos según tu DTO
        user.setNombres(request.getFirstName());
        user.setApellidos(request.getLastName());

        // mapear el teléfono al campo clínico
        user.setTelefonoClinico(request.getPhone());

        // clínica de la invitación
        user.setClinic(inv.getClinic());

        // estado y flags según el flujo nuevo
        user.setStatus(UserStatus.ACTIVE);
        user.setMustChangePassword(false);
        user.setMustCompleteProfile(true); // para que luego entre al wizard de perfil profesional

        // asignar rol de dentista
        user.getRoles().add(dentistRole);

        // opcional: registrar quién lo creó (admin que invitó)
        userRepository.findByUsername(inv.getInvitedBy())
                .or(() -> userRepository.findByEmail(inv.getInvitedBy()))
                .ifPresent(user::setCreatedBy);

        userRepository.save(user);

        // 6) Marcar la invitación como aceptada
        inv.setStatus(InvitationStatus.ACCEPTED);
        doctorInvitationRepository.save(inv);

        // ❌ Ya no creamos ActivationToken ni mandamos correo de activación:
        // el doctor ya tiene su contraseña y puede ir directo a /login.
    }

}
