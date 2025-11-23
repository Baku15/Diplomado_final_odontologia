package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.ActivationToken;
import com.app_odontologia.diplomado_final.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivationTokenRepository extends JpaRepository<ActivationToken, Long> {
    Optional<ActivationToken> findByToken(String token);

    void deleteByUser(User user);

}