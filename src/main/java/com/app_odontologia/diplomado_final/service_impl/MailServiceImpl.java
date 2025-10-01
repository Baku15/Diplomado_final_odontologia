package com.app_odontologia.diplomado_final.service_impl;

import com.app_odontologia.diplomado_final.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendCredentialsEmail(String to, String username, String tempPassword) {
        var msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Tus credenciales - Clínica Odontológica");
        msg.setText("""
      ¡Hola!
      Tu solicitud fue aprobada.

      Usuario: %s
      Contraseña temporal: %s

      Ingresa al sistema y cambia tu contraseña en tu primer acceso.
      """.formatted(username, tempPassword));
        mailSender.send(msg);
    }
}
