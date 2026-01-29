package tn.tn.elfatoora.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="adhesion_notification", indexes = {
        @Index(name="idx_notif_email", columnList="recipient_email")
})
public class AdhesionNotification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="draft_id", nullable=false)
    private UUID draftId;

    @Column(name="dossier_ref", length=64)
    private String dossierRef;

    @Column(name="recipient_email", nullable=false)
    private String recipientEmail;

    @Column(nullable=false, length=20)
    private String channel; // PORTAIL / EMAIL

    @Column(nullable=false)
    private String subject;

    @Column(columnDefinition="text", nullable=false)
    private String message;

    @Column(nullable=false, length=20)
    private String status = "PENDING"; // PENDING/SENT/FAILED

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name="sent_at")
    private LocalDateTime sentAt;

    // Getters/Setters
    public Long getId() { return id; }
    public UUID getDraftId() { return draftId; }
    public void setDraftId(UUID draftId) { this.draftId = draftId; }
    public String getDossierRef() { return dossierRef; }
    public void setDossierRef(String dossierRef) { this.dossierRef = dossierRef; }
    public String getRecipientEmail() { return recipientEmail; }
    public void setRecipientEmail(String recipientEmail) { this.recipientEmail = recipientEmail; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
