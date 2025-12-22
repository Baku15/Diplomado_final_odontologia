package com.app_odontologia.diplomado_final.alerts;

import com.app_odontologia.diplomado_final.dto.AppointmentEmailData;
import com.app_odontologia.diplomado_final.model.entity.Appointment;
import com.app_odontologia.diplomado_final.repository.AppointmentRepository;
import com.app_odontologia.diplomado_final.service.MailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
@Transactional
@Component
@RequiredArgsConstructor
public class AppointmentEmailReminderScheduler {

    private final AppointmentRepository appointmentRepository;
    private final MailService mailService;

    // Cada 5 minutos
    @Scheduled(fixedDelay = 300_000)
    public void sendPendingReminders() {

        List<AppointmentEmailData> reminders =
                appointmentRepository.findPendingEmailReminders();

        Instant now = Instant.now();

        for (AppointmentEmailData data : reminders) {

            LocalDateTime appointmentDateTime =
                    LocalDateTime.of(data.date(), data.startTime());

            Instant reminderTime =
                    appointmentDateTime
                            .minusMinutes(1440) // o data.reminderMinutesBefore si luego lo agregas
                            .atZone(ZoneId.systemDefault())
                            .toInstant();

            if (now.isBefore(reminderTime)) {
                continue;
            }

            mailService.sendAppointmentReminderEmail(
                    data.patientEmail(),
                    data.patientFullName(),
                    data.doctorFullName(),
                    data.clinicName(),
                    data.date(),
                    data.startTime()
            );

            appointmentRepository.markEmailSent(
                    data.appointmentId(),
                    now
            );
        }
    }
}