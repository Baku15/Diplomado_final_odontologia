package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.model.entity.ActivationToken;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.model.enums.UserStatus;
import com.app_odontologia.diplomado_final.repository.ActivationTokenRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.ActivationService;
import com.app_odontologia.diplomado_final.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final MailService mailService;

    // URL base del front: http://localhost:4200 en dev
    @Value("${app.frontend-base-url:http://localhost:4200}")
    private String frontendBaseUrl;

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

    @Override
    public void createActivationForUserAndSendEmail(User user) {
        // 1) borrar tokens anteriores de ese usuario
        tokenRepo.deleteByUser(user);

        // 2) crear un nuevo token válido por 72 horas (3 días, ajustable)
        String token = createActivationToken(user.getId(), 72);

        // 3) construir el enlace para el front
        String activationLink = frontendBaseUrl + "/activar?token=" + token;

        // 4) enviar correo usando tu MailService
        mailService.sendActivationEmail(user.getEmail(), activationLink);
    }
}
