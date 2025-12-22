package com.app_odontologia.diplomado_final.dto;

import com.app_odontologia.diplomado_final.model.entity.SystemAlert;
import java.time.Instant;

public record SystemAlertDto(
        Long id,
        String type,
        String severity,
        String message,
        Instant createdAt
) {
    public static SystemAlertDto fromEntity(SystemAlert a) {
        return new SystemAlertDto(
                a.getId(),
                a.getType().name(),
                a.getSeverity().name(),
                a.getMessage(),
                a.getCreatedAt()
        );
    }
}
