package tn.tn.elfatoora.repo;

import tn.tn.elfatoora.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {
    Optional<EmailOtp> findTopByEmailOrderByCreatedAtDesc(String email);
    long deleteByExpiresAtBefore(LocalDateTime t);
}
