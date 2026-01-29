package tn.tn.elfatoora.web;

import tn.tn.elfatoora.entity.AdhesionDossier;
import tn.tn.elfatoora.model.AdhesionStatut;
import tn.tn.elfatoora.repo.AdhesionDossierRepository;
import tn.tn.elfatoora.repo.AdhesionPieceVersionRepository;
import tn.tn.elfatoora.service.AdhesionService;
import tn.tn.elfatoora.service.PieceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequestMapping("/adhesion/complement")
@SessionAttributes("wiz")
public class ComplementController {

    private final AdhesionDossierRepository dossierRepo;
    private final AdhesionPieceVersionRepository pvRepo;
    private final PieceService pieceService;
    private final AdhesionService adhesionService;

    public ComplementController(AdhesionDossierRepository dossierRepo,
                               AdhesionPieceVersionRepository pvRepo,
                               PieceService pieceService,
                               AdhesionService adhesionService) {
        this.dossierRepo = dossierRepo;
        this.pvRepo = pvRepo;
        this.pieceService = pieceService;
        this.adhesionService = adhesionService;
    }

    @GetMapping
    public String page(@ModelAttribute("wiz") AdhesionWizard wiz, Model model, Principal principal) {
        UUID draftId = UUID.fromString(wiz.draftId);
        AdhesionDossier d = dossierRepo.findByDraftId(draftId).orElseThrow(() -> new IllegalArgumentException("Draft introuvable"));

        if (d.getStatut() != AdhesionStatut.COMPLEMENT) {
            model.addAttribute("error", "Aucun complément requis pour ce dossier.");
            return "redirect:/adhesion/suivi";
        }

        wiz.dossierRef = d.getDossierRef();
        wiz.statut = d.getStatut().name();

        model.addAttribute("dossier", d);
        model.addAttribute("pieces", pvRepo.findByDraftIdOrderByPieceTypeAscVersionNoDesc(draftId));
        return "adhesion/complement";
    }

    @PostMapping("/upload")
    public String upload(@ModelAttribute("wiz") AdhesionWizard wiz,
                         @RequestParam("pieceType") String pieceType,
                         @RequestParam("file") MultipartFile file,
                         Principal principal,
                         Model model) {
        try {
            UUID draftId = UUID.fromString(wiz.draftId);
            AdhesionDossier d = dossierRepo.findByDraftId(draftId).orElseThrow(() -> new IllegalArgumentException("Draft introuvable"));

            if (d.getStatut() != AdhesionStatut.COMPLEMENT) {
                throw new IllegalStateException("Upload autorisé uniquement en COMPLEMENT.");
            }

            pieceService.uploadVersion(draftId, pieceType, file, principal.getName());

            // pièces modifiées => signature invalide
            adhesionService.invalidateSignature(draftId, principal.getName(), "Remplacement pièce en COMPLEMENT");
            wiz.signatureOk = false;

            return "redirect:/adhesion/complement";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "adhesion/complement";
        }
    }

    @PostMapping("/sign")
    public String resign(@ModelAttribute("wiz") AdhesionWizard wiz, Principal principal) {
        AdhesionDossier d = adhesionService.sign(UUID.fromString(wiz.draftId), principal.getName());
        wiz.signatureOk = d.isSignatureOk();
        wiz.hash = d.getSignatureHash();
        wiz.horodatage = d.getHorodatage();
        wiz.certAuthority = d.getCertAuthority();
        wiz.certSerial = d.getCertSerial();
        wiz.certExpiry = d.getCertExpiry();
        return "redirect:/adhesion/complement";
    }

    @PostMapping("/resubmit")
    public String resubmit(@ModelAttribute("wiz") AdhesionWizard wiz, Principal principal, Model model) {
        try {
            AdhesionDossier d = adhesionService.resubmitAfterComplement(UUID.fromString(wiz.draftId), principal.getName());
            wiz.statut = d.getStatut().name();
            return "redirect:/adhesion/etape7";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "adhesion/complement";
        }
    }
}
