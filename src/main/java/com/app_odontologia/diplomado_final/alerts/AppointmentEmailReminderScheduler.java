package com.app_odontologia.diplomado_final.alerts;

import com.app_odontologia.diplomado_final.dto.AppointmentEmailData;
import com.app_odontologia.diplomado_final.model.entity.Appointment;
import com.app_odontologia.diplomado_final.model.entity.SystemAlert;
import com.app_odontologia.diplomado_final.repository.AppointmentRepository;
import com.app_odontologia.diplomado_final.repository.SystemAlertRepository;
import com.app_odontologia.diplomado_final.service.MailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Transactional
@Component
@RequiredArgsConstructor
public class AppointmentEmailReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final MailService mailService;
    private final SystemAlertRepository systemAlertRepository;

    // ⏱️ Cada 5 minutos
    @Scheduled(fixedDelay = 300_000)
    public void sendPendingReminders() {

        List<AppointmentEmailData> reminders =
                appointmentRepository.findPendingEmailReminders();

        Instant now = Instant.now();

        for (AppointmentEmailData data : reminders) {

            // ⏰ calcular momento del recordatorio
            LocalDateTime appointmentDateTime =
                    LocalDateTime.of(data.date(), data.startTime());

            Instant reminderTime =
                    appointmentDateTime
                            .minusMinutes(1440) // 24h (puedes cambiar luego a data.reminderMinutesBefore)
                            .atZone(ZoneId.systemDefault())
                            .toInstant();

            if (now.isBefore(reminderTime)) {
                continue;
            }

            // 📧 enviar correo
            mailService.sendAppointmentReminderEmail(
                    data.patientEmail(),
                    data.patientFullName(),
                    data.doctorFullName(),
                    data.clinicName(),
                    data.date(),
                    data.startTime()
            );

            // ✅ marcar cita como recordatorio enviado
            appointmentRepository.markEmailSent(
                    data.appointmentId(),
                    now
            );

            // 🔎 cargar la CITA COMPLETA (NO DTO)
            Appointment appointment = appointmentRepository.findById(data.appointmentId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Cita no encontrada al generar alerta de recordatorio"
                    ));

            // 🔔 crear alerta del sistema (VISIBLE PARA EL DOCTOR)
            SystemAlert alert = SystemAlert.builder()
                    .clinic(appointment.getClinic())
                    .patient(appointment.getPatient())
                    .appointmentId(appointment.getId())
                    .consultationId(
                            appointment.getConsultation() != null
                                    ? appointment.getConsultation().getId()
                                    : null
                    )
                    .type(SystemAlert.AlertType.APPOINTMENT)
                    .severity(SystemAlert.AlertSeverity.INFO)
                    .message(
                            "📧 Recordatorio enviado a "
                                    + appointment.getPatient().getGivenName()
                                    + " "
                                    + appointment.getPatient().getFamilyName()
                                    + " ("
                                    + appointment.getPatient().getEmail()
                                    + ")\n"
                                    + "📅 "
                                    + appointment.getDate().format(
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy")
                            )
                                    + " – "
                                    + appointment.getStartTime().format(
                                    DateTimeFormatter.ofPattern("HH:mm")
                            )
                    )
                    .resolved(false)
                    .build();

            systemAlertRepository.save(alert);
        }
    }
}
