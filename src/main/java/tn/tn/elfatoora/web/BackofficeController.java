package tn.tn.elfatoora.web;

import tn.tn.elfatoora.entity.AdhesionDossier;
import tn.tn.elfatoora.repo.AdhesionAuditRepository;
import tn.tn.elfatoora.repo.AdhesionDossierRepository;
import tn.tn.elfatoora.repo.AdhesionPieceVersionRepository;
import tn.tn.elfatoora.service.AdhesionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/bo")
public class BackofficeController {

    private final AdhesionDossierRepository dossierRepo;
    private final AdhesionPieceVersionRepository pvRepo;
    private final AdhesionAuditRepository auditRepo;
    private final AdhesionService adhesionService;

    public BackofficeController(AdhesionDossierRepository dossierRepo,
                                AdhesionPieceVersionRepository pvRepo,
                                AdhesionAuditRepository auditRepo,
                                AdhesionService adhesionService) {
        this.dossierRepo = dossierRepo;
        this.pvRepo = pvRepo;
        this.auditRepo = auditRepo;
        this.adhesionService = adhesionService;
    }

    @GetMapping("/dossiers")
    public String list(@RequestParam(value="ref", required=false) String ref,
                       @RequestParam(value="mf", required=false) String mf,
                       @RequestParam(value="statut", required=false) String statut,
                       Model model) {

        // V1 simple: listes basiques. En prod, tu feras une recherche multi-critères (Specification).
        List<AdhesionDossier> dossiers = dossierRepo.findAll();

        model.addAttribute("dossiers", dossiers);
        return "bo/dossiers";
    }

    @PostMapping("/dossiers/{ref}/take")
    public String take(@PathVariable("ref") String ref, Principal principal) {
        adhesionService.take(ref, principal.getName());
        return "redirect:/bo/dossiers/" + ref;
    }

    @GetMapping("/dossiers/{ref}")
    public String detail(@PathVariable("ref") String ref, Model model) {
        AdhesionDossier d = dossierRepo.findByDossierRef(ref)
                .orElseThrow(() -> new IllegalArgumentException("Dossier introuvable"));
        model.addAttribute("dossier", d);
        model.addAttribute("pieces", pvRepo.findByDraftIdOrderByPieceTypeAscVersionNoDesc(d.getDraftId()));
        model.addAttribute("audit", auditRepo.findByDraftIdOrderByCreatedAtDesc(d.getDraftId()));
        return "bo/dossier-detail";
    }

    @PostMapping("/dossiers/{ref}/decision")
    public String decision(@PathVariable("ref") String ref,
                           @RequestParam("decision") String decision,
                           @RequestParam(value="motif", required=false) String motif,
                           Principal principal,
                           Model model) {
        try {
            adhesionService.decide(ref, decision, motif, principal.getName());
            return "redirect:/bo/dossiers/" + ref;
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/bo/dossiers/" + ref;
        }
    }
}
