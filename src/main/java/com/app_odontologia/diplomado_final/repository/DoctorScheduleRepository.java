package com.app_odontologia.diplomado_final.repository;

import com.app_odontologia.diplomado_final.model.entity.DoctorSchedule;
import com.app_odontologia.diplomado_final.model.entity.DoctorProfile;
import com.app_odontologia.diplomado_final.model.entity.ClinicRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, Long> {

    List<DoctorSchedule> findByDoctorAndActiveTrue(DoctorProfile doctor);

    List<DoctorSchedule> findByDoctorAndRoomAndDayOfWeekAndActiveTrue(
            DoctorProfile doctor,
            ClinicRoom room,
            DayOfWeek dayOfWeek
    );
}

