package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.ClinicRoom;
import com.app_odontologia.diplomado_final.model.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClinicRoomRepository extends JpaRepository<ClinicRoom, Long> {

    List<ClinicRoom> findByClinicAndActiveTrue(Clinic clinic);

    Optional<ClinicRoom> findByIdAndClinicId(Long id, Long clinicId); // 👈 este

}
