package tn.tn.elfatoora.service;

import tn.tn.elfatoora.entity.SmsOtp;
import tn.tn.elfatoora.repo.EmailOtpRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private final EmailOtpRepository otpRepo;
    private final MailService mailService;
    private final SmsService smsService;
    private final StringRedisTemplate redisTemplate;


    @Value("${app.otp.length}")
    private int otpLength;

    @Value("${app.otp.ttl.minutes}")
    private int ttlMinutes;

    @Value("${app.otp.max.attempts}")
    private int maxAttempts;

    private final SecureRandom rnd = new SecureRandom();

    public OtpService(EmailOtpRepository otpRepo, SmsService smsService, MailService mailService, StringRedisTemplate redisTemplate) {
        this.otpRepo = otpRepo;
        this.smsService = smsService;
        this.mailService = mailService;
        this.redisTemplate = redisTemplate;
    }

    public String generateNumericCode(int len) {
        int max = (int) Math.pow(10, len);
        int value = rnd.nextInt(max);
        return String.format("%0" + len + "d", value);
    }


    public void sendOtp(String email, String phoneNumber) {
        String lockKey = "otp:lock:" + email.toLowerCase();

        // 1. Vérifier Redis AVANT toute opération lourde
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            throw new IllegalStateException("Veuillez patienter 4 minutes avant de demander un nouveau code.");
        }

        // 2. Créer l'OTP en base (On isole la transaction)
        SmsOtp otp = saveOtpInDatabase(email, phoneNumber);

        // 3. Poser le verrou Redis TOUT DE SUITE
        // Comme ça, même si le mail met 3 secondes à partir, l'utilisateur ne peut pas recliquer
        redisTemplate.opsForValue().set(lockKey, "LOCKED", Duration.ofMinutes(4));

        // 4. Envoyer le mail (Idéalement ce sera @Async plus tard)
        String msg =
                "Votre code de confirmation El Fatoora est  : " + otp.getCode() + "\n" +
                        "Validite : " + ttlMinutes + " minutes";
        //mailService.send(email, "Code de vérification", body);
        smsService.sendOTP(phoneNumber, msg);
    }

    @Transactional
    public SmsOtp saveOtpInDatabase(String email, String phoneNumber) {
        SmsOtp otp = new SmsOtp();
        otp.setEmail(email.toLowerCase());
        otp.setPhoneNumber(phoneNumber);
        otp.setCode(generateNumericCode(otpLength));
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        return otpRepo.save(otp);
    }

    @Transactional
    public boolean verify(String email, String code) {
        SmsOtp otp = otpRepo.findTopByEmailOrderByCreatedAtDesc(email.toLowerCase())
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
