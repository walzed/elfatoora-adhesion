package tn.tn.elfatoora.repo;

import tn.tn.elfatoora.entity.AdhesionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AdhesionAuditRepository extends JpaRepository<AdhesionAudit, Long> {
    List<AdhesionAudit> findByDraftIdOrderByCreatedAtDesc(UUID draftId);
    List<AdhesionAudit> findByDraftIdOrderByCreatedAtAsc(UUID draftId);
}
