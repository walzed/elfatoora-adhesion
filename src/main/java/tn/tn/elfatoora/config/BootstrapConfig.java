package tn.tn.elfatoora.config;

import tn.tn.elfatoora.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BootstrapConfig {

    @Bean
    public CommandLineRunner init(UserService userService) {
        return args -> {
            // Agent TTN de test
            userService.createAgentIfNeeded("walid.zeddini@tradenet.com.tn", "98914989","@gent123TTNO!");
         // Client de test
            userService.registerClient("walid.zeddini@gmail.com", "94602545","@gent123TTNO!");
        // Admin de test
            userService.createAdminIfNeeded("admin@fatoora.tn", "94602521","@gent123TTNO!");
          
        };
    }
}
