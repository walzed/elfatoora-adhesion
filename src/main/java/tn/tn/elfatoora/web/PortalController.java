package tn.tn.elfatoora.web;

import tn.tn.elfatoora.repo.AdhesionNotificationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class PortalController {

    private final AdhesionNotificationRepository notifRepo;

    public PortalController(AdhesionNotificationRepository notifRepo) {
        this.notifRepo = notifRepo;
    }

    
    @GetMapping("/")
    public String index() {
        return "portal/home";
    }

    @GetMapping("/myspace")
    public String home() {
        return "portal/espace";
    }
    
    

    @GetMapping("/myspace/notifications")
    public String notifications(Principal principal, Model model) {
        String email = principal.getName();
        model.addAttribute("notifications", notifRepo.findByRecipientEmailOrderByCreatedAtDesc(email));
        return "portal/notifications";
    }
}
