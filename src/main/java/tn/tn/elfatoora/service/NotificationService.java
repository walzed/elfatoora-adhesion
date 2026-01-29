package tn.tn.elfatoora.service;

import tn.tn.elfatoora.entity.AdhesionNotification;
import tn.tn.elfatoora.repo.AdhesionNotificationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class NotificationService {

    private final AdhesionNotificationRepository notifRepo;
    private final MailService mailService;

    public NotificationService(AdhesionNotificationRepository notifRepo, MailService mailService) {
        this.notifRepo = notifRepo;
        this.mailService = mailService;
    }

    public void notifyPortal(UUID draftId, String dossierRef, String email, String subject, String message) {
        AdhesionNotification n = new AdhesionNotification();
        n.setDraftId(draftId);
        n.setDossierRef(dossierRef);
        n.setRecipientEmail(email);
        n.setChannel("PORTAIL");
        n.setSubject(subject);
        n.setMessage(message);
        notifRepo.save(n);
    }

    public void notifyEmail(UUID draftId, String dossierRef, String email, String subject, String message) {
        // Persist queue row
        AdhesionNotification n = new AdhesionNotification();
        n.setDraftId(draftId);
        n.setDossierRef(dossierRef);
        n.setRecipientEmail(email);
        n.setChannel("EMAIL");
        n.setSubject(subject);
        n.setMessage(message);
        n.setStatus("SENT");
        n.setSentAt(LocalDateTime.now());
        notifRepo.save(n);

        // Send immediately (V1)
        mailService.send(email, subject, message);
    }

    public void notifyBoth(UUID draftId, String dossierRef, String email, String subject, String message) {
        notifyPortal(draftId, dossierRef, email, subject, message);
        notifyEmail(draftId, dossierRef, email, subject, message);
    }
}
