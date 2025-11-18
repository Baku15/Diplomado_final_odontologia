package com.app_odontologia.diplomado_final.service;

public interface ActivationService {
    String createActivationToken(Long userId, long ttlHours);     // devuelve token
    void activateAccount(String token, String newPassword);       // activa + set password
}