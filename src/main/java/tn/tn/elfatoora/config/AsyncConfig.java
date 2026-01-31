package tn.tn.elfatoora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5); // 5 threads toujours actifs
        executor.setMaxPoolSize(20); // Jusqu'à 20 threads en cas de pic
        executor.setQueueCapacity(500); // 500 mails en file d'attente max
        executor.setThreadNamePrefix("MailThread-");
        executor.initialize();
        return executor;
    }
}