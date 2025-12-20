package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.DentalProcedure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DentalProcedureRepository extends JpaRepository<DentalProcedure, Long> {

    // 🔥 PROCEDIMIENTOS CREADOS EN UNA CONSULTA
    List<DentalProcedure> findByCreatedInConsultationId(Long consultationId);

    // 🔥 (opcional, para el futuro)
    List<DentalProcedure> findByCompletedInConsultationId(Long consultationId);

    long countByChartClinicId(Long clinicId);

}
