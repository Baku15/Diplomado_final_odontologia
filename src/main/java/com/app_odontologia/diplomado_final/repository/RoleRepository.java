package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}