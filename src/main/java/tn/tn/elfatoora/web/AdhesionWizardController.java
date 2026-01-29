package tn.tn.elfatoora.web;

import tn.tn.elfatoora.entity.AdhesionDossier;
import tn.tn.elfatoora.entity.AdhesionPieceVersion;
import tn.tn.elfatoora.model.AdhesionStatut;
import tn.tn.elfatoora.repo.AdhesionAuditRepository;
import tn.tn.elfatoora.repo.AdhesionDossierRepository;
import tn.tn.elfatoora.service.AdhesionService;
import tn.tn.elfatoora.service.PieceService;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.Principal;
import java.util.*;
import java.util.UUID;

@Controller
@RequestMapping("/adhesion")
@SessionAttributes("wiz")
public class AdhesionWizardController {

    private final AdhesionService adhesionService;
    private final AdhesionDossierRepository dossierRepo;
    private final AdhesionAuditRepository auditRepo;
    private final PieceService pieceService;

    public AdhesionWizardController(AdhesionService adhesionService,
                                    AdhesionDossierRepository dossierRepo,
                                    AdhesionAuditRepository auditRepo,
                                    PieceService pieceService) {
        this.adhesionService = adhesionService;
        this.dossierRepo = dossierRepo;
        this.auditRepo = auditRepo;
        this.pieceService = pieceService;
    }

    @ModelAttribute("wiz")
    public AdhesionWizard wiz() {
        AdhesionWizard w = new AdhesionWizard();
        w.draftId = UUID.randomUUID().toString();
        return w;
    }

    // -------------------------------------------------------
    // Etape 0
    // -------------------------------------------------------
    @GetMapping
    public String etape0(@ModelAttribute("wiz") AdhesionWizard wiz, Principal principal) {
        // initialise draft si nécessaire
        // adhesionService.getOrCreateDraft(UUID.fromString(wiz.draftId), principal.getName());
        return "adhesion/etape0";
    }

    // -------------------------------------------------------
    // Etape 1
    // -------------------------------------------------------
    @GetMapping("/etape1")
    public String etape1() {
        return "adhesion/etape1";
    }

    @PostMapping("/etape1")
    public String postEtape1(@ModelAttribute("wiz") AdhesionWizard w,
                             Principal principal,
                             Model model) {
        try {
            // Normalisation + validations serveur (défense en profondeur)
            if (w.matriculeFiscal != null) w.matriculeFiscal = w.matriculeFiscal.trim();
            if (w.telephone != null) w.telephone = w.telephone.trim();

            if (w.matriculeFiscal == null || !w.matriculeFiscal.matches("^[0-9]{7}[A-Za-z]$")) {
                model.addAttribute("error",
                        "Matricule fiscal invalide : attendu 7 chiffres suivis d’une lettre (ex: 1234567A).");
                return "adhesion/etape1";
            }
            // Uniformiser la lettre en majuscule
            if (w.matriculeFiscal.length() == 8) {
                String prefix = w.matriculeFiscal.substring(0, 7);
                char last = Character.toUpperCase(w.matriculeFiscal.charAt(7));
                w.matriculeFiscal = prefix + last;
            }

            if (w.telephone == null || !w.telephone.matches("^[0-9]{8}$")) {
                model.addAttribute("error", "Téléphone invalide : attendu 8 chiffres (ex: 98123456).");
                return "adhesion/etape1";
            }

            AdhesionDossier d = mapWizardToDossier(
                    w,
                    adhesionService.getOrCreateDraft(UUID.fromString(w.draftId), principal.getName())
            );

            adhesionService.save(d, principal.getName(), "SAVE_STEP1");
            return "redirect:/adhesion/etape2";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "adhesion/etape1";
        }
    }

    // -------------------------------------------------------
    // Etape 2
    // -------------------------------------------------------
    @GetMapping("/etape2")
    public String etape2(@ModelAttribute("wiz") AdhesionWizard w, Principal principal) {
        // Si l'email de l'admin n'est pas encore rempli, on met celui de l'utilisateur connecté
        if (w.emailAdminPrincipal == null || w.emailAdminPrincipal.isEmpty()) {
            w.emailAdminPrincipal = principal.getName();
        }
        return "adhesion/etape2";
    }

