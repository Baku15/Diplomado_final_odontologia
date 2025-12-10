// src/main/java/com/app_odontologia/diplomado_final/service/MailService.java
package com.app_odontologia.diplomado_final.service;

import com.app_odontologia.diplomado_final.model.entity.DoctorInvitation;

public interface MailService {

    void sendCredentialsEmail(String to, String username, String tempPassword);
    void sendActivationEmail(String to, String activationLink);
    void sendDoctorInvitationEmail(DoctorInvitation invitation);

    // NUEVO: correo para activación de paciente
    void sendPatientActivationEmail(String to, String activationLink);
}
