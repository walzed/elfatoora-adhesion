package tn.tn.elfatoora.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.tn.elfatoora.entity.AdhesionDossier;
import tn.tn.elfatoora.model.AdhesionStatut;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdhesionDossierRepository extends JpaRepository<AdhesionDossier, Long> {

    Optional<AdhesionDossier> findByDraftId(UUID draftId);
    Optional<AdhesionDossier> findByDossierRef(String dossierRef);
    
    Optional<AdhesionDossier> findByDraftId(String draftId);


    List<AdhesionDossier> findByMatriculeFiscal(String matriculeFiscal);

    List<AdhesionDossier> findByAppUser_IdOrderByCreatedAtDesc(Long appUserId);
    List<AdhesionDossier> findByAppUser_IdAndStatutOrderByCreatedAtDesc(Long appUserId, AdhesionStatut statut);
    List<AdhesionDossier> findByAppUser_IdAndDossierRefContainingIgnoreCaseOrderByCreatedAtDesc(Long appUserId, String dossierRef);
    Optional<AdhesionDossier> findFirstByAppUser_IdAndStatutOrderByIdDesc(Long appUserId, AdhesionStatut statut);
    Optional<AdhesionDossier> findByAppUser_IdAndDossierRef(Long appUserId, String dossierRef);
    
    java.util.Optional<AdhesionDossier> findByAppUser_IdAndDossierRefAndStatut(Long appUserId,
            String dossierRef,
            AdhesionStatut statut);


    long countByStatut(AdhesionStatut statut);
    long countByAppUser_IdAndStatut(Long appUserId, AdhesionStatut statut);

    default long countDraft(){ return countByStatut(AdhesionStatut.DRAFT); }
    default long countSoumis(){ return countByStatut(AdhesionStatut.SOUMIS); }
    default long countEnInstruction(){ return countByStatut(AdhesionStatut.EN_INSTRUCTION); }
    default long countComplement(){ return countByStatut(AdhesionStatut.COMPLEMENT); }
    default long countValidee(){ return countByStatut(AdhesionStatut.VALIDEE); }
    default long countRejetee(){ return countByStatut(AdhesionStatut.REJETEE); }
}
