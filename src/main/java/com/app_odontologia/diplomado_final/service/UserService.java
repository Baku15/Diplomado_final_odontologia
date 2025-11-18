package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.model.entity.RegistrationRequest;
import com.app_odontologia.diplomado_final.model.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    // Método antiguo (un solo rol)
    User createUserFromRegistration(RegistrationRequest rr, String username, String rawPassword, String roleName);

    // NUEVO MÉTODO -> múltiples roles
    User createUserFromRegistration(RegistrationRequest rr, String username, String rawPassword, List<String> roleNames);
}
