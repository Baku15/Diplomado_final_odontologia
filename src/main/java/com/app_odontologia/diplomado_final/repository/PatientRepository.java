// src/main/java/com/app_odontologia/diplomado_final/repository/PatientRepository.java
package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    List<Patient> findByClinicIdAndActiveTrueOrderByFamilyNameAscGivenNameAsc(Long clinicId);

    Optional<Patient> findByIdAndClinicIdAndActiveTrue(Long id, Long clinicId);

    boolean existsByClinicIdAndDocumentTypeAndDocumentNumberAndActiveTrue(
            Long clinicId,
            String documentType,
            String documentNumber
    );

    // NEW helpers (convenientes para las reglas de unicidad/contacto)
    boolean existsByClinicIdAndEmailIgnoreCaseAndActiveTrue(Long clinicId, String email);

    boolean existsByClinicIdAndPhoneMobileAndActiveTrue(Long clinicId, String phoneMobile);

    boolean existsByClinicIdAndUsernameAndActiveTrue(Long clinicId, String username);

    Optional<Patient> findByClinicIdAndEmailIgnoreCase(Long clinicId, String email);

    Page<Patient> findByClinicId(Long clinicId, Pageable pageable);

    Optional<Patient> findByIdAndClinicId(Long id, Long clinicId);

}
