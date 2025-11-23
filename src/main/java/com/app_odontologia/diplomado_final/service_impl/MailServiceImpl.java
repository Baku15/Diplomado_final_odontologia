package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.model.entity.DoctorInvitation;
import com.app_odontologia.diplomado_final.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j

public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendCredentialsEmail(String to, String username, String tempPassword) {
        // 🔸 No lo usas en la opción B, pero lo dejamos funcional por compatibilidad.
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

            // Nombre de la clínica
            String clinicName = null;
            if (invitation.getClinic() != null) {
                clinicName = invitation.getClinic().getNombreComercial();
                if (clinicName == null || clinicName.isBlank()) {
                    clinicName = "Clínica #" + invitation.getClinic().getId();
                }
            } else {
                clinicName = "Clínica odontológica";
            }

            // TODO: si quieres, saca esta base URL a application.properties
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

}
