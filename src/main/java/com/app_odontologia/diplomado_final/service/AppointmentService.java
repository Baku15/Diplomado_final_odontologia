package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.dto.appointment.AppointmentDto;
import com.app_odontologia.diplomado_final.dto.appointment.CreateAppointmentRequest;
import com.app_odontologia.diplomado_final.dto.appointment.UpdateAppointmentRequest;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {

    AppointmentDto createAppointment(
            Long clinicId,
            Long patientId,
            Long doctorId,
            CreateAppointmentRequest request
    );

    AppointmentDto createDirectAppointment(
            Long clinicId,
            Long doctorId,
            CreateAppointmentRequest request
    );

    List<AppointmentDto> listDoctorAgenda(
            Long doctorId,
            LocalDate date
    );

    AppointmentDto updateAppointment(
            Long appointmentId,
            UpdateAppointmentRequest request
    );

    AppointmentDto cancelAppointment(Long appointmentId);

    AppointmentDto markNoShow(Long appointmentId);

    AppointmentDto confirmAttendance(Long appointmentId);

    AppointmentDto cancelLate(Long appointmentId, boolean accepted);

    AppointmentDto markSpecialCase(Long appointmentId, String note);

    // ✅ CLÍNICA (MANUAL)
    AppointmentDto completeClinicalAppointment(
            Long clinicId,
            Long patientId,
            Long appointmentId
    );

    // ✅ DIRECTA (MANUAL)
    void completeDirectAppointment(
            Long clinicId,
            Long appointmentId,
            String dentistUsername
    );

    AppointmentDto findByConsultationId(Long consultationId);
}
