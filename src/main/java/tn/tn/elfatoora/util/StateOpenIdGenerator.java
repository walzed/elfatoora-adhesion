package tn.tn.elfatoora.util;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class StateOpenIdGenerator {
    private final SecureRandom rnd = new SecureRandom();

    public String generateState() {
        byte[] b = new byte[32];
        rnd.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
