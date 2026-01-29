package tn.tn.elfatoora.web;

import tn.tn.elfatoora.entity.AdhesionPieceVersion;
import tn.tn.elfatoora.repo.AdhesionPieceVersionRepository;
import tn.tn.elfatoora.service.MinioStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;

@Controller
@RequestMapping("/bo/pieces")
public class BackofficePieceController {

    private final AdhesionPieceVersionRepository pvRepo;
    private final MinioStorageService storage;

    public BackofficePieceController(AdhesionPieceVersionRepository pvRepo, MinioStorageService storage) {
        this.pvRepo = pvRepo;
        this.storage = storage;
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<byte[]> download(@PathVariable("id") Long id) throws Exception {
        AdhesionPieceVersion pv = pvRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pièce introuvable"));

        try (InputStream in = storage.download(pv.getObjectKey())) {
            byte[] bytes = org.springframework.util.StreamUtils.copyToByteArray(in);

            String ct = pv.getContentType() != null ? pv.getContentType() : "application/octet-stream";
            String filename = pv.getOriginalFilename() != null ? pv.getOriginalFilename() : "piece";

            return ResponseEntity.ok()
                    .header("Content-Type", ct)
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .body(bytes);
        }
    }
}
