package tn.tn.elfatoora.web;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import tn.tn.elfatoora.entity.AdhesionDossier;
import tn.tn.elfatoora.entity.AdhesionSignataire;
import tn.tn.elfatoora.repo.AdhesionDossierRepository;
import tn.tn.elfatoora.service.SignataireService;

import java.time.LocalDate;
import java.util.*;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/adhesion/signataires")
@SessionAttributes("wiz")
public class SignataireController {

    private final SignataireService signataireService;
    private final AdhesionDossierRepository dossierRepo;

   
    public SignataireController(SignataireService signataireService, AdhesionDossierRepository dossierRepo) {
        this.signataireService = signataireService;
        this.dossierRepo = dossierRepo;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<SignataireDto> list(@ModelAttribute("wiz") AdhesionWizard wiz) {
        UUID draftId = (wiz.getDraftIdUUID() != null ? wiz.getDraftIdUUID() : (wiz.draftId != null && !wiz.draftId.trim().isEmpty() ? UUID.fromString(wiz.draftId.trim()) : null));
        if (draftId == null) return Collections.emptyList();

        List<AdhesionSignataire> list = signataireService.listByDraftId(draftId);
// Tri: actifs d'abord (0), puis nom/prénom
        list.sort(Comparator
                .comparing((AdhesionSignataire s) -> s.getDesactivated() != null ? s.getDesactivated() : 0)
                .thenComparing(s -> opt(s.getNom()))
                .thenComparing(s -> opt(s.getPrenom()))
        );

        return list.stream().map(SignataireDto::from).collect(Collectors.toList());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SignataireDto create(@ModelAttribute("wiz") AdhesionWizard wiz, @RequestBody SignatairePayload payload) {

        AdhesionDossier dossier = dossierRepo.findByDraftId(UUID.fromString(wiz.draftId))
                .orElseThrow(() -> new IllegalArgumentException("Draft introuvable"));

        AdhesionSignataire s = new AdhesionSignataire();
        applyPayload(s, payload);
        s.setDesactivated(0);

        // IMPORTANT : draft_id doit être rempli
        s.setDraftId(dossier.getDraftId());
        s.setMatriculeFiscale(dossier.getMatriculeFiscal());
        s.setRcRne(dossier.getRegistreCommerce());

        AdhesionSignataire saved = signataireService.addForDossier(dossier, s);
        return SignataireDto.from(saved);
    }


    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SignataireDto update(@ModelAttribute("wiz") AdhesionWizard wiz,
                               @PathVariable("id") Long id,
                               @RequestBody SignatairePayload payload) {

        AdhesionSignataire s = signataireService.get(id)
                .orElseThrow(() -> new IllegalArgumentException("Signataire introuvable"));

        // S'assure que le signataire appartient au draft en cours
        String draft = (s.getDraftId() != null ? s.getDraftId().toString() : null);

        if (draft == null || !draft.equals(wiz.draftId)) {
            throw new IllegalArgumentException("Accès non autorisé au signataire");
        }

        applyPayload(s, payload);
        AdhesionSignataire saved = signataireService.save(s);
        return SignataireDto.from(saved);
    }

    @PostMapping(value = "/{id}/toggle", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public SignataireDto toggle(@ModelAttribute("wiz") AdhesionWizard wiz,
                                @PathVariable("id") Long id,
                                @RequestParam("desactivated") int desactivated) {

        AdhesionSignataire s = signataireService.get(id)
                .orElseThrow(() -> new IllegalArgumentException("Signataire introuvable"));

        String draft = (s.getDossier() != null && s.getDossier().getDraftId() != null)
                ? s.getDossier().getDraftId().toString()
                : null;

        if (draft == null || !draft.equals(wiz.draftId)) {
            throw new IllegalArgumentException("Accès non autorisé au signataire");
        }

        s.setDesactivated(desactivated == 1 ? 1 : 0);
        AdhesionSignataire saved = signataireService.save(s);
        return SignataireDto.from(saved);
    }

    private static void applyPayload(AdhesionSignataire s, SignatairePayload p) {
        s.setNom(trim(p.nom));
        s.setPrenom(trim(p.prenom));
        s.setCin(trim(p.cin));
        s.setEmail(trim(p.email));
        s.setMatriculeFiscale(trim(p.matriculeFiscale));
        s.setRcRne(trim(p.rcRne));
        s.setCertNumSerie(trim(p.certNumSerie));
        s.setCertDebutValidite(parseDate(p.certDebutValidite));
        s.setCertFinValidite(parseDate(p.certFinValidite));
    }

    private static String trim(String v) { return v == null ? null : v.trim(); }

    private static LocalDate parseDate(String v) {
        if (v == null || v.trim().isEmpty()) return null;
        return LocalDate.parse(v.trim()); // yyyy-MM-dd
    }

    private static String opt(String v) { return v == null ? "" : v; }

    public static class SignatairePayload {
        public String nom;
        public String prenom;
        public String cin;
        public String email;
        public String certNumSerie;
        public String matriculeFiscale;
        public String rcRne;
        public String certDebutValidite;
        public String certFinValidite;
    }

    public static class SignataireDto {
        public Long id;
        public String nom;
        public String prenom;
        public String cin;
        public String email;
        public String certNumSerie;
        public String matriculeFiscale;
        public String rcRne;        
        public String certDebutValidite;
        public String certFinValidite;
        public int desactivated;

        public static SignataireDto from(AdhesionSignataire s) {
            SignataireDto d = new SignataireDto();
            d.id = s.getId();
            d.nom = s.getNom();
            d.prenom = s.getPrenom();
            d.cin = s.getCin();
            d.email = s.getEmail();
            d.matriculeFiscale = s.getMatriculeFiscale();
            d.rcRne = s.getRcRne();
            d.certNumSerie = s.getCertNumSerie();
            d.certDebutValidite = s.getCertDebutValidite() != null ? s.getCertDebutValidite().toString() : null;
            d.certFinValidite = s.getCertFinValidite() != null ? s.getCertFinValidite().toString() : null;
            d.desactivated = (s.getDesactivated() != null ? s.getDesactivated() : 0);
            return d;
        }
    }
}
