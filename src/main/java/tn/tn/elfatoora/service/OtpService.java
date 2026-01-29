package tn.tn.elfatoora.service;

import tn.tn.elfatoora.entity.EmailOtp;
import tn.tn.elfatoora.repo.EmailOtpRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private final EmailOtpRepository otpRepo;
    private final MailService mailService;

    @Value("${app.otp.length}")
    private int otpLength;

    @Value("${app.otp.ttl.minutes}")
    private int ttlMinutes;

    @Value("${app.otp.max.attempts}")
    private int maxAttempts;

    private final SecureRandom rnd = new SecureRandom();

    public OtpService(EmailOtpRepository otpRepo, MailService mailService) {
        this.otpRepo = otpRepo;
        this.mailService = mailService;
    }

    public String generateNumericCode(int len) {
        int max = (int) Math.pow(10, len);
        int value = rnd.nextInt(max);
        return String.format("%0" + len + "d", value);
    }

    @Transactional
    public void sendOtp(String email) {
        EmailOtp otp = new EmailOtp();
        otp.setEmail(email.toLowerCase());
        otp.setCode(generateNumericCode(otpLength));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        otpRepo.save(otp);

        String body =
                "Bonjour,\n\n" +
                "Voici votre code de vérification El Fatoora : " + otp.getCode() + "\n" +
                "Validité : " + ttlMinutes + " minutes.\n\n" +
                "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n" +
                "TTN – El Fatoora";
        mailService.send(email, "Code de vérification", body);
    }

    @Transactional
    public boolean verify(String email, String code) {
        EmailOtp otp = otpRepo.findTopByEmailOrderByCreatedAtDesc(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Aucun OTP trouvé pour cet email."));

        if (otp.isConsumed()) {
            throw new IllegalStateException("Ce code a déjà été utilisé.");
        }
        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Code expiré. Veuillez demander un nouveau code.");
        }
        if (otp.getAttempts() >= maxAttempts) {
            throw new IllegalStateException("Nombre maximum de tentatives dépassé.");
        }

        otp.setAttempts(otp.getAttempts() + 1);
        otpRepo.save(otp);

        if (!otp.getCode().equals(code)) {
            return false;
        }

        otp.setConsumed(true);
        otpRepo.save(otp);
        return true;
    }
}
