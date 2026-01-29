package tn.tn.elfatoora.security;

import tn.tn.elfatoora.entity.AppUser;
import tn.tn.elfatoora.repo.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepo;

    public DbUserDetailsService(AppUserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AppUser u = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Compte non vérifié => on bloque la connexion
        if (!u.isEmailVerified()) {
            throw new UsernameNotFoundException("Email not verified");
        }

        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPasswordHash(),
                u.isEnabled(),
                true,
                true,
                !u.isLocked(),
                u.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority(r))
                        .collect(Collectors.toList())
        );
    }
}
