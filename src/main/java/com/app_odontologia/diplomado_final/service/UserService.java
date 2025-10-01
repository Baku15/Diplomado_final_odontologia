package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.model.entity.RegistrationRequest;
import com.app_odontologia.diplomado_final.model.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    User createUserFromRegistration(RegistrationRequest rr, String username, String rawPassword, String roleName);
}