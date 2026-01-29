package tn.tn.elfatoora.service;

import tn.tn.elfatoora.entity.PasswordResetToken;
import tn.tn.elfatoora.repo.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserService userService;
    private final SecureRandom rnd = new SecureRandom();

    public PasswordResetService(PasswordResetTokenRepository tokenRepo, UserService userService) {
        this.tokenRepo = tokenRepo;
        this.userService = userService;
    }

    @Transactional
    public String createResetToken(String email) {
        Long userId = userService.findUserIdByEmail(email);
        if (userId == null) {
            return null; // anti-énumération
        }

        PasswordResetToken t = new PasswordResetToken();
        t.setUserId(userId);
        t.setToken(generateToken());
        t.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        t.setUsed(false);

        tokenRepo.save(t);
        return t.getToken();
    }

    @Transactional(readOnly = true)
    public boolean isTokenUsable(String token) {
        if (token == null || token.trim().isEmpty()) return false;
        return tokenRepo.findByToken(token.trim())
                .filter(PasswordResetToken::isUsable)
                .isPresent();
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken prt = tokenRepo.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Lien de réinitialisation invalide."));

        if (!prt.isUsable()) {
            throw new IllegalStateException("Lien expiré ou déjà utilisé.");
        }

        userService.updatePassword(prt.getUserId(), newPassword);

        prt.setUsed(true);
        tokenRepo.save(prt);
    }

    private String generateToken() {
        byte[] buf = new byte[32];
        rnd.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