    @PostMapping("/etape2")
    public String postEtape2(@ModelAttribute("wiz") AdhesionWizard w,
                             Principal principal,
                             Model model) {
        try {
            AdhesionDossier d = mapWizardToDossier(
                    w,
                    dossierRepo.findByDraftId(UUID.fromString(w.draftId))
                            .orElseThrow(() -> new IllegalArgumentException("Draft introuvable"))
            );

            adhesionService.save(d, principal.getName(), "SAVE_STEP2");
            return "redirect:/adhesion/etape3";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "adhesion/etape2";
        }
    }

    // -------------------------------------------------------
    // Etape 3
    // -------------------------------------------------------
    @GetMapping("/etape3")
    public String etape3() {
        return "adhesion/etape3";
    }

    @PostMapping("/etape3")
    public String postEtape3(@ModelAttribute("wiz") AdhesionWizard w,
                             Principal principal,
                             Model model) {
        try {
            AdhesionDossier d = mapWizardToDossier(
                    w,
                    dossierRepo.findByDraftId(UUID.fromString(w.draftId))
                            .orElseThrow(() -> new IllegalArgumentException("Draft introuvable"))
            );

            adhesionService.save(d, principal.getName(), "SAVE_STEP3");
            return "redirect:/adhesion/etape4";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "adhesion/etape3";
        }
    }

