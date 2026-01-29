package tn.tn.elfatoora.service;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import tn.tn.elfatoora.entity.AdhesionPieceVersion;
import tn.tn.elfatoora.repo.AdhesionPieceVersionRepository;

@Service
public class PieceServiceImpl implements PieceService {

    private final AdhesionPieceVersionRepository pieceRepo;
    private final MinioStorageService minioStorageService;

    /**
     * valeur dans application.properties:
     * piece.upload.max-file-size=1MB
     */
    @Value("${piece.upload.max-file-size}")
    private String maxFileSizeConfig;

    public PieceServiceImpl(AdhesionPieceVersionRepository pieceRepo,
                            MinioStorageService minioStorageService) {
        this.pieceRepo = pieceRepo;
        this.minioStorageService = minioStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, AdhesionPieceVersion> latestByDraftId(UUID draftId) {

        if (draftId == null) {
            return new LinkedHashMap<String, AdhesionPieceVersion>();
        }

        List<AdhesionPieceVersion> active = pieceRepo.findActiveByDraftId(draftId);

        Map<String, AdhesionPieceVersion> out = new LinkedHashMap<String, AdhesionPieceVersion>();
        if (active == null) return out;

        for (AdhesionPieceVersion pv : active) {
            if (pv == null) continue;

            String type = normalizeType(pv.getPieceType());
            if (type.isEmpty()) continue;

            AdhesionPieceVersion current = out.get(type);
            if (current == null) {
                out.put(type, pv);
            } else {
                Integer v1 = current.getVersionNo();
                Integer v2 = pv.getVersionNo();
                int a = (v1 == null) ? 0 : v1.intValue();
                int b = (v2 == null) ? 0 : v2.intValue();
                if (b > a) out.put(type, pv);
            }
        }

        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdhesionPieceVersion> listByDraftId(UUID draftId) {
        if (draftId == null) {
            return java.util.Collections.emptyList();
        }
        return pieceRepo.findByDraftIdOrderByUploadedAtDesc(draftId);
    }

    @Override
    @Transactional
    public AdhesionPieceVersion uploadVersion(UUID draftId, String pieceType, MultipartFile file, String actor) throws Exception {

        if (draftId == null) {
            throw new IllegalArgumentException("draftId requis");
        }

        String type = normalizeType(pieceType);
        if (type.isEmpty()) {
            throw new IllegalArgumentException("pieceType requis");
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Fichier manquant pour " + type);
        }

        validateFile(file);

        // s’assurer que le bucket existe avant upload
        minioStorageService.ensureBucket();

        // 1) Supersede toutes les versions ACTIVE existantes pour ce draft/type
        pieceRepo.supersedeActiveByDraftIdAndType(draftId, type, "SUPERSEDED");

        // 2) Prochaine version
        int nextVersion = pieceRepo.maxVersion(draftId, type) + 1;

        // 3) objectKey
        String ext = safeExt(file.getOriginalFilename());
        String objectKey = draftId.toString() + "/" + type + "/v" + nextVersion + "-" + UUID.randomUUID().toString() + ext;

        // 4) Upload + SHA-256 (ta méthode)
        String sha256 = minioStorageService.uploadAndComputeHash(objectKey, file);

        // 5) Save DB
        AdhesionPieceVersion pv = new AdhesionPieceVersion();
        pv.setDraftId(draftId);
        pv.setPieceType(type);
        pv.setVersionNo(nextVersion);
        pv.setStatus("ACTIVE");

        pv.setOriginalFilename(file.getOriginalFilename());
        pv.setContentType(file.getContentType());
        pv.setSizeBytes(file.getSize());

        pv.setObjectKey(objectKey);
        pv.setSha256(sha256);

        pv.setUploadedBy(actor);

        return pieceRepo.save(pv);
    }

    @Override
    @Transactional(readOnly = true)
    public InputStream download(String objectKey) throws Exception {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            throw new IllegalArgumentException("objectKey requis");
        }
        return minioStorageService.download(objectKey);
    }

    private static String normalizeType(String s) {
        if (s == null) return "";
        return s.trim().toUpperCase();
    }

    private void validateFile(MultipartFile file) {

        // 1) Taille (via property)
        long maxBytes = parseMaxBytesFromConfig(maxFileSizeConfig);
        if (maxBytes > 0 && file.getSize() > maxBytes) {
            throw new IllegalArgumentException("Fichier trop volumineux (max " + maxFileSizeConfig + ").");
        }

        // 2) Extension
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        boolean okExt = name.endsWith(".pdf") || name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg");
        if (!okExt) {
            throw new IllegalArgumentException("Format non supporté. Formats acceptés: PDF, PNG, JPG.");
        }
    }

    /**
     * Parse une config telle que:
     * - "1MB" / "5mb"
     * - "1" (supposé MB)
     * - "2M" / "2m"
     * Retour en bytes.
     */
    private static long parseMaxBytesFromConfig(String cfg) {

        if (cfg == null) return 0;
        String s = cfg.trim();
        if (s.isEmpty()) return 0;

        String up = s.toUpperCase();

        // Cas purement numérique: "1" => 1 MB
        boolean numericOnly = up.matches("^[0-9]+$");
        if (numericOnly) {
            long mb = safeParseLong(up, 0);
            return mb * 1024L * 1024L;
        }

        // Cas "1MB", "2M"
        if (up.endsWith("MB")) {
            String n = up.substring(0, up.length() - 2).trim();
            long mb = safeParseLong(n, 0);
            return mb * 1024L * 1024L;
        }
        if (up.endsWith("M")) {
            String n = up.substring(0, up.length() - 1).trim();
            long mb = safeParseLong(n, 0);
            return mb * 1024L * 1024L;
        }

        // Optionnel: accepter KB / GB si un jour tu en as besoin
        if (up.endsWith("KB")) {
            String n = up.substring(0, up.length() - 2).trim();
            long kb = safeParseLong(n, 0);
            return kb * 1024L;
        }
        if (up.endsWith("GB")) {
            String n = up.substring(0, up.length() - 2).trim();
            long gb = safeParseLong(n, 0);
            return gb * 1024L * 1024L * 1024L;
        }

        // Si format inconnu, on n’applique pas de limite côté service
        return 0;
    }

    private static long safeParseLong(String s, long def) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return def;
        }
    }

    private static String safeExt(String filename) {
        if (filename == null) return "";
        int idx = filename.lastIndexOf('.');
        if (idx < 0) return "";
        String ext = filename.substring(idx).toLowerCase();
        if (ext.length() > 10) return "";
        if (!ext.matches("\\.[a-z0-9]+")) return "";
        return ext;
    }
}
