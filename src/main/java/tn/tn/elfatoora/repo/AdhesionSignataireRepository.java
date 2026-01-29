package tn.tn.elfatoora.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.tn.elfatoora.entity.AdhesionSignataire;

import java.util.List;
import java.util.UUID;

@Repository
public interface AdhesionSignataireRepository extends JpaRepository<AdhesionSignataire, Long> {

    List<AdhesionSignataire> findByDraftId(UUID draftId);

    List<AdhesionSignataire> findByMatriculeFiscaleAndCin(String matriculeFiscale, String cin);

    long deleteByDraftId(UUID draftId);
}
