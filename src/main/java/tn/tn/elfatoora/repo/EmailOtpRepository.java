package tn.tn.elfatoora.repo;

import tn.tn.elfatoora.entity.SmsOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<SmsOtp, Long> {
    Optional<SmsOtp> findTopByEmailOrderByCreatedAtDesc(String email);
    long deleteByExpiresAtBefore(LocalDateTime t);
}
