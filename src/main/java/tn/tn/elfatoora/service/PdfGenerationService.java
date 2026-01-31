package tn.tn.elfatoora.service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tn.tn.elfatoora.entity.AdhesionDossier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class PdfGenerationService {

    public byte[] generateAdhesionPdf(AdhesionDossier dossier) throws JRException, IOException {
        // 1. Charger le fichier .jrxml
        ClassPathResource reportResource = new ClassPathResource("static/jrxml/form-adhesion-elf.jrxml");
        InputStream reportStream = reportResource.getInputStream();

        // 2. Compiler le rapport
        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // 3. Préparer les données
        // On encapsule l'objet dossier dans une liste pour JRBeanCollectionDataSource
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(dossier));

        // 4. Paramètres (si nécessaire, ici vide)
        Map<String, Object> parameters = new HashMap<>();
        
        // 5. Remplir le rapport
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        // 6. Exporter en PDF (byte array)
        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
