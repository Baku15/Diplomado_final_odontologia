package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.SystemAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface SystemAlertRepository extends JpaRepository<SystemAlert, Long> {

    List<SystemAlert> findByClinicIdAndResolvedFalseOrderByCreatedAtDesc(Long clinicId);

    long countByClinicIdAndResolvedFalse(Long clinicId);

    List<SystemAlert> findByPatientIdAndResolvedFalse(Long patientId);

    List<SystemAlert> findByClinicIdAndResolvedFalseAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long clinicId,
            Instant start,
            Instant end
    );

}
