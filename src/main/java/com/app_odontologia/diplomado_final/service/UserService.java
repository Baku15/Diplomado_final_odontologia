package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.CompleteProfileDto;
import com.app_odontologia.diplomado_final.dto.CreateStaffDto;
import com.app_odontologia.diplomado_final.dto.UpdateStaffDto;
import com.app_odontologia.diplomado_final.dto.UserMeDto;
import com.app_odontologia.diplomado_final.model.entity.RegistrationRequest;
import com.app_odontologia.diplomado_final.model.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService extends UserDetailsService {

    User createUserFromRegistration(RegistrationRequest rr, String username, String rawPassword, String roleName);
    User createUserFromRegistration(RegistrationRequest rr, String username, String rawPassword, List<String> roleNames);

    User createUserForClinic(CreateStaffDto dto, List<String> roleNames, Long clinicId, String createdByUsername);
    User updateUserForClinic(Long clinicId, Long userId, UpdateStaffDto dto, String actingUsername);

    void setUserStatus(Long userId, String status, String actingUsername);

    // 🌟 NUEVO
    UserMeDto getUserMe(Long userId);

    // Para que el usuario complete su propio perfil
    User completeOwnProfile(Long userId, CompleteProfileDto dto);

    // Para que admin complete el perfil de otro
    User completeProfile(Long userId, CompleteProfileDto dto, String actingUsername);
}
