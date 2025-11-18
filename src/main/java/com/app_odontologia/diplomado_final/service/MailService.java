package com.app_odontologia.diplomado_final.service;

public interface MailService {

    void sendCredentialsEmail(String to, String username, String tempPassword);
    void sendActivationEmail(String to, String activationLink);

}