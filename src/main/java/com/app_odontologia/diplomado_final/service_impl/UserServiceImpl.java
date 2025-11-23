package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.CompleteProfileDto;
import com.app_odontologia.diplomado_final.dto.CreateStaffDto;
import com.app_odontologia.diplomado_final.dto.UpdateStaffDto;
import com.app_odontologia.diplomado_final.dto.UserMeDto;
import com.app_odontologia.diplomado_final.model.entity.Clinic;
import com.app_odontologia.diplomado_final.model.entity.RegistrationRequest;
import com.app_odontologia.diplomado_final.model.entity.Role;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.model.enums.UserStatus;
import com.app_odontologia.diplomado_final.repository.ClinicRepository;
import com.app_odontologia.diplomado_final.repository.RoleRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.ActivationService;
import com.app_odontologia.diplomado_final.service.MailService;
import com.app_odontologia.diplomado_final.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final ClinicRepository clinicRepo;
    private final MailService mailService;
    private final ActivationService activationService;

    // -------------------------
    // UserDetails lookup
    // -------------------------
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Permite login por username o por email
        return userRepo.findByUsername(username)
                .map(u -> (UserDetails) u)
                .or(() -> userRepo.findByEmail(username).map(u -> (UserDetails) u))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    // ============================
    //   MÉTODO ANTIGUO (compatible)
    // ============================
    @Override
    public User createUserFromRegistration(RegistrationRequest rr, String username, String rawPassword, String roleName) {
        return createUserFromRegistration(rr, username, rawPassword, List.of(roleName));
    }

    // ==========================================
    //   NUEVO MÉTODO — MULTI ROLES
    // ==========================================
    @Override
    public User createUserFromRegistration(
            RegistrationRequest rr,
            String username,
            String rawPassword,
            List<String> roleNames
    ) {
        userRepo.findByUsername(username).ifPresent(u -> {
            throw new IllegalStateException("Username ya está en uso");
        });

        userRepo.findByEmail(rr.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("Email ya está en uso");
        });

        User user = new User();
        user.setUsername(username);
        user.setEmail(rr.getEmail());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNombres(rr.getNombre());
        user.setApellidos(rr.getApellido());
        user.setOcupacion(rr.getOcupacion());
        user.setZona(rr.getZona());
        user.setDireccion(rr.getDireccion());
        user.setStatus(UserStatus.PENDING_ACTIVATION);

        // Agregar cada rol
        for (String roleName : roleNames) {
            Role role = roleRepo.findByName(roleName)
                    .orElseThrow(() -> new IllegalStateException("Rol no existe: " + roleName));
            user.getRoles().add(role);
        }

        // ⭐ AQUÍ EL CAMBIO: usar directamente isDentist()
        boolean isDentist = rr.isDentist();
        user.setMustCompleteProfile(isDentist);

        return userRepo.save(user);
    }

    // =====================================================
    //   Crear usuario de staff asociado a clínica
    // =====================================================
    @Override
    public User createUserForClinic(CreateStaffDto dto, List<String> roleNames, Long clinicId, String createdByUsername) {
        if (dto == null) throw new IllegalArgumentException("CreateStaffDto es requerido");
        if (roleNames == null || roleNames.isEmpty()) throw new IllegalArgumentException("roleNames no puede ser vacío");

        // Email único
        userRepo.findByEmail(dto.getEmail()).ifPresent(u -> {
            throw new IllegalStateException("Email ya está en uso");
        });

        // Determinar username
        String username;
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            final String provided = dto.getUsername().trim();
            userRepo.findByUsername(provided).ifPresent(u -> {
                throw new IllegalStateException("Username ya está en uso");
            });
            username = provided;
        } else {
            username = generateUsername(dto.getNombre(), dto.getApellido());
            int tries = 0;
            while (userRepo.findByUsername(username).isPresent() && tries++ < 20) {
                username = generateUsername(dto.getNombre(), dto.getApellido()) + ThreadLocalRandom.current().nextInt(0, 1000);
            }
            if (userRepo.findByUsername(username).isPresent()) {
                throw new IllegalStateException("No se pudo generar un username único, intenta con uno personalizado.");
            }
        }

        // Obtener clínica
        Clinic clinic = clinicRepo.findById(clinicId)
                .orElseThrow(() -> new IllegalStateException("Clinic no encontrada: " + clinicId));

        // Crear user
        User user = new User();
        user.setUsername(username);
        user.setEmail(dto.getEmail());

        String tmpPass = generateTemporaryPassword();
        user.setPassword(passwordEncoder.encode(tmpPass));
        user.setNombres(dto.getNombre());
        user.setApellidos(dto.getApellido());
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            user.setZona(dto.getPhone()); // ajustar si luego tienes campo phone dedicado
        }
        user.setStatus(UserStatus.ACTIVE);
        user.setMustChangePassword(true);
        user.setClinic(clinic);

        // Asignar roles
        for (String rn : roleNames) {
            Role role = roleRepo.findByName(rn)
                    .orElseThrow(() -> new IllegalStateException("Rol no existe: " + rn));
            user.getRoles().add(role);
        }

        // ⭐ Usuarios de staff NO usan wizard de completar perfil.
        user.setMustCompleteProfile(false);

        // createdBy opcional
        if (createdByUsername != null) {
            userRepo.findByUsername(createdByUsername).ifPresent(user::setCreatedBy);
        }

        User saved = userRepo.save(user);

        // Enviar password temporal (opcional)
        try {
            mailService.sendActivationEmail(saved.getEmail(), tmpPass);
        } catch (Exception ex) {
            System.err.println("Warning: no se pudo enviar password temporal por correo: " + ex.getMessage());
        }

        return saved;
    }

    // ----------------------------
    //   Actualizar usuario de clínica
    // ----------------------------
    @Override
    public User updateUserForClinic(Long clinicId, Long userId, UpdateStaffDto dto, String actingUsername) {
        if (dto == null) throw new IllegalArgumentException("UpdateStaffDto requerido");

        User target = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + userId));

        if (target.getClinic() == null || !target.getClinic().getId().equals(clinicId)) {
            throw new IllegalStateException("El usuario no pertenece a la clínica indicada.");
        }

        // Email
        if (!target.getEmail().equals(dto.getEmail())) {
            userRepo.findByEmail(dto.getEmail()).ifPresent(u -> {
                if (!u.getId().equals(userId)) throw new IllegalStateException("Email ya está en uso");
            });
            target.setEmail(dto.getEmail());
        }

        // Username
        if (dto.getUsername() != null && !dto.getUsername().isBlank()
                && !dto.getUsername().equals(target.getUsername())) {
            userRepo.findByUsername(dto.getUsername()).ifPresent(u -> {
                if (!u.getId().equals(userId)) throw new IllegalStateException("Username ya está en uso");
            });
            target.setUsername(dto.getUsername());
        }

        target.setNombres(dto.getNombre());
        target.setApellidos(dto.getApellido());

        if (dto.getPhone() != null) {
            target.setZona(dto.getPhone()); // ajustar si luego tienes campo phone
        }

        // Roles
        if (dto.getRoleNames() != null) {
            target.getRoles().clear();
            for (String rn : dto.getRoleNames()) {
                Role role = roleRepo.findByName(rn)
                        .orElseThrow(() -> new IllegalStateException("Rol no existe: " + rn));
                target.getRoles().add(role);
            }
        }

        // Estado
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                UserStatus ns = UserStatus.valueOf(dto.getStatus());
                target.setStatus(ns);
            } catch (IllegalArgumentException ex) {
                throw new IllegalStateException("Estado no válido: " + dto.getStatus());
            }
        }

        return userRepo.save(target);
    }

    // ----------------------------
    //   Cambiar estado de usuario
    // ----------------------------
    @Override
    public void setUserStatus(Long userId, String status, String actingUsername) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + userId));
        try {
            UserStatus ns = UserStatus.valueOf(status);
            u.setStatus(ns);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Estado no válido: " + status);
        }
        userRepo.save(u);
    }

    // ----------------------------
    //   Completar perfil clínico (wizard)
    // ----------------------------
    @Override
    public User completeProfile(Long userId, CompleteProfileDto dto, String actingUsername) {
        if (dto == null) throw new IllegalArgumentException("CompleteProfileDto requerido");

        User target = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + userId));

        if (dto.getEspecialidad() != null) target.setEspecialidad(dto.getEspecialidad());
        if (dto.getMatricula() != null) target.setMatricula(dto.getMatricula());
        if (dto.getTelefono() != null) target.setTelefonoClinico(dto.getTelefono());
        if (dto.getBio() != null) target.setBioClinica(dto.getBio());

        // Al terminar wizard, ya no debe volver a verlo
        target.setMustCompleteProfile(false);

        return userRepo.save(target);
    }

    @Override
    public User completeOwnProfile(Long userId, CompleteProfileDto dto) {
        return completeProfile(userId, dto, null);
    }

    // ----------------------------
    //   Helpers
    // ----------------------------
    private String generateTemporaryPassword() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
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

    @Override
    public UserMeDto getUserMe(Long userId) {
        User u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        UserMeDto dto = new UserMeDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setMustCompleteProfile(u.getMustCompleteProfile());

        // roles como antes
        dto.setRoles(
                u.getRoles().stream()
                        .map(Role::getName)
                        .toList()
        );

        // clinicId si existe
        if (u.getClinic() != null) {
            dto.setClinicId(u.getClinic().getId());
        }

        // ---- NUEVOS CAMPOS NOMBRE FHIR-FRIENDLY ----
        dto.setGivenName(u.getNombres());
        dto.setFamilyName(u.getApellidos());

        String nombres = u.getNombres() != null ? u.getNombres().trim() : "";
        String apellidos = u.getApellidos() != null ? u.getApellidos().trim() : "";
        String fullName = (nombres + " " + apellidos).trim();

        dto.setFullName(fullName.isEmpty() ? null : fullName);

        // ---- FLAGS DERIVADOS DE ROLES ----
        boolean isDentist = u.getRoles().stream()
                .anyMatch(r -> "ROLE_DENTIST".equalsIgnoreCase(r.getName()));

        boolean isClinicAdmin = u.getRoles().stream()
                .anyMatch(r -> "ROLE_CLINIC_ADMIN".equalsIgnoreCase(r.getName()));

        dto.setDentist(isDentist);
        dto.setClinicAdmin(isClinicAdmin);

        return dto;
    }
}
