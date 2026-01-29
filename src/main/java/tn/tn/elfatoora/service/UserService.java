package tn.tn.elfatoora.service;

import tn.tn.elfatoora.entity.AppUser;
import tn.tn.elfatoora.repo.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
public class UserService {

    private final AppUserRepository userRepo;
    private final PasswordEncoder encoder;

    public UserService(AppUserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder = encoder;
    }

    @Transactional
    public AppUser registerClient(String email, String rawPassword) {
        String e = email.toLowerCase().trim();

        userRepo.findByEmailIgnoreCase(e).ifPresent(u -> {
            throw new IllegalArgumentException("Cet email est déjà enregistré.");
        });

        AppUser u = new AppUser();
        u.setEmail(e);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setEmailVerified(true);
        u.setEnabled(true);
        u.setLocked(false);
        u.setRoles(Collections.singleton("ROLE_CLIENT"));

        return userRepo.save(u);
    }

    @Transactional
    public void markEmailVerified(String email) {
        AppUser u = userRepo.findByEmailIgnoreCase(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));
        u.setEmailVerified(true);
        userRepo.save(u);
    }

    @Transactional
    public void createAgentIfNeeded(String email, String rawPassword) {
        // Utilitaire pour bootstrap (à enlever en prod)
        String e = email.toLowerCase().trim();
        if (userRepo.findByEmailIgnoreCase(e).isPresent()) return;

        AppUser u = new AppUser();
        u.setEmail(e);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setEmailVerified(true);
        u.setEnabled(true);
        u.setLocked(false);
        u.getRoles().add("ROLE_AGENT_TTN");
        userRepo.save(u);
    }

    @Transactional 
    public void updatePassword(Long userId, String rawPassword) {
        AppUser u = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Compte introuvable."));

        u.setPasswordHash(encoder.encode(rawPassword));
        userRepo.save(u);
    }

    public Long findUserIdByEmail(String email) {
        return userRepo.findByEmailIgnoreCase(email)
                .map(AppUser::getId)
                .orElse(null);
    }
}
