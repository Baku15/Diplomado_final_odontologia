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

    @Override
    public User createUserFromRegistration(RegistrationRequest rr, String username, String rawPassword, String roleName) {
        userRepo.findByUsername(username).ifPresent(u -> { throw new IllegalStateException("Username ya está en uso"); });
        userRepo.findByEmail(rr.getEmail()).ifPresent(u -> { throw new IllegalStateException("Email ya está en uso"); });

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Rol no existe: " + roleName));

        User user = new User();
        user.setUsername(username);
        user.setEmail(rr.getEmail());
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNombres(rr.getNombre());
        user.setApellidos(rr.getApellido());
        user.setOcupacion(rr.getOcupacion());
        user.setZona(rr.getZona());
        user.setDireccion(rr.getDireccion());
        user.setStatus(UserStatus.ACTIVO);
        user.getRoles().add(role);

        return userRepo.save(user);
    }
}
