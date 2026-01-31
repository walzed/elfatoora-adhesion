package tn.tn.elfatoora.security;

import tn.tn.elfatoora.entity.AppUser;
import tn.tn.elfatoora.repo.AppUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepo;

    public DbUserDetailsService(AppUserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        AppUser u = userRepo.findByEmailIgnoreCase(email)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        // Compte non vérifié => on bloque la connexion
//        if (!u.isEmailVerified()) {
//            throw new UsernameNotFoundException("Email not verified");
//        }
//
//        return new org.springframework.security.core.userdetails.User(
//                u.getEmail(),
//                u.getPasswordHash(),
//                u.isEnabled(),
//                true,
//                true,
//                !u.isLocked(),
//                u.getRoles().stream()
//                        .map(r -> new SimpleGrantedAuthority(r))
//                        .collect(Collectors.toList())
//        );
//    }

    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        String identifier = login.trim();
        Optional<AppUser> userOptional;

        // 1. Détection du type d'identifiant
        if (identifier.contains("@")) {
            // C'est probablement un email
            userOptional = userRepo.findByEmailIgnoreCase(identifier);
        } else if (identifier.matches("\\d+")) {
            // C'est une suite de chiffres (numéro de téléphone)
            userOptional = userRepo.findByPhoneNumber(identifier);
        } else {
            // Ni un email, ni un numéro : on peut stopper ici ou tenter une recherche globale
            throw new UsernameNotFoundException("Format d'identifiant invalide : " + identifier);
        }
        // Compte non vérifié => on bloque la connexion
        if(userOptional.get()!=null) {
            if (!userOptional.get().isEmailVerified()) {
                throw new UsernameNotFoundException("Email non encore vérifié ! SMS émis par TTN Fatoora");
            }
        }

        // 2. Mapping vers UserDetails
        return userOptional
                .map(user -> new org.springframework.security.core.userdetails.User(
                        user.getEmail(), // On garde l'email comme principal interne
                        user.getPasswordHash(), //
                        user.isEnabled(), //
                        !user.isLocked(), //
                        true, true,
                        user.getRoles().stream() //
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList())
                ))
                .orElseThrow(() -> new UsernameNotFoundException("L’e-mail ou le numéro de mobile entré n’est pas associé à un compte. Trouvez votre compte et connectez-vous."));
    }
}
