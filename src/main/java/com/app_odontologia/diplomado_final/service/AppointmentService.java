package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.appointment.AppointmentDto;
import com.app_odontologia.diplomado_final.dto.appointment.CreateAppointmentRequest;
import com.app_odontologia.diplomado_final.dto.appointment.UpdateAppointmentRequest;
import com.app_odontologia.diplomado_final.model.entity.Appointment;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AppointmentService {

    AppointmentDto createAppointment(
            Long clinicId,
            Long patientId,
            Long doctorId,
            CreateAppointmentRequest request
    );

    List<AppointmentDto> listDoctorAgenda(
            Long doctorId,
            LocalDate date
    );

    // 🔥 NUEVO: cancelar cita
    AppointmentDto cancelAppointment(Long appointmentId);

    // 🔥 NUEVO: marcar no asistencia
    AppointmentDto markNoShow(Long appointmentId);

    AppointmentDto findByConsultationId(Long consultationId);

    AppointmentDto confirmAttendance(Long appointmentId);

    AppointmentDto cancelLate(Long appointmentId, boolean accepted);

    AppointmentDto markSpecialCase(Long appointmentId, String note);

    AppointmentDto completeAppointment(Long appointmentId);

    AppointmentDto createDirectAppointment(
            Long clinicId,
            Long doctorId,
            CreateAppointmentRequest request
    );

    AppointmentDto updateAppointment(
            Long appointmentId,
            UpdateAppointmentRequest request
    );

    AppointmentDto completeDirectAppointment(Long appointmentId);
    void completeDirectAppointment(
            Long clinicId,
            Long appointmentId,
            String dentistUsername
    );


}
