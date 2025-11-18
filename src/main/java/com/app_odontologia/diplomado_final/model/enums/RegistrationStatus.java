package com.app_odontologia.diplomado_final.model.enums;

public enum RegistrationStatus {
    PENDING_REVIEW,   // recién creada, esperando revisión de admin
    APPROVED,         // aprobada, ya se envió link de activación
    REJECTED,         // rechazada por el admin
    ACTIVATED         }
