// src/main/java/com/app_odontologia/diplomado_final/repository/DoctorScheduleRepository.java
package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    // 🔹 Cargar todos los días del doctor ya ordenados (usando doctor.id explícito)
    @Query("SELECT ds FROM DoctorSchedule ds " +
            "WHERE ds.doctor.id = :doctorId " +
            "ORDER BY ds.dayOfWeek ASC")
    List<DoctorSchedule> findByDoctorIdOrderByDayOfWeekAsc(@Param("doctorId") Long doctorId);

    // 🔹 Borrar todos los horarios de un doctor
    @Modifying
    @Query("DELETE FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId")
    void deleteAllByDoctorId(@Param("doctorId") Long doctorId);

    // 🔹 Saber si tiene al menos un horario
    @Query("SELECT (COUNT(ds) > 0) FROM DoctorSchedule ds WHERE ds.doctor.id = :doctorId")
    boolean existsByDoctorId(@Param("doctorId") Long doctorId);
}
