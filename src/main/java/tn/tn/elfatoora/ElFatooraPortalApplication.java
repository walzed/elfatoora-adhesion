package tn.tn.elfatoora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync; // Ajout

@SpringBootApplication(scanBasePackages = "tn.tn.elfatoora")
@EnableAsync // Pour Code OTP par mail
@EnableRetry // <--- Active le mécanisme de ré-essai automatique
public class ElFatooraPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElFatooraPortalApplication.class, args);
    }
}