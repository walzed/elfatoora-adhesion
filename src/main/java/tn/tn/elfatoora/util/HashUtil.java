package tn.tn.elfatoora.util;

import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;

public class HashUtil {
    public static String sha256(MultipartFile file) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(file.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
