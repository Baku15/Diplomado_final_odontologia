package com.app_odontologia.diplomado_final.util;

import com.app_odontologia.diplomado_final.dto.RegistrationRequestViewDto;
import com.app_odontologia.diplomado_final.model.entity.RegistrationRequest;

public final class MappingUtils {
    private MappingUtils() {}

    public static RegistrationRequestViewDto toViewDto(RegistrationRequest rr) {
        RegistrationRequestViewDto dto = new RegistrationRequestViewDto();
        dto.setId(rr.getId());
        dto.setNombre(rr.getNombre());
        dto.setApellido(rr.getApellido());
        dto.setEmail(rr.getEmail());
        dto.setOcupacion(rr.getOcupacion());
        dto.setZona(rr.getZona());
        dto.setDireccion(rr.getDireccion());
        dto.setStatus(rr.getStatus());
        dto.setCreatedAt(rr.getCreatedAt());

        // Asegúrate que la entidad RegistrationRequest tenga isDentist()
        dto.setDentist(rr.isDentist());  // getter será isDentist() tras renombrar field a 'dentist'

        return dto;
    }
}
