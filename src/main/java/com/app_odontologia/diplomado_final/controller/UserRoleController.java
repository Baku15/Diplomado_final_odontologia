package com.app_odontologia.diplomado_final.controller;

import com.app_odontologia.diplomado_final.dto.UpdateRolesDto;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.service.UserService;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserRoleController {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final UserService userService; // we'll add updateRoles in service implementation

    @PatchMapping("/users/me/roles")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateMyRoles(@RequestBody UpdateRolesDto dto, Authentication auth) {
        Object principal = auth.getPrincipal();
        if (!(principal instanceof User)) return ResponseEntity.status(403).body("Principal no válido");
        User me = (User) principal;

        // Business rule: allow self-add of ROLE_DENTIST only if user has ROLE_CLINIC_ADMIN
        List<String> add = dto.getAdd();
        if (add != null && add.contains("ROLE_DENTIST") && !me.hasRole("ROLE_CLINIC_ADMIN")) {
            return ResponseEntity.status(403).body("Solo administradores de clínica pueden asignarse ROLE_DENTIST");
        }

        // Delegate to UserService-like method (we'll implement helper below)
        userServiceUpdateRoles(me.getId(), dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/admin/users/{userId}/roles")
    @PreAuthorize("hasRole('SUPERUSER')")
    public ResponseEntity<?> updateRolesByAdmin(@PathVariable Long userId, @RequestBody UpdateRolesDto dto) {
        userServiceUpdateRoles(userId, dto);
        return ResponseEntity.ok().build();
    }

    // Helper minimal implementation here to avoid changing UserService signature:
    private void userServiceUpdateRoles(Long userId, UpdateRolesDto dto) {
        var user = userRepo.findById(userId).orElseThrow();
        if (dto.getAdd() != null) {
            dto.getAdd().forEach(rn -> {
                var role = roleRepo.findByName(rn).orElseThrow(() -> new IllegalStateException("Rol no existe: " + rn));
                user.getRoles().add(role);
            });
        }
        if (dto.getRemove() != null) {
            dto.getRemove().forEach(rn -> user.getRoles().removeIf(r -> r.getName().equals(rn)));
        }
        userRepo.save(user);
    }
}
