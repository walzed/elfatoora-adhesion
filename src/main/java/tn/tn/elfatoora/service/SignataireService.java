package tn.tn.elfatoora.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tn.elfatoora.entity.AdhesionDossier;
import tn.tn.elfatoora.entity.AdhesionSignataire;
import tn.tn.elfatoora.repo.AdhesionSignataireRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SignataireService {

    private final AdhesionSignataireRepository repo;

    public SignataireService(AdhesionSignataireRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<AdhesionSignataire> listByDraftId(UUID draftId) {
        return repo.findByDraftId(draftId);
    }

    @Transactional(readOnly = true)
    public Optional<AdhesionSignataire> get(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public AdhesionSignataire addForDossier(AdhesionDossier dossier, AdhesionSignataire payload) {
        // IMPORTANT : on persist sur la colonne draft_id
        payload.setDraftId(dossier.getDraftId());
        payload.setMatriculeFiscale(dossier.getMatriculeFiscal());
        payload.setRcRne(dossier.getRegistreCommerce());
        
        // Relation dossier en lecture seule (insertable=false/updatable=false)
        payload.setDossier(dossier);

        return repo.save(payload);
    }

    @Transactional
    public AdhesionSignataire save(AdhesionSignataire entity) {
        return repo.save(entity);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    @Transactional
    public long deleteAllForDraft(UUID draftId) {
        return repo.deleteByDraftId(draftId);
    }
}
