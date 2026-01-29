package tn.tn.elfatoora.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

public class FileValidation {

    private static final long MAX = 10L * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED = Arrays.asList(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    public static void validate(MultipartFile f, String label) {
        if (f == null || f.isEmpty()) {
            throw new IllegalArgumentException("Pièce manquante: " + label);
        }
        if (f.getSize() > MAX) {
            throw new IllegalArgumentException("Fichier trop volumineux (>10MB): " + label);
        }
        String ct = f.getContentType();
        if (ct == null || !ALLOWED.contains(ct)) {
            throw new IllegalArgumentException("Format non autorisé (PDF/JPG/PNG): " + label);
        }
    }
}
