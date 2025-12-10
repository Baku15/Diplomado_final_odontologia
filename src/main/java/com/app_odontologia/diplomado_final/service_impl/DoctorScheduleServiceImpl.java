// src/main/java/com/app_odontologia/diplomado_final/service_impl/DoctorScheduleServiceImpl.java
package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.dto.doctor.DoctorDayScheduleDto;
import com.app_odontologia.diplomado_final.dto.doctor.DoctorWeeklyScheduleDto;
import com.app_odontologia.diplomado_final.model.entity.ClinicRoom;
import com.app_odontologia.diplomado_final.model.entity.DoctorProfile;
import com.app_odontologia.diplomado_final.model.entity.DoctorSchedule;
import com.app_odontologia.diplomado_final.model.entity.User;
import com.app_odontologia.diplomado_final.repository.DoctorProfileRepository;
import com.app_odontologia.diplomado_final.repository.DoctorScheduleRepository;
import com.app_odontologia.diplomado_final.repository.UserRepository;
import com.app_odontologia.diplomado_final.service.DoctorScheduleService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DoctorScheduleServiceImpl implements DoctorScheduleService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorScheduleRepository doctorScheduleRepository;

    public DoctorScheduleServiceImpl(UserRepository userRepository,
                                     DoctorProfileRepository doctorProfileRepository,
                                     DoctorScheduleRepository doctorScheduleRepository) {
        this.userRepository = userRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.doctorScheduleRepository = doctorScheduleRepository;
    }

    private User getDoctorUserOrThrow(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + username));
    }

    @Override
    @Transactional
    public DoctorWeeklyScheduleDto getMyWeeklySchedule(String username) {
        User doctor = getDoctorUserOrThrow(username);

        List<DoctorSchedule> schedules =
                doctorScheduleRepository.findByDoctorIdOrderByDayOfWeekAsc(doctor.getId());

        List<DoctorDayScheduleDto> days = new ArrayList<>();

        for (int d = 1; d <= 7; d++) {
            final int day = d;

            DoctorDayScheduleDto dto = new DoctorDayScheduleDto();
            dto.setDayOfWeek(day);

            DoctorSchedule found = schedules.stream()
                    .filter(s -> s.getDayOfWeek() == day && Boolean.TRUE.equals(s.getActive()))
                    .findFirst()
                    .orElse(null);

            if (found == null) {
                dto.setWorking(false);
            } else {
                dto.setWorking(true);
                dto.setStartTime(found.getStartTime().toString());
                dto.setEndTime(found.getEndTime().toString());
                dto.setGiveBreak(Boolean.TRUE.equals(found.getHasBreak()));
                dto.setChairs(found.getChairs());

                if (Boolean.TRUE.equals(found.getHasBreak())) {
                    dto.setBreakStart(
                            found.getBreakStart() != null ? found.getBreakStart().toString() : null
                    );
                    dto.setBreakEnd(
                            found.getBreakEnd() != null ? found.getBreakEnd().toString() : null
                    );
                }
            }

            days.add(dto);
        }

        days.sort(Comparator.comparingInt(DoctorDayScheduleDto::getDayOfWeek));

        DoctorWeeklyScheduleDto result = new DoctorWeeklyScheduleDto();
        result.setDays(days);
        return result;
    }

    @Override
    @Transactional
    public void saveMyWeeklySchedule(String username, DoctorWeeklyScheduleDto weeklyDto) {
        User doctor = getDoctorUserOrThrow(username);

        DoctorProfile profile = doctorProfileRepository.findByUser(doctor)
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene perfil de doctor configurado."));

        ClinicRoom primaryRoom = profile.getPrimaryRoom();
        if (primaryRoom == null) {
            throw new IllegalStateException("El perfil del doctor no tiene consultorio principal (primaryRoom) asignado.");
        }

        // 1) Borramos horarios anteriores de ese doctor
        doctorScheduleRepository.deleteAllByDoctorId(doctor.getId());

        // 2) Si no hay nada que guardar, salimos
        if (weeklyDto == null || weeklyDto.getDays() == null) {
            return;
        }

        // 3) Insertamos según lo que viene del formulario
        for (DoctorDayScheduleDto dto : weeklyDto.getDays()) {
            if (!dto.isWorking()) {
                continue; // no atiende ese día
            }

            DoctorSchedule s = new DoctorSchedule();
            s.setDoctor(doctor);
            s.setRoom(primaryRoom);
            s.setDayOfWeek(dto.getDayOfWeek());

            s.setStartTime(LocalTime.parse(dto.getStartTime()));
            s.setEndTime(LocalTime.parse(dto.getEndTime()));
            s.setChairs(dto.getChairs() != null ? dto.getChairs() : 1);

            boolean giveBreak = dto.isGiveBreak();
            s.setHasBreak(giveBreak);

            if (giveBreak && dto.getBreakStart() != null && dto.getBreakEnd() != null) {
                s.setBreakStart(LocalTime.parse(dto.getBreakStart()));
                s.setBreakEnd(LocalTime.parse(dto.getBreakEnd()));
            }

            s.setActive(true);

            doctorScheduleRepository.save(s);
        }
    }

    @Override
    @Transactional
    public boolean hasAnySchedule(String username) {
        User doctor = getDoctorUserOrThrow(username);
        return doctorScheduleRepository.existsByDoctorId(doctor.getId());
    }
}
