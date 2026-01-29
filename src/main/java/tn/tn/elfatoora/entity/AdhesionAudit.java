package tn.tn.elfatoora.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="adhesion_audit", indexes = {
        @Index(name="idx_audit_draft", columnList="draft_id")
})
public class AdhesionAudit {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="draft_id", nullable=false)
    private UUID draftId;

    private String actor;

    @Column(nullable=false, length=40)
    private String action;

    @Column(columnDefinition="text")
    private String details;

    @Column(name="created_at", nullable=false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Getters/Setters
    public Long getId() { return id; }
    public UUID getDraftId() { return draftId; }
    public void setDraftId(UUID draftId) { this.draftId = draftId; }
    public String getActor() { return actor; }
    public void setActor(String actor) { this.actor = actor; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
