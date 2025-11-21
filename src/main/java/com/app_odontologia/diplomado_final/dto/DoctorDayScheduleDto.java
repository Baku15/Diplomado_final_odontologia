// src/main/java/com/app_odontologia/diplomado_final/dto/DoctorDayScheduleDto.java
package com.app_odontologia.diplomado_final.dto;

import lombok.Data;

@Data
public class DoctorDayScheduleDto {
    /**
     * 1 = Lunes ... 7 = Domingo
     */
    private int dayOfWeek;

    /**
     * Si no trabaja ese día, working = false
     */
    private boolean working;

    private String startTime;   // "09:00"
    private String endTime;     // "18:00"

    private boolean giveBreak;  // ¿da descanso?
    private String breakStart;  // "14:00"
    private String breakEnd;    // "15:00"

    private Integer chairs = 1; // sillones simultáneos
}