    // -------------------------------------------------------
    // Visualisation d'une pièce (inline)
    // -------------------------------------------------------
    @GetMapping("/piece/{type}")
    @ResponseBody
    public ResponseEntity<InputStreamResource> viewPiece(@PathVariable("type") String type,
                                                         @ModelAttribute("wiz") AdhesionWizard w) {
        try {
            if (w == null || w.draftId == null || w.draftId.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            String normalizedType = normalizeType(type);

            UUID draftId = UUID.fromString(w.draftId);
            Map<String, AdhesionPieceVersion> latest = pieceService.latestByDraftId(draftId);
            if (latest == null) latest = new LinkedHashMap<String, AdhesionPieceVersion>();

            AdhesionPieceVersion pv = latest.get(normalizedType);
            if (pv == null) return ResponseEntity.notFound().build();

            InputStream is = pieceService.download(pv.getObjectKey());
            InputStreamResource resource = new InputStreamResource(is);

            String filename = pv.getOriginalFilename() != null ? pv.getOriginalFilename() : (normalizedType + ".pdf");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            pv.getContentType() != null ? pv.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE
                    ))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // -------------------------------------------------------
    // Etape 4 (GET) : afficher état des pièces et upload
    // -------------------------------------------------------
    @GetMapping("/etape4")
    public String etape4(@ModelAttribute("wiz") AdhesionWizard w, Model model) {
        prepareEtape4Model(w, model);
        return "adhesion/etape4";
    }

    // -------------------------------------------------------
    // Etape 4 (POST) : upload / remplacement
    // -------------------------------------------------------
    @PostMapping("/etape4")
    public String postEtape4(@ModelAttribute("wiz") AdhesionWizard w,
                             @RequestParam(value = "file_RC", required = false) MultipartFile rc,
                             @RequestParam(value = "file_CIF", required = false) MultipartFile cif,
                             @RequestParam(value = "file_CIN_RESP", required = false) MultipartFile cinResp,
                             @RequestParam(value = "file_CIN_ADMIN", required = false) MultipartFile cinAdmin,
                             Principal principal,
                             Model model) {

        try {
            if (w == null || w.draftId == null || w.draftId.trim().isEmpty()) {
                throw new IllegalArgumentException("DraftId manquant (session wizard).");
            }
            UUID draftId = UUID.fromString(w.draftId);

            // Dernier état des pièces (ACTIVE)
            Map<String, AdhesionPieceVersion> latest = pieceService.latestByDraftId(draftId);
            if (latest == null) latest = new LinkedHashMap<String, AdhesionPieceVersion>();

            // Validation : si pas de pièce en base ET pas de fichier envoyé => erreur
            validateRequiredOrAlreadyPresent(latest, "RC", rc, "Le Registre de Commerce (RC) est obligatoire.");
            validateRequiredOrAlreadyPresent(latest, "CIF", cif, "L'Identifiant fiscal (CIF) est obligatoire.");
            validateRequiredOrAlreadyPresent(latest, "CIN_RESP", cinResp, "La CIN du Responsable est obligatoire.");
            validateRequiredOrAlreadyPresent(latest, "CIN_ADMIN", cinAdmin, "La CIN de l'Administrateur est obligatoire.");

            boolean anyUploaded = false;

            // Upload uniquement si un nouveau fichier est fourni (sinon on ne touche pas)
            if (rc != null && !rc.isEmpty()) {
                pieceService.uploadVersion(draftId, "RC", rc, principal.getName());
                anyUploaded = true;
            }
            if (cif != null && !cif.isEmpty()) {
                pieceService.uploadVersion(draftId, "CIF", cif, principal.getName());
                anyUploaded = true;
            }
            if (cinResp != null && !cinResp.isEmpty()) {
                pieceService.uploadVersion(draftId, "CIN_RESP", cinResp, principal.getName());
                anyUploaded = true;
            }
            if (cinAdmin != null && !cinAdmin.isEmpty()) {
                pieceService.uploadVersion(draftId, "CIN_ADMIN", cinAdmin, principal.getName());
                anyUploaded = true;
            }

            // Si on a modifié les pièces, on invalide la signature
            if (anyUploaded) {
                adhesionService.invalidateSignature(draftId, principal.getName(), "Mise à jour des pièces jointes");
                w.signatureOk = false;
            }

            return "redirect:/adhesion/etape5";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            prepareEtape4Model(w, model);
            return "adhesion/etape4";
        }
    }

    private void prepareEtape4Model(AdhesionWizard w, Model model) {

        // Types requis (utilisés par ton HTML)
        List<String> requiredTypes = Arrays.asList("RC", "CIF", "CIN_RESP", "CIN_ADMIN");
        model.addAttribute("requiredTypes", requiredTypes);

        if (w == null || w.draftId == null || w.draftId.trim().isEmpty()) {
            model.addAttribute("latest", Collections.emptyMap());
            model.addAttribute("piecesLatest", Collections.emptyList());
            model.addAttribute("hasRC", false);
            model.addAttribute("hasCIF", false);
            model.addAttribute("hasCIN_RESP", false);
            model.addAttribute("hasCIN_ADMIN", false);
            return;
        }

        UUID draftId = UUID.fromString(w.draftId);

        Map<String, AdhesionPieceVersion> latest = pieceService.latestByDraftId(draftId);
        if (latest == null) latest = new LinkedHashMap<String, AdhesionPieceVersion>();

        model.addAttribute("latest", latest);

        // Liste pour la section "Documents déjà déposés"
        ArrayList<AdhesionPieceVersion> piecesLatest = new ArrayList<AdhesionPieceVersion>(latest.values());
        Collections.sort(piecesLatest, new Comparator<AdhesionPieceVersion>() {
            @Override
            public int compare(AdhesionPieceVersion a, AdhesionPieceVersion b) {
                String ta = (a.getPieceType() == null) ? "" : a.getPieceType();
                String tb = (b.getPieceType() == null) ? "" : b.getPieceType();
                return ta.compareTo(tb);
            }
        });
        model.addAttribute("piecesLatest", piecesLatest);

        model.addAttribute("hasRC", latest.containsKey("RC"));
        model.addAttribute("hasCIF", latest.containsKey("CIF"));
        model.addAttribute("hasCIN_RESP", latest.containsKey("CIN_RESP"));
        model.addAttribute("hasCIN_ADMIN", latest.containsKey("CIN_ADMIN"));
    }

    private static void validateRequiredOrAlreadyPresent(Map<String, AdhesionPieceVersion> latest,
                                                         String type,
                                                         MultipartFile file,
                                                         String message) {
        boolean already = (latest != null && latest.containsKey(type));
        boolean provided = (file != null && !file.isEmpty());
        if (!already && !provided) {
            throw new IllegalArgumentException(message);
        }
    }

    private static String normalizeType(String type) {
        if (type == null) return "";
        return type.trim().toUpperCase();
    }

    // -------------------------------------------------------
    // Etape 5
    // -------------------------------------------------------
    @GetMapping("/etape5")
    public String etape5() {
        return "adhesion/etape5";
    }

    @PostMapping("/etape5")
    public String postEtape5(@ModelAttribute("wiz") AdhesionWizard w,
                             Principal principal,
                             Model model) {
        try {
            if (!Boolean.TRUE.equals(w.accepteContrat) || !Boolean.TRUE.equals(w.conserveOriginaux)) {
                model.addAttribute("error", "Vous devez cocher les deux engagements obligatoires avant de continuer.");
                model.addAttribute("wiz", w);
                return "adhesion/etape5";
            }

            AdhesionDossier d = dossierRepo.findByDraftId(UUID.fromString(w.draftId))
                    .orElseThrow(() -> new IllegalArgumentException("Draft introuvable"));

            d.setAccepteContrat(Boolean.TRUE.equals(w.accepteContrat));
            d.setConserveOriginaux(Boolean.TRUE.equals(w.conserveOriginaux));

            adhesionService.save(d, principal.getName(), "SAVE_STEP5");
            return "redirect:/adhesion/etape6";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("wiz", w);
            return "adhesion/etape5";
        }
    }

    // -------------------------------------------------------
    // Etape 6
    // -------------------------------------------------------
    @GetMapping("/etape6")
    public String etape6() {
        return "adhesion/etape6";
    }

    @PostMapping("/etape6/sign")
    public String sign(@ModelAttribute("wiz") AdhesionWizard w, Principal principal) {
        AdhesionDossier d = adhesionService.sign(UUID.fromString(w.draftId), principal.getName());
        w.signatureOk = d.isSignatureOk();
        w.hash = d.getSignatureHash();
        w.horodatage = d.getHorodatage();
        w.certAuthority = d.getCertAuthority();
        w.certSerial = d.getCertSerial();
        w.certExpiry = d.getCertExpiry();
        return "redirect:/adhesion/etape6";
    }

    @PostMapping("/etape6/submit")
    public String submit(@ModelAttribute("wiz") AdhesionWizard w,
                         Principal principal,
                         Model model) {
        try {
            AdhesionDossier d = adhesionService.submit(UUID.fromString(w.draftId), principal.getName());
            w.dossierRef = d.getDossierRef();
            w.statut = d.getStatut().name();
            return "redirect:/adhesion/etape7";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "adhesion/etape6";
        }
    }

    // -------------------------------------------------------
    // Etape 7
    // -------------------------------------------------------
    @GetMapping("/etape7")
    public String etape7() {
        return "adhesion/etape7";
    }

    // -------------------------------------------------------
    // Suivi
    // -------------------------------------------------------
    @GetMapping("/suivi")
    public String suivi(@RequestParam(value = "ref", required = false) String ref,
                        @RequestParam(value = "statut", required = false) AdhesionStatut statut,
                        Principal principal,
                        Model model) {

        Map<AdhesionStatut, Long> raw = adhesionService.getDashboardStats(principal.getName());

        Map<String, Long> stats = new LinkedHashMap<String, Long>();
        stats.put("SOUMIS", raw.getOrDefault(AdhesionStatut.SOUMIS, 0L));
        stats.put("EN_INSTRUCTION", raw.getOrDefault(AdhesionStatut.EN_INSTRUCTION, 0L));
        stats.put("COMPLEMENT", raw.getOrDefault(AdhesionStatut.COMPLEMENT, 0L));
        stats.put("VALIDEE", raw.getOrDefault(AdhesionStatut.VALIDEE, 0L));
        stats.put("REJETEE", raw.getOrDefault(AdhesionStatut.REJETEE, 0L));
        model.addAttribute("stats", stats);

        model.addAttribute("dossiers",
                adhesionService.listNonDraftDossiersForUser(principal.getName(), statut, ref));

        model.addAttribute("selectedStatut", statut);
        model.addAttribute("searchRef", ref);

        return "adhesion/suivi";
    }

    @GetMapping("/suivi/{dossierRef}")
    public String suiviDetail(@PathVariable("dossierRef") String dossierRef,
                              @RequestParam(value = "statut", required = false) AdhesionStatut statut,
                              @RequestParam(value = "ref", required = false) String refSearch,
                              Principal principal,
                              Model model) {

        model.addAttribute("selectedStatut", statut);
        model.addAttribute("searchRef", refSearch);

        AdhesionDossier dossier = adhesionService.getDossierForUserByRef(principal.getName(), dossierRef);

        model.addAttribute("dossier", dossier);
        model.addAttribute("audit",
                auditRepo.findByDraftIdOrderByCreatedAtAsc(dossier.getDraftId()));

        return "adhesion/suivi-detail";
    }

    // -------------------------------------------------------
    // Brouillons
    // -------------------------------------------------------
    @GetMapping("/mes-brouillons")
    public String mesBrouillons(@RequestParam(value = "ref", required = false) String ref,
                                Principal principal,
                                Model model) {

        model.addAttribute("brouillons", adhesionService.listDraftsForUser(principal.getName(), ref));
        model.addAttribute("searchRef", ref);

        return "adhesion/brouillons";
    }

    @GetMapping("/reprendre")
    public String reprendre(@RequestParam("ref") String dossierRef,
                            @ModelAttribute("wiz") AdhesionWizard wiz,
                            Principal principal,
                            Model model) {

        try {
            AdhesionDossier draft = adhesionService.getDraftForUserByRef(principal.getName(), dossierRef.trim());

            wiz.draftId = draft.getDraftId().toString();
            wiz.dossierRef = draft.getDossierRef();
            wiz.statut = (draft.getStatut() != null ? draft.getStatut().name() : null);

            fillWizardFromDossier(wiz, draft);

            return "redirect:/adhesion/etape1";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "adhesion/brouillons";
        }
    }

    // -------------------------------------------------------
    // PDFs (Contrat / Fiche)
    // -------------------------------------------------------
    @GetMapping("/contrat")
    @ResponseBody
    public ResponseEntity<Resource> visualiserContrat() {
        Resource pdf = new ClassPathResource("static/pdf/Contrat_EL-Fatoora.pdf");
        if (!pdf.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"Contrat_EL-Fatoora.pdf\"")
                .body(pdf);
    }

    @GetMapping("/contrat/pdf")
    @ResponseBody
    public ResponseEntity<Resource> telechargerPdf() {
        Resource pdf = new ClassPathResource("static/pdf/Fiche_Renseignements-El-Fatoora.pdf");
        if (!pdf.exists()) return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Fiche_Renseignements-El-Fatoora.pdf\"")
                .body(pdf);
    }

