package tn.tn.elfatoora.service;

import tn.tn.elfatoora.entity.AdhesionAudit;
import tn.tn.elfatoora.repo.AdhesionAuditRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuditService {

    private final AdhesionAuditRepository auditRepo;

    public AuditService(AdhesionAuditRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    public void log(UUID draftId, String actor, String action, String details) {
        AdhesionAudit a = new AdhesionAudit();
        a.setDraftId(draftId);
        a.setActor(actor);
        a.setAction(action);
        a.setDetails(details);
        auditRepo.save(a);
    }
}
