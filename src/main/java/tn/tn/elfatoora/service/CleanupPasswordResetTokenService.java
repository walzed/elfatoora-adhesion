package tn.tn.elfatoora.service;

import tn.tn.elfatoora.repo.PasswordResetTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service technique chargé de la purge des tokens de réinitialisation
 * de mot de passe expirés.
 *
 * Objectifs :
 * - Éviter l'accumulation de tokens obsolètes en base
 * - Réduire la surface d'attaque (tokens expirés mais toujours stockés)
 * - Maintenir une base propre et performante
 *
 * Ce service est purement technique et n'expose aucune API publique.
 */
@Service
public class CleanupPasswordResetTokenService {

    private final PasswordResetTokenRepository tokenRepository;

    public CleanupPasswordResetTokenService(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Tâche planifiée exécutée automatiquement par Spring.
     *
     * @Scheduled(cron = "0 0 * * * *")
     *
     * Signification du cron :
     * ┌──────── second (0)
     * │ ┌────── minute (0)
     * │ │ ┌──── heure (* = toutes les heures)
     * │ │ │ ┌── jour du mois (*)
     * │ │ │ │ ┌─ mois (*)
     * │ │ │ │ │ ┌─ jour de la semaine (*)
     * │ │ │ │ │ │
     * 0  0  *  *  *  *
     *
     * 👉 Résultat : la tâche s'exécute à chaque début d’heure
     * (ex: 01:00, 02:00, 03:00, etc.)
     *
     * Ce rythme est largement suffisant car les tokens expirent
     * au bout de 15 minutes.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredResetTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}
