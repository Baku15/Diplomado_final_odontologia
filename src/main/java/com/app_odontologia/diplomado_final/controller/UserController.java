// src/main/java/com/app_odontologia/diplomado_final/controller/UserController.java
package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.CompleteProfileDto;
import com.app_odontologia.diplomado_final.dto.UserMeDto;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * Maneja GET /api/me para ambos tipos de principal:
     * - cuando authentication.getPrincipal() es la entidad User (login por session/DB)
     * - cuando principal es un Jwt (bearer token / resource server)
     */
    @GetMapping("/me")
    public ResponseEntity<UserMeDto> me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();

        // 1) Si principal es la entidad User (login clásico / session)
        if (principal instanceof User) {
            User u = (User) principal;
            return ResponseEntity.ok(mapToUserMeDto(u));
        }

        // 2) Si principal es Jwt (token)
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;

            // 2.a Preferimos user_id claim (si existe)
            Long userIdFromClaim = null;
            Object uidClaim = jwt.getClaims().get("user_id");
            if (uidClaim instanceof Number) {
                userIdFromClaim = ((Number) uidClaim).longValue();
            } else if (uidClaim instanceof String) {
                try { userIdFromClaim = Long.valueOf((String) uidClaim); } catch (Exception ignored) {}
            }

            if (userIdFromClaim != null) {
                Optional<User> opt = userRepository.findById(userIdFromClaim);
                if (opt.isPresent()) return ResponseEntity.ok(mapToUserMeDto(opt.get()));
            }

            // 2.b Si no hay user_id, probamos con subject (sub) -> username o email
            String sub = Optional.ofNullable(jwt.getSubject()).orElse("");
            if (!sub.isBlank()) {
                Optional<User> optUser = userRepository.findByUsername(sub);
                if (optUser.isEmpty()) optUser = userRepository.findByEmail(sub);
                if (optUser.isPresent()) return ResponseEntity.ok(mapToUserMeDto(optUser.get()));
            }

            // 2.c Fallback: construimos DTO a partir de claims (sin ID en BD)
            UserMeDto dto = new UserMeDto();
            dto.setId(null);

            // username/email claims (si existen)
            String usernameClaim = jwt.getClaimAsString("username");
            dto.setUsername(usernameClaim != null ? usernameClaim : Optional.ofNullable(jwt.getSubject()).orElse(null));
            dto.setEmail(jwt.getClaimAsString("email"));

            // roles claim puede ser lista o string
            Object rolesClaim = jwt.getClaims().get("roles");
            List<String> roles = new ArrayList<>();
            if (rolesClaim instanceof Collection) {
                ((Collection<?>) rolesClaim).forEach(r -> roles.add(String.valueOf(r)));
            } else if (rolesClaim != null) {
                roles.add(String.valueOf(rolesClaim));
            }
            dto.setRoles(roles);

            // clinicId claim
            Object clinicClaim = jwt.getClaims().get("clinic_id");
            if (clinicClaim instanceof Number) {
                dto.setClinicId(((Number) clinicClaim).longValue());
            } else if (clinicClaim != null) {
                try { dto.setClinicId(Long.parseLong(String.valueOf(clinicClaim))); } catch (Exception ignored) {}
            }

            // mustCompleteProfile claim
            Object mustComplete = jwt.getClaims().get("mustCompleteProfile");
            if (mustComplete instanceof Boolean) {
                dto.setMustCompleteProfile((Boolean) mustComplete);
            } else if (mustComplete != null) {
                dto.setMustCompleteProfile(Boolean.parseBoolean(String.valueOf(mustComplete)));
            } else {
                dto.setMustCompleteProfile(false);
            }

            return ResponseEntity.ok(dto);
        }

        // 3) Otros tipos de principal (ej. String) -> intentar usar name y buscar en BD
        String name = authentication.getName();
        if (name != null && !name.isBlank()) {
            Optional<User> optUser = userRepository.findByUsername(name);
            if (optUser.isEmpty()) optUser = userRepository.findByEmail(name);
            if (optUser.isPresent()) return ResponseEntity.ok(mapToUserMeDto(optUser.get()));
        }

        // No se pudo resolver
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @PostMapping("/users/me/complete-profile")
    public ResponseEntity<?> completeProfile(@RequestBody CompleteProfileDto dto, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = authentication.getPrincipal();
        Long userId = null;

        // 1) Si principal es User
        if (principal instanceof User) {
            userId = ((User) principal).getId();
        }
        // 2) Si principal es Jwt, preferimos user_id claim
        else if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            Object uidClaim = jwt.getClaims().get("user_id");
            if (uidClaim instanceof Number) {
                userId = ((Number) uidClaim).longValue();
            } else if (uidClaim instanceof String) {
                try { userId = Long.valueOf((String) uidClaim); } catch (Exception ignored) {}
            }

            // si no viene user_id, intentar por sub -> username/email
            if (userId == null) {
                String sub = Optional.ofNullable(jwt.getSubject()).orElse("");
                if (!sub.isBlank()) {
                    Optional<User> optUser = userRepository.findByUsername(sub);
                    if (optUser.isEmpty()) optUser = userRepository.findByEmail(sub);
                    if (optUser.isPresent()) userId = optUser.get().getId();
                }
            }
        }
        // 3) Fallback: usar authentication.getName()
        else {
            String name = authentication.getName();
            if (name != null && !name.isBlank()) {
                Optional<User> optUser = userRepository.findByUsername(name);
                if (optUser.isEmpty()) optUser = userRepository.findByEmail(name);
                if (optUser.isPresent()) userId = optUser.get().getId();
            }
        }

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado para completar perfil");
        }

        userService.completeOwnProfile(userId, dto);
        return ResponseEntity.ok().build();
    }

    // -----------------------
    // Helpers
    // -----------------------
    private UserMeDto mapToUserMeDto(User u) {
        UserMeDto dto = new UserMeDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        List<String> roles = u.getRoles() == null ? List.of() :
                u.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList());
        dto.setRoles(roles);
        dto.setClinicId(u.getClinic() != null ? u.getClinic().getId() : null);
        dto.setMustCompleteProfile(u.getMustCompleteProfile() != null ? u.getMustCompleteProfile() : false);
        return dto;
    }
}