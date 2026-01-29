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
            userService.createAgentIfNeeded("walid.zeddini@tradenet.com.tn", "@gent123TTNO!");
         // Client de test
            userService.registerClient("walid.zeddini@gmail.com", "@gent123TTNO!");
            
        };
    }
}
