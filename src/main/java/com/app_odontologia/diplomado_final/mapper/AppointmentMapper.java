package com.app_odontologia.diplomado_final.mapper;

import com.app_odontologia.diplomado_final.dto.appointment.AppointmentDto;
import com.app_odontologia.diplomado_final.model.entity.Appointment;

public class AppointmentMapper {

    private AppointmentMapper() {}

    public static AppointmentDto toDto(Appointment a) {
        return AppointmentDto.builder()
                .id(a.getId())
                .clinicId(a.getClinic().getId())
                .patientId(a.getPatient().getId())
                .doctorId(a.getDoctor().getId())
                .doctorName(
                        a.getDoctor().getNombres() + " " + a.getDoctor().getApellidos()
                )
                .date(a.getDate())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .durationMinutes(a.getDurationMinutes())
                .status(a.getStatus().name())
                .reason(a.getReason())
                .sendWhatsapp(a.getSendWhatsapp())
                .sendEmail(a.getSendEmail())
                .reminderMinutesBefore(a.getReminderMinutesBefore())
                .build();
    }
}
