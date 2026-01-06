package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.model.entity.DoctorInvitation;
import com.app_odontologia.diplomado_final.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
@Service
@RequiredArgsConstructor
@Slf4j
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendCredentialsEmail(String to, String username, String tempPassword) {
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Tus credenciales temporales - Clínica Odontológica");
            helper.setText("""
        <div style="font-family:Arial,sans-serif;">
          <h2>Credenciales temporales</h2>
          <p>Se han generado credenciales temporales para tu acceso:</p>
          <ul>
            <li><b>Usuario:</b> %s</li>
            <li><b>Contraseña temporal:</b> %s</li>
          </ul>
          <p>Por seguridad, cambia la contraseña en tu primer inicio de sesión.</p>
        </div>
      """.formatted(username, tempPassword), true);
            helper.setFrom("no-reply@tu-dominio.com");
            mailSender.send(mime);
            log.info("Correo de credenciales temporales enviado a {}", to);
        } catch (Exception e) {
            throw new IllegalStateException("Error enviando correo de credenciales: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendActivationEmail(String to, String activationLink) {
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Activa tu cuenta - Clínica Odontológica");
            helper.setText("""
      <div style="font-family:Arial,sans-serif;">
        <h2>Tu cuenta fue aprobada</h2>
        <p>Actívala y define tu contraseña desde aquí:</p>
        <p><a href="%s">%s</a></p>
        <p>Este enlace expira en 24 horas.</p>
      </div>
    """.formatted(activationLink, activationLink), true);
            helper.setFrom("no-reply@tu-dominio.com");
            mailSender.send(mime);
            log.info("Correo de activación enviado a {}", to);
        } catch (Exception e) {
            throw new IllegalStateException("Error enviando correo de activación: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendDoctorInvitationEmail(DoctorInvitation invitation) {
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, "UTF-8");

            String to = invitation.getEmail();
            String doctorName = invitation.getFullName();
            if (doctorName == null || doctorName.isBlank()) {
                doctorName = "Doctor(a)";
            }

            String clinicName = null;
            if (invitation.getClinic() != null) {
                clinicName = invitation.getClinic().getNombreComercial();
                if (clinicName == null || clinicName.isBlank()) {
                    clinicName = "Clínica #" + invitation.getClinic().getId();
                }
            } else {
                clinicName = "Clínica odontológica";
            }

            String frontendBaseUrl = "http://localhost:4200";
            String invitationLink = frontendBaseUrl + "/invitacion-doctor/" + invitation.getToken();

            helper.setTo(to);
            helper.setSubject("Acceso a la plataforma de gestión de la clínica " + clinicName);

            helper.setText("""
            <div style="font-family:Arial,sans-serif; line-height:1.5;">
              <h2 style="color:#0f172a;">Acceso a la plataforma de gestión</h2>
              
              <p>Hola %s,</p>
              
              <p>
                Desde la clínica <strong>%s</strong> hemos habilitado tu acceso
                a la plataforma de gestión odontológica que utilizamos en el día a día.
              </p>
              
              <p>Para completar tu registro y definir tu usuario y contraseña, entra a este enlace seguro:</p>
              
              <p>
                <a href="%s" style="color:#2563eb; text-decoration:none;">
                  %s
                </a>
              </p>
              
              <p style="font-size:13px; color:#6b7280;">
                Si no esperabas este correo, por favor comunícate con la administración
                de la clínica o ignora este mensaje.
              </p>
              
              <p style="margin-top:16px; font-size:13px; color:#4b5563;">
                Saludos,<br/>
                Administración – %s
              </p>
            </div>
            """.formatted(
                    doctorName,
                    clinicName,
                    invitationLink,
                    invitationLink,
                    clinicName
            ), true);

            helper.setFrom("no-reply@tu-dominio.com");
            mailSender.send(mime);

            log.info("Correo de invitación de doctor enviado a {}", to);
        } catch (Exception e) {
            throw new IllegalStateException("Error enviando correo de invitación de doctor: " + e.getMessage(), e);
        }
    }

    // ---------- NUEVO: plantilla específica para activación de paciente ----------
    @Override
    public void sendPatientActivationEmail(String to, String activationLink) {
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, "UTF-8");
            helper.setTo(to);
            helper.setSubject("Activa tu cuenta en la clínica - Acceso al portal de pacientes");
            helper.setText("""
      <div style="font-family:Arial,sans-serif;">
        <h2>Bienvenido</h2>
        <p>Hemos creado una cuenta para ti en el portal de la clínica. Para activar tu cuenta y definir una contraseña segura, haz clic en el siguiente enlace:</p>
        <p><a href="%s">%s</a></p>
        <p style="font-size:13px; color:#6b7280;">Este enlace expira en unas horas. Si no solicitaste este acceso, ignora este correo o comunícate con la clínica.</p>
        <p style="margin-top:12px; font-size:13px; color:#4b5563;">Saludos,<br/>Administración – Tu clínica</p>
      </div>
    """.formatted(activationLink, activationLink), true);
            helper.setFrom("no-reply@tu-dominio.com");
            mailSender.send(mime);
            log.info("Correo de activación de paciente enviado a {}", to);
        } catch (Exception e) {
            throw new IllegalStateException("Error enviando correo de activación de paciente: " + e.getMessage(), e);
        }
    }


    @Override
    public void sendAppointmentReminderEmail(
            String to,
            String patientFullName,
            String doctorFullName,
            String clinicName,
            LocalDate date,
            LocalTime startTime
    ) {
        try {
            var mime = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mime, "UTF-8");

            // ===== FORMATEO HUMANO (CLÍNICO) =====
            // Fecha: lunes 22 de diciembre de 2025
            String formattedDate = date.format(
                    java.time.format.DateTimeFormatter.ofPattern(
                            "EEEE d 'de' MMMM 'de' yyyy",
                            new java.util.Locale("es", "ES")
                    )
            );

            // Hora: 12:30 (SIN segundos ni milisegundos)
            String formattedTime = startTime.format(
                    java.time.format.DateTimeFormatter.ofPattern("HH:mm")
            );

            helper.setTo(to);
            helper.setSubject("Recordatorio de cita odontológica");

            helper.setText("""
        <div style="font-family:Arial,sans-serif; line-height:1.6;">
          <h2>Recordatorio de cita</h2>

          <p>Estimado/a <strong>%s</strong>,</p>

          <p>
            Le recordamos que tiene una cita programada en la clínica
            <strong>%s</strong>.
          </p>

          <ul>
            <li><strong>Profesional:</strong> %s</li>
            <li><strong>Fecha:</strong> %s</li>
            <li><strong>Hora:</strong> %s</li>
          </ul>

          <p style="font-size:13px; color:#4b5563;">
            Atentamente,<br/>
            Equipo %s
          </p>
        </div>
        """.formatted(
                    patientFullName,
                    clinicName,
                    doctorFullName,
                    formattedDate,
                    formattedTime,
                    clinicName
            ), true);

            helper.setFrom("no-reply@odontoweb.com");
            mailSender.send(mime);

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Error enviando recordatorio de cita: " + e.getMessage(), e
            );
        }
    }

    private String formatDate(LocalDate date) {
        return date.format(
                DateTimeFormatter.ofPattern(
                        "EEEE dd 'de' MMMM 'de' yyyy",
                        new Locale("es", "ES")
                )
        );
    }



}
