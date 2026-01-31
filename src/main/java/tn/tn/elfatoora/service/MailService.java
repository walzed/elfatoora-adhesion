package tn.tn.elfatoora.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Recover;
import org.springframework.scheduling.annotation.Async; // Ajout
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private final JavaMailSender sender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.subject.prefix}")
    private String subjectPrefix;

    public MailService(JavaMailSender sender) {
        this.sender = sender;
    }

    @Async("mailExecutor")
    @Retryable(
            value = { org.springframework.mail.MailException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void send(String to, String subject, String body) {
        log.info("Tentative d'envoi du mail à {}", to);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subjectPrefix + " " + subject);
        msg.setText(body);

        sender.send(msg);

        log.info("Mail envoyé avec succès à {}", to);
    }
// Cette méthode est appelée si les 3 tentatives échouent
    @Recover
    public void recover(org.springframework.mail.MailException e, String to, String subject, String body) {
        log.error("ÉCHEC DÉFINITIF de l'envoi à {} après 3 tentatives. Erreur : {}", to, e.getMessage());
        // Optionnel : enregistrer l'échec en base pour une analyse manuelle
    }
}