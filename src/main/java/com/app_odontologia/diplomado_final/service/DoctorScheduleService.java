// src/main/java/com/app_odontologia/diplomado_final/service/DoctorScheduleService.java
package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.DoctorWeeklyScheduleDto;

public interface DoctorScheduleService {

    DoctorWeeklyScheduleDto getMyWeeklySchedule(String username);

    void saveMyWeeklySchedule(String username, DoctorWeeklyScheduleDto weeklyDto);
}