    // -------------------------------------------------------
    // Helpers mapping Wizard <-> Dossier
    // -------------------------------------------------------
    private void fillWizardFromDossier(AdhesionWizard w, AdhesionDossier d) {
    	w.formeJuridique = d.getFormeJuridique();
    	w.raisonSociale = d.getRaisonSociale();
        w.nomPersonnePhysique = d.getNomPersonnePhysique();
        w.prenomPersonnePhysique = d.getPrenomPersonnePhysique();
        w.registreCommerce = d.getRegistreCommerce();
        w.matriculeFiscal = d.getMatriculeFiscal();
        w.codeTVA = d.getCodeTVA();
        w.codeCategorie = d.getCodeCategorie();
        w.etabSecondaire = d.getEtabSecondaire();
        w.secteurActivite = d.getSecteurActivite();
        w.adresse = d.getAdresse();
        w.codePostal = d.getCodePostal();
        w.gouvernorat = d.getGouvernorat();
        w.ville = d.getVille();
        w.telephone = d.getTelephone();
        w.emailGeneral = d.getEmailGeneral();
        w.emailFacturation = d.getEmailFacturation();

        w.nomRespLegal = d.getNomRespLegal();
        w.prenomRespLegal = d.getPrenomRespLegal();
        w.fonctionRespLegal = d.getFonctionRespLegal();
        w.telRespLegal = d.getTelRespLegal();
        w.emailRespLegal = d.getEmailRespLegal();
        w.cinRespLegal = d.getCinRespLegal();

        w.nomAdminPrincipal = d.getNomAdminPrincipal();
        w.prenomAdminPrincipal = d.getPrenomAdminPrincipal();
        w.cinAdminPrincipal = d.getCinAdminPrincipal();
        w.telAdminPrincipal = d.getTelAdminPrincipal();
        w.emailAdminPrincipal = d.getEmailAdminPrincipal();

        w.typeSignatureElfAdh = d.getTypeSignatureElfAdh();
        w.modeConnexion = d.getModeConnexion();
        w.nombreComptes = d.getNombreComptes();
        w.canalEdi = d.getCanalEdi();
        w.ipFixe = d.getIpFixe();
        w.nomRespTechniqueEdi = d.getNomRespTechniqueEdi();
        w.prenomRespTechniqueEdi = d.getPrenomRespTechniqueEdi();
        w.cinRespTechniqueEdi = d.getCinRespTechniqueEdi();
        w.emailRespTechniqueEdi = d.getEmailRespTechniqueEdi();
        w.telRespTechniqueEdi = d.getTelRespTechniqueEdi();

        w.accepteContrat = d.isAccepteContrat();
        w.conserveOriginaux = d.isConserveOriginaux();

        w.signatureOk = d.isSignatureOk();
        w.hash = d.getSignatureHash();
        w.horodatage = d.getHorodatage();
        w.certAuthority = d.getCertAuthority();
        w.certSerial = d.getCertSerial();
        w.certExpiry = d.getCertExpiry();

        w.toErp = d.getToErp();
    }

