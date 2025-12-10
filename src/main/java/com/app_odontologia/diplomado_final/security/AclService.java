package com.app_odontologia.diplomado_final.security;

import com.app_odontologia.diplomado_final.repository.PatientRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service("aclService")
public class AclService {

    private final PatientRepository patientRepository;

    public AclService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public boolean canAccessPatient(Long patientId, Authentication authentication) {
        if (authentication == null) return false;
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt)) {
            // si el principal no es Jwt (p. ej. introspección opaca), denegar por defecto
            return false;
        }
        Jwt jwt = (Jwt) principal;
        Object clinicClaim = jwt.getClaim("clinic_id");
        if (clinicClaim == null) return false;
        Long clinicId;
        try {
            clinicId = Long.valueOf(clinicClaim.toString());
        } catch (Exception e) {
            return false;
        }

        return patientRepository.findById(patientId)
                .map(p -> p.getClinic() != null && p.getClinic().getId() != null && p.getClinic().getId().equals(clinicId))
                .orElse(false);
    }
}
