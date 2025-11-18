package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.model.entity.ActivationToken;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.model.enums.UserStatus;
import com.app_odontologia.diplomado_final.repository.ActivationTokenRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.ActivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivationServiceImpl implements ActivationService {

    private final ActivationTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String createActivationToken(Long userId, long ttlHours) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        ActivationToken t = new ActivationToken();
        t.setToken(UUID.randomUUID().toString());
        t.setUser(user);
        t.setExpiresAt(Instant.now().plusSeconds(ttlHours * 3600));
        t.setUsed(false);

        tokenRepo.save(t);
        return t.getToken();
    }

    @Override
    public void activateAccount(String token, String newPassword) {
        ActivationToken t = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (t.isUsed() || t.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalStateException("Token expirado o ya utilizado");
        }

        User u = t.getUser();
        u.setPassword(passwordEncoder.encode(newPassword));

        // 🔹 Ahora el usuario pasa a ACTIVE
        u.setStatus(UserStatus.ACTIVE);
        u.setMustChangePassword(false);
        userRepo.save(u);

        // 🔹 Marca el token como usado
        t.setUsed(true);
        tokenRepo.save(t);
    }
}
