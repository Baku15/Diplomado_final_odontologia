// src/main/java/com/app_odontologia/diplomado_final/dto/DoctorWeeklyScheduleDto.java
package com.app_odontologia.diplomado_final.dto.doctor;

import lombok.Data;

import java.util.List;

@Data
public class DoctorWeeklyScheduleDto {
    private List<DoctorDayScheduleDto> days;
}
