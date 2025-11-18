package com.app_odontologia.diplomado_final.service_impl;

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
}
