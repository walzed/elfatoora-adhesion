package tn.tn.elfatoora.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender sender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.subject.prefix}")
    private String subjectPrefix;

    public MailService(JavaMailSender sender) {
        this.sender = sender;
    }

    public void send(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subjectPrefix + " " + subject);
        msg.setText(body);
        sender.send(msg);
    }
}