    private AdhesionDossier mapWizardToDossier(AdhesionWizard w, AdhesionDossier d) {

    	d.setFormeJuridique(w.formeJuridique);
    	d.setRaisonSociale(w.raisonSociale);
        d.setNomPersonnePhysique(w.nomPersonnePhysique);
        d.setPrenomPersonnePhysique(w.prenomPersonnePhysique);

        d.setRegistreCommerce(w.registreCommerce);
        d.setMatriculeFiscal(w.matriculeFiscal);
        d.setCodeTVA(w.codeTVA);
        d.setCodeCategorie(w.codeCategorie);
        d.setEtabSecondaire(w.etabSecondaire);
        d.setSecteurActivite(w.secteurActivite);
        d.setAdresse(w.adresse);
        d.setCodePostal(w.codePostal);
        d.setGouvernorat(w.gouvernorat);
        d.setVille(w.ville);

        d.setTelephone(w.telephone);
        d.setEmailGeneral(w.emailGeneral);
        d.setEmailFacturation(w.emailFacturation);

        d.setNomRespLegal(w.nomRespLegal);
        d.setPrenomRespLegal(w.prenomRespLegal);
        d.setFonctionRespLegal(w.fonctionRespLegal);
        d.setTelRespLegal(w.telRespLegal);
        d.setEmailRespLegal(w.emailRespLegal);
        d.setCinRespLegal(w.cinRespLegal);

        d.setNomAdminPrincipal(w.nomAdminPrincipal);
        d.setPrenomAdminPrincipal(w.prenomAdminPrincipal);
        d.setCinAdminPrincipal(w.cinAdminPrincipal);
        d.setTelAdminPrincipal(w.telAdminPrincipal);
        d.setEmailAdminPrincipal(w.emailAdminPrincipal);

        d.setTypeSignatureElfAdh(w.typeSignatureElfAdh);
        d.setModeConnexion(w.modeConnexion);
        d.setNombreComptes(w.nombreComptes != null ? w.nombreComptes : 1);
        d.setCanalEdi(w.canalEdi);
        d.setIpFixe(w.ipFixe);

        d.setNomRespTechniqueEdi(w.nomRespTechniqueEdi);
        d.setPrenomRespTechniqueEdi(w.prenomRespTechniqueEdi);
        d.setCinRespTechniqueEdi(w.cinRespTechniqueEdi);
        d.setEmailRespTechniqueEdi(w.emailRespTechniqueEdi);
        d.setTelRespTechniqueEdi(w.telRespTechniqueEdi);

        d.setToErp(w.toErp != null ? w.toErp : 0);

        return d;
    }
}
