// src/main/java/com/app_odontologia/diplomado_final/repository/DoctorScheduleRepository.java
package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    // Para cargar todos los días del doctor ya ordenados
    List<DoctorSchedule> findByDoctorIdOrderByDayOfWeekAsc(Long doctorId);

    // Para limpiar antes de volver a guardar el esquema completo
    void deleteByDoctorId(Long doctorId);
}
