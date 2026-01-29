package tn.tn.elfatoora.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Active le moteur de tâches planifiées Spring (@Scheduled).
 *
 * Sans cette annotation, les méthodes annotées @Scheduled
 * ne seront jamais exécutées.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
