package tn.tn.elfatoora.service;

import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

@Service
public class MonitoringService {

    private final Executor mailExecutor;

    public MonitoringService(Executor mailExecutor) {
        this.mailExecutor = mailExecutor;
    }

    public Map<String, Object> getMailExecutorStats() {
        Map<String, Object> stats = new HashMap<>();

        // Syntaxe Java 8 : Cast manuel
        if (mailExecutor instanceof ThreadPoolTaskExecutor) {
            ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) mailExecutor;
            stats.put("active_threads", executor.getActiveCount());
            stats.put("pool_size", executor.getPoolSize());
            stats.put("queue_size", executor.getThreadPoolExecutor().getQueue().size());
            stats.put("completed_tasks", executor.getThreadPoolExecutor().getCompletedTaskCount());
        }
        return stats;
    }
}