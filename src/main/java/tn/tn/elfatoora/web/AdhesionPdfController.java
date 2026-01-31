package tn.tn.elfatoora.web;

import net.sf.jasperreports.engine.JRException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tn.tn.elfatoora.entity.AdhesionDossier;
import tn.tn.elfatoora.service.AdhesionService;
import tn.tn.elfatoora.service.PdfGenerationService;

import java.io.IOException;

@Controller
@RequestMapping("/adhesion")
public class AdhesionPdfController {

    private final AdhesionService adhesionService;
    private final PdfGenerationService pdfService;

    public AdhesionPdfController(AdhesionService adhesionService, PdfGenerationService pdfService) {
        this.adhesionService = adhesionService;
        this.pdfService = pdfService;
    }

    @GetMapping("/fiche")
    public ResponseEntity<byte[]> viewFiche(@RequestParam("ref") String refOrId, Authentication auth) {
        return generatePdfResponse(refOrId, auth.getName(), true);
    }

    @GetMapping("/fiche/pdf")
    public ResponseEntity<byte[]> downloadFiche(@RequestParam("ref") String refOrId, Authentication auth) {
        return generatePdfResponse(refOrId, auth.getName(), false);
    }

    private ResponseEntity<byte[]> generatePdfResponse(String refOrId, String userEmail, boolean inline) {
        try {
            // 1. Récupérer le dossier par ref ou draftId
            AdhesionDossier dossier = adhesionService.getDossierForUserByRefOrDraftId(userEmail, refOrId);

            // 2. Générer le PDF
            byte[] pdfBytes = pdfService.generateAdhesionPdf(dossier);

            // 3. Déterminer le nom du fichier
            String filename = (dossier.getDossierRef() != null ? dossier.getDossierRef() : dossier.getDraftId().toString()) + ".pdf";

            // 4. Construire la réponse
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            
            // inline = visualisation dans le navigateur, attachment = téléchargement
            String disposition = inline ? "inline" : "attachment";
            headers.setContentDispositionFormData(disposition, filename);
            
            // Optionnel : headers de cache pour éviter de retélécharger si inchangé
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            // Dossier non trouvé ou accès interdit
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (JRException | IOException e) {
            // Erreur technique Jasper
            e.printStackTrace(); // Log l'erreur
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
