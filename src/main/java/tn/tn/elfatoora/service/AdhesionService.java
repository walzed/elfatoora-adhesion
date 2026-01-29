package tn.tn.elfatoora.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.tn.elfatoora.entity.AdhesionDossier;
import tn.tn.elfatoora.entity.AppUser;
import tn.tn.elfatoora.model.AdhesionStatut;
import tn.tn.elfatoora.repo.AdhesionDossierRepository;
import tn.tn.elfatoora.repo.AppUserRepository;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdhesionService {

    private final AdhesionDossierRepository dossierRepo;
    private final AppUserRepository userRepo;
    private final AuditService audit;
    private final NotificationService notif;

    public AdhesionService(AdhesionDossierRepository dossierRepo,
                           AppUserRepository userRepo,
                           AuditService audit,
                           NotificationService notif) {
        this.dossierRepo = dossierRepo;
        this.userRepo = userRepo;
        this.audit = audit;
        this.notif = notif;
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    private AppUser requireOwner(String actorEmail) {
        if (actorEmail == null || actorEmail.trim().isEmpty()) {
            throw new IllegalStateException("Actor (email) manquant.");
        }
        return userRepo.findByEmailIgnoreCase(actorEmail.trim())
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable: " + actorEmail));
    }

    private void requireFilled(String v, String label) {
        if (v == null || v.trim().isEmpty()) {
            throw new IllegalStateException("Veuillez saisir le champs: " + label);
        }
    }

    private void validateBeforeSubmit(AdhesionDossier d) {
    	

    	if ("MORALE".equalsIgnoreCase(d.getFormeJuridique())) {
    	    requireFilled(d.getRaisonSociale(), "Raison sociale - Etape 1");
    	} 
    	else if ("PHYSIQUE".equalsIgnoreCase(d.getFormeJuridique())) {
    	    requireFilled(d.getNomPersonnePhysique(), "Nom - Etape 1");
    	    requireFilled(d.getPrenomPersonnePhysique(), "Prénom - Etape 1");
    	}
    	
        requireFilled(d.getRegistreCommerce(), "N° RNE (Registre de commerce) - Etape 1");
        requireFilled(d.getMatriculeFiscal(), "Matricule fiscal - Etape 1");
        
        requireFilled(d.getSecteurActivite(), "Secteur activité - Etape 1");
        
        requireFilled(d.getAdresse(), "Adresse - Etape 1");
        requireFilled(d.getGouvernorat(), "Gouvernorat - Etape 1");
        requireFilled(d.getVille(), "Ville - Etape 1");
        requireFilled(d.getTelephone(), "Téléphone - Etape 1");
        requireFilled(d.getEmailGeneral(), "Email général - Etape 1");

        requireFilled(d.getNomRespLegal(), "Nom responsable légal - Etape 2");
        requireFilled(d.getPrenomRespLegal(), "Prénom responsable légal - Etape 2");
        requireFilled(d.getFonctionRespLegal(), "Fonction responsable légal - Etape 2");
        requireFilled(d.getTelRespLegal(), "Téléphone responsable légal - Etape 2");
        requireFilled(d.getEmailRespLegal(), "Email responsable légal - Etape 2");

        requireFilled(d.getNomAdminPrincipal(), "Nom administrateur principal - Etape 2");
        requireFilled(d.getPrenomAdminPrincipal(), "Prénom administrateur principal - Etape 2");
        requireFilled(d.getCinAdminPrincipal(), "CIN administrateur principal");
        requireFilled(d.getTelAdminPrincipal(), "Téléphone administrateur principal");
        requireFilled(d.getEmailAdminPrincipal(), "Email administrateur principal");

        requireFilled(d.getTypeSignatureElfAdh(), "Type Signature");
        requireFilled(d.getModeConnexion(), "Mode de connexion");

        if (d.getNombreComptes() == null || d.getNombreComptes() < 1) {
            throw new IllegalStateException("Nombre de comptes invalide.");
        }
    }

    private String generateBusinessRef() {
        int year = LocalDateTime.now().getYear();
        int rnd = (int) (Math.random() * 100000000);
        return "ELF-ADH-" + year + "-" + String.format("%08d", rnd);
    }

    private AdhesionDossier requireByDraft(UUID draftId) {
        return dossierRepo.findByDraftId(draftId)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable (draftId=" + draftId + ")"));
    }

    private AdhesionDossier requireByDossierRef(String dossierRef) {
        if (dossierRef == null || dossierRef.trim().isEmpty()) {
            throw new IllegalArgumentException("Référence dossier vide.");
        }
        return dossierRepo.findByDossierRef(dossierRef.trim())
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable (ref=" + dossierRef + ")"));
    }

    // ---------------------------------------------------------------------
    // Draft / Save
    // ---------------------------------------------------------------------

    @Transactional
    public AdhesionDossier getOrCreateDraft(UUID draftId, String actorEmail) {
        AppUser owner = requireOwner(actorEmail);

        return dossierRepo.findByDraftId(draftId)
                .map(existing -> {
                    // Sécurité : si un ancien draft existe sans appUser, on le rattache
                    if (existing.getAppUser() == null) {
                        existing.setAppUser(owner);
                    }
                    // IMPORTANT : attribuer une référence dès le début (utile pour lister les brouillons)
                    if (existing.getDossierRef() == null || existing.getDossierRef().trim().isEmpty()) {
                        existing.setDossierRef(generateBusinessRef());
                    }
                    return dossierRepo.save(existing);
                })
                .orElseGet(() -> {
                    AdhesionDossier d = new AdhesionDossier();
                    d.setDraftId(draftId);
                    d.setStatut(AdhesionStatut.DRAFT);
                    d.setAppUser(owner);
                    d.setDossierRef(generateBusinessRef());
                    AdhesionDossier saved = dossierRepo.save(d);
                    audit.log(draftId, actorEmail, "CREATE_DRAFT", "Création dossier draft");
                    return saved;
                });
    }

    @Transactional
    public AdhesionDossier save(AdhesionDossier d, String actorEmail, String action) {
        if (d == null) {
            throw new IllegalArgumentException("Dossier null.");
        }

        // Défense en profondeur : rattacher le user si manquant
        if (d.getAppUser() == null && actorEmail != null && !actorEmail.trim().isEmpty()) {
            d.setAppUser(requireOwner(actorEmail));
        }
        // Référence dossier toujours présente
        if (d.getDossierRef() == null || d.getDossierRef().trim().isEmpty()) {
            d.setDossierRef(generateBusinessRef());
        }

        AdhesionDossier saved = dossierRepo.save(d);
        audit.log(saved.getDraftId(), actorEmail, action, "Sauvegarde dossier");
        return saved;
    }

    // ---------------------------------------------------------------------
    // Signature / Soumission
    // ---------------------------------------------------------------------

    @Transactional
    public AdhesionDossier sign(UUID draftId, String actorEmail) {
        AdhesionDossier d = requireByDraft(draftId);

        // Simulation V1 signature
        d.setSignatureOk(true);
        d.setSignatureHash(UUID.randomUUID().toString().replace("-", ""));
        d.setHorodatage(LocalDateTime.now().toString());
        d.setCertAuthority("TTN-CA (SIMU)");
        d.setCertSerial("SER-" + (100000 + (int) (Math.random() * 900000)));
        d.setCertExpiry(LocalDateTime.now().plusYears(1).toLocalDate().toString());

        dossierRepo.save(d);
        audit.log(draftId, actorEmail, "SIGN", "Signature (simulation V1)");
        return d;
    }

    @Transactional
    public AdhesionDossier submit(UUID draftId, String actorEmail) {
        AdhesionDossier d = requireByDraft(draftId);

        validateBeforeSubmit(d);

        if (!d.isAccepteContrat() || !d.isConserveOriginaux()) {
            throw new IllegalStateException("Veuillez accepter le contrat et l'engagement de conservation des originaux.");
        }
        if (!d.isSignatureOk()) {
            throw new IllegalStateException("Signature obligatoire avant soumission.");
        }

        // la ref existe déjà, mais on la garde si déjà créée.
        if (d.getDossierRef() == null || d.getDossierRef().trim().isEmpty()) {
            d.setDossierRef(generateBusinessRef());
        }

        d.setStatut(AdhesionStatut.SOUMIS);
        dossierRepo.save(d);
        audit.log(draftId, actorEmail, "SUBMIT", "Soumission dossier " + d.getDossierRef());

        notif.notifyBoth(
                d.getDraftId(),
                d.getDossierRef(),
                d.getEmailAdminPrincipal(),
                "Dossier soumis",
                "Votre dossier " + d.getDossierRef() + " a été soumis à TTN pour instruction."
        );

        return d;
    }

    // ---------------------------------------------------------------------
    // Traitement Agent TTN (Instruction / Décision)
    // ---------------------------------------------------------------------

    @Transactional
    public AdhesionDossier take(String dossierRef, String agentEmail) {
        AdhesionDossier d = requireByDossierRef(dossierRef);

        if (d.getStatut() != AdhesionStatut.SOUMIS) {
            throw new IllegalStateException("Seuls les dossiers SOUMIS peuvent être pris en charge.");
        }

        d.setStatut(AdhesionStatut.EN_INSTRUCTION);
        dossierRepo.save(d);
        audit.log(d.getDraftId(), agentEmail, "TAKE", "Prise en charge");
        return d;
    }

    @Transactional
    public AdhesionDossier decide(String dossierRef, String decision, String motif, String agentEmail) {
        AdhesionDossier d = requireByDossierRef(dossierRef);

        if (d.getStatut() != AdhesionStatut.EN_INSTRUCTION) {
            throw new IllegalStateException("Décision possible uniquement en EN_INSTRUCTION.");
        }

        if ("VALIDER".equalsIgnoreCase(decision)) {

            d.setStatut(AdhesionStatut.VALIDEE);
            d.setMotifDecision(null);
            audit.log(d.getDraftId(), agentEmail, "VALIDATE", "Adhésion validée");

            notif.notifyBoth(d.getDraftId(), d.getDossierRef(), d.getEmailAdminPrincipal(),
                    "Adhésion validée",
                    "Votre adhésion El Fatoora est validée. Référence : " + d.getDossierRef());

        } else if ("COMPLEMENT".equalsIgnoreCase(decision)) {

            requireFilled(motif, "Motif complément");
            d.setStatut(AdhesionStatut.COMPLEMENT);
            d.setMotifDecision(motif);
            audit.log(d.getDraftId(), agentEmail, "REQUEST_COMPLEMENT", motif);

            notif.notifyBoth(d.getDraftId(), d.getDossierRef(), d.getEmailAdminPrincipal(),
                    "Complément requis",
                    "TTN demande un complément pour le dossier " + d.getDossierRef() + " :\n" + motif);

        } else if ("REJETER".equalsIgnoreCase(decision)) {

            requireFilled(motif, "Motif rejet");
            d.setStatut(AdhesionStatut.REJETEE);
            d.setMotifDecision(motif);
            audit.log(d.getDraftId(), agentEmail, "REJECT", motif);

            notif.notifyBoth(d.getDraftId(), d.getDossierRef(), d.getEmailAdminPrincipal(),
                    "Dossier rejeté",
                    "Votre dossier " + d.getDossierRef() + " a été rejeté :\n" + motif);

        } else {
            throw new IllegalArgumentException("Décision inconnue: " + decision);
        }

        d.setDecidedBy(agentEmail);
        d.setDecidedAt(LocalDateTime.now());
        return dossierRepo.save(d);
    }

    // ---------------------------------------------------------------------
    // Flux complément
    // ---------------------------------------------------------------------

    @Transactional
    public AdhesionDossier invalidateSignature(UUID draftId, String actorEmail, String reason) {
        AdhesionDossier d = requireByDraft(draftId);

        d.setSignatureOk(false);
        d.setSignatureHash(null);
        dossierRepo.save(d);

        audit.log(draftId, actorEmail, "INVALIDATE_SIGNATURE", reason);
        return d;
    }

    @Transactional
    public AdhesionDossier resubmitAfterComplement(UUID draftId, String actorEmail) {
        AdhesionDossier d = requireByDraft(draftId);

        if (d.getStatut() != AdhesionStatut.COMPLEMENT) {
            throw new IllegalStateException("Re-soumission autorisée uniquement en COMPLEMENT.");
        }
        if (!d.isSignatureOk()) {
            throw new IllegalStateException("Veuillez re-signer avant re-soumission.");
        }

        d.setStatut(AdhesionStatut.SOUMIS);
        dossierRepo.save(d);
        audit.log(draftId, actorEmail, "RESUBMIT", "Re-soumission après complément");

        notif.notifyBoth(draftId, d.getDossierRef(), d.getEmailAdminPrincipal(),
                "Dossier re-soumis",
                "Votre dossier " + d.getDossierRef() + " a été re-soumis après correction.");

        return d;
    }

    // ---------------------------------------------------------------------
    // Dashboard (Suivi)
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public AdhesionDossier getLatestByUserAndStatut(Long appUserId, AdhesionStatut statut) {
        return dossierRepo.findFirstByAppUser_IdAndStatutOrderByIdDesc(appUserId, statut).orElse(null);
    }

    /**
     * Liste des dossiers de l'utilisateur (tous statuts).
     */
    @Transactional(readOnly = true)
    public List<AdhesionDossier> listDossiersForUser(String actorEmail, AdhesionStatut statut, String ref) {
        AppUser owner = requireOwner(actorEmail);

        if (ref != null && !ref.trim().isEmpty()) {
            return dossierRepo.findByAppUser_IdAndDossierRefContainingIgnoreCaseOrderByCreatedAtDesc(owner.getId(), ref.trim());
        }
        if (statut != null) {
            return dossierRepo.findByAppUser_IdAndStatutOrderByCreatedAtDesc(owner.getId(), statut);
        }
        return dossierRepo.findByAppUser_IdOrderByCreatedAtDesc(owner.getId());
    }

    /**
     * (AJOUT) Liste uniquement NON-DRAFT pour la page "suivi".
     * Le filtrage se fait en mémoire pour éviter d'ajouter des méthodes JPA supplémentaires.
     */
    @Transactional(readOnly = true)
    public List<AdhesionDossier> listNonDraftDossiersForUser(String actorEmail, AdhesionStatut statut, String ref) {
        if (statut == AdhesionStatut.DRAFT) {
            return java.util.Collections.emptyList();
        }
        List<AdhesionDossier> base = listDossiersForUser(actorEmail, statut, ref);
        return base.stream()
                .filter(d -> d.getStatut() != AdhesionStatut.DRAFT)
                .collect(Collectors.toList());
    }

    /**
     * (AJOUT) Liste des brouillons de l'utilisateur (optionnellement filtrés par ref).
     */
    @Transactional(readOnly = true)
    public List<AdhesionDossier> listDraftsForUser(String actorEmail, String ref) {
        List<AdhesionDossier> drafts = listDossiersForUser(actorEmail, AdhesionStatut.DRAFT, null);
        if (ref == null || ref.trim().isEmpty()) {
            return drafts;
        }
        String r = ref.trim().toLowerCase();
        return drafts.stream()
                .filter(d -> d.getDossierRef() != null && d.getDossierRef().toLowerCase().contains(r))
                .collect(Collectors.toList());
    }
    
    

    @Transactional(readOnly = true)
    public AdhesionDossier getDossierForUserByRef(String actorEmail, String dossierRef) {
        AppUser owner = requireOwner(actorEmail);
        return dossierRepo.findByAppUser_IdAndDossierRef(owner.getId(), dossierRef)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable: " + dossierRef));
    }

    /**
     * (AJOUT) Récupère un brouillon par ref pour le modifier.
     */
//    @Transactional(readOnly = true)
//    public AdhesionDossier getDraftForUserByRef(String actorEmail, String dossierRef) {
//        AdhesionDossier d = getDossierForUserByRef(actorEmail, dossierRef);
//        if (d.getStatut() != AdhesionStatut.DRAFT) {
//            throw new IllegalStateException("Ce dossier n'est pas un brouillon: " + dossierRef);
//        }
//        return d;
//    }
    
    @Transactional(readOnly = true)
    public AdhesionDossier getDraftForUserByRef(String actorEmail, String dossierRef) {
        AppUser owner = requireOwner(actorEmail);

        return dossierRepo
                .findByAppUser_IdAndDossierRefAndStatut(owner.getId(), dossierRef, AdhesionStatut.DRAFT)
                .orElseThrow(() -> new IllegalArgumentException("Brouillon introuvable pour cette référence."));
    }

    @Transactional(readOnly = true)
    public Map<AdhesionStatut, Long> getDashboardStats(String actorEmail) {
        AppUser owner = requireOwner(actorEmail);

        EnumMap<AdhesionStatut, Long> stats = new EnumMap<>(AdhesionStatut.class);
        stats.put(AdhesionStatut.DRAFT, dossierRepo.countByAppUser_IdAndStatut(owner.getId(), AdhesionStatut.DRAFT));
        stats.put(AdhesionStatut.SOUMIS, dossierRepo.countByAppUser_IdAndStatut(owner.getId(), AdhesionStatut.SOUMIS));
        stats.put(AdhesionStatut.EN_INSTRUCTION, dossierRepo.countByAppUser_IdAndStatut(owner.getId(), AdhesionStatut.EN_INSTRUCTION));
        stats.put(AdhesionStatut.COMPLEMENT, dossierRepo.countByAppUser_IdAndStatut(owner.getId(), AdhesionStatut.COMPLEMENT));
        stats.put(AdhesionStatut.VALIDEE, dossierRepo.countByAppUser_IdAndStatut(owner.getId(), AdhesionStatut.VALIDEE));
        stats.put(AdhesionStatut.REJETEE, dossierRepo.countByAppUser_IdAndStatut(owner.getId(), AdhesionStatut.REJETEE));
        return stats;
    }

    @Transactional(readOnly = true)
    public AdhesionDossier getByUserAndDossierRef(Long appUserId, String dossierRef) {
        return dossierRepo.findByAppUser_IdAndDossierRef(appUserId, dossierRef).orElse(null);
    }

    // ---------------------------------------------------------------------
    // Global stats (optionnel / admin)
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public long countDraft() { return dossierRepo.countDraft(); }

    @Transactional(readOnly = true)
    public long countSoumis() { return dossierRepo.countSoumis(); }

    @Transactional(readOnly = true)
    public long countEnInstruction() { return dossierRepo.countEnInstruction(); }

    @Transactional(readOnly = true)
    public long countComplement() { return dossierRepo.countComplement(); }

    @Transactional(readOnly = true)
    public long countValidee() { return dossierRepo.countValidee(); }

    @Transactional(readOnly = true)
    public long countRejetee() { return dossierRepo.countRejetee(); }
}
