package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.model.entity.User;

public interface ActivationService {
    String createActivationToken(Long userId, long ttlHours);     // devuelve token
    void activateAccount(String token, String newPassword);
    void createActivationForUserAndSendEmail(User user);

// activa + set password
}