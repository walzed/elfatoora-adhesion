package tn.tn.elfatoora.repo;

import tn.tn.elfatoora.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmailIgnoreCase(String email);

    Optional<AppUser> findByPhoneNumber(String phoneNumber);

    // Recherche par email OU par numéro de téléphone
    Optional<AppUser> findByEmailIgnoreCaseOrPhoneNumber(String email, String phoneNumber);
    Optional<AppUser> findByEmail(String email);
}
