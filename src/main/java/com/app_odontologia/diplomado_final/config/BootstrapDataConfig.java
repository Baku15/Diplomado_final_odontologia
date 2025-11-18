package com.app_odontologia.diplomado_final.config;

import com.app_odontologia.diplomado_final.model.entity.Role;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.model.enums.UserStatus;
import com.app_odontologia.diplomado_final.repository.RoleRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

@Configuration
@RequiredArgsConstructor
public class BootstrapDataConfig {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            // 1) Roles base del sistema
            String[] baseRoles = {
                    "ROLE_SUPERUSER",
                    "ROLE_CLINIC_ADMIN", //  rol para dueños de clínica
                    "ROLE_DENTIST",
                    "ROLE_ASSISTANT",
                    "ROLE_PATIENT"
            };

            for (String r : baseRoles) {
                roleRepository.findByName(r).orElseGet(() -> {
                    Role role = new Role();
                    role.setName(r);
                    return roleRepository.save(role);
                });
            }

            // 2) Usuario SUPERADMIN global de la plataforma
            final String adminUsername = "superadmin";
            final String adminEmail = "superadmin@odontoweb.local";
            final String adminRawPass = "Admin123!";
            if (userRepository.findByUsername(adminUsername).isEmpty()) {
                Role roleSuper = roleRepository.findByName("ROLE_SUPERUSER")
                        .orElseThrow();

                User admin = new User();
                admin.setUsername(adminUsername);
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminRawPass));
                admin.setNombres("Super");
                admin.setApellidos("Admin");

                // IMPORTANTE: superadmin debe estar ACTIVO para poder loguearse
                admin.setStatus(UserStatus.ACTIVE);

                admin.setRoles(new HashSet<>());
                admin.getRoles().add(roleSuper);

                userRepository.save(admin);
                System.out.println(">>> SUPERUSER creado: " + adminUsername + " / " + adminEmail);
            }
        };
    }
}
