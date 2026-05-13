package com.adabyron.infraestructure.mail;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarCorreoEliminacion(String destinatario, String contenido){
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setFrom("reservistasAdaByron@resend.dev");
        mensaje.setSubject("ALERTA: Reserva Eliminada");
        mensaje.setText(contenido);
        mailSender.send(mensaje);
    }

    public void enviarCorreoCancelacion(String destinatario, String contenido){
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setFrom("reservistasAdaByron@resend.dev");
        mensaje.setSubject("ALERTA: Reserva Cancelada");
        mensaje.setText(contenido);
        mailSender.send(mensaje);
    }

    // Ya no se notifica al usuario cuando pasa a potencialmente invalida
    /*
    public void enviarCorreoInvalida(String destinatario, String contenido){
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setFrom("reservistasAdaByron@resend.dev");
        mensaje.setSubject("ALERTA: Reserva Potencialmente Inválida");
        mensaje.setText(contenido);
        mailSender.send(mensaje);
    }
    */
}
