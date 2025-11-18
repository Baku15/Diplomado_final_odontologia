package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.model.entity.Role;
import com.app_odontologia.diplomado_final.model.entity.RegistrationRequest;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.model.enums.UserStatus;
import com.app_odontologia.diplomado_final.repository.RoleRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

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
    //   NUEVO MÉTODO — MULTI ROLES (USAR ESTE)
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

        return userRepo.save(user);
    }
}
