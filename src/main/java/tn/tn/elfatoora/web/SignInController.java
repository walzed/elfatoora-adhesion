package tn.tn.elfatoora.web;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import tn.tn.elfatoora.dto.ReponseOpenIdDTO;
import tn.tn.elfatoora.service.SignInService;

@Controller
@RequestMapping("/sign-in")
public class SignInController {

    private static final Logger logger = LoggerFactory.getLogger(SignInController.class);

    private final SignInService signInService;

    public SignInController(SignInService signInService) {
        this.signInService = signInService;
    }

    @GetMapping("/authorize")
    public String authorize(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);
        String authorizeUrl = signInService.signInAuthorise(baseUrl).getMessage();
        return "redirect:" + authorizeUrl;
    }

    @GetMapping("/callback")
    public String callback(@RequestParam("code") String code,
                           @RequestParam("state") String state,
                           HttpServletRequest request,
                           HttpSession session,
                           RedirectAttributes ra) {
        try {
            String baseUrl = getBaseUrl(request);
            ReponseOpenIdDTO tokens = signInService.postDataToExternalApi(baseUrl, code, state);

            if (tokens == null || tokens.getAccess_token() == null) {
                ra.addFlashAttribute("errorMessage", "Échec d’authentification (token manquant).");
                return "redirect:/auth/login";
            }

            // Stocker dans la session (minimum)
            session.setAttribute("OIDC_STATE", state);
            session.setAttribute("OIDC_ACCESS_TOKEN", tokens.getAccess_token());
            session.setAttribute("OIDC_ID_TOKEN", tokens.getId_token());

            return "redirect:/portal/home"; // adapte vers ta page d’accueil réelle

        } catch (Exception e) {
            logger.error("OIDC callback failed: {}", e.getMessage());
            ra.addFlashAttribute("errorMessage", "Erreur lors de l’authentification.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpSession session, RedirectAttributes ra) {
        String baseUrl = getBaseUrl(request);

        String state = (String) session.getAttribute("OIDC_STATE");
        if (state == null) {
            session.invalidate();
            return "redirect:/";
        }

        String logoutUrl = signInService.buildLogoutUrl(baseUrl, state);

        session.invalidate();

        if (logoutUrl == null) {
            ra.addFlashAttribute("infoMessage", "Déconnexion locale effectuée.");
            return "redirect:/";
        }
        return "redirect:" + logoutUrl;
    }

    private static String getBaseUrl(HttpServletRequest request) {
        // Important si reverse proxy : tu pourras améliorer via headers X-Forwarded-*
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();

        boolean standardPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                            || ("https".equalsIgnoreCase(scheme) && port == 443);

        return standardPort ? (scheme + "://" + host) : (scheme + "://" + host + ":" + port);
    }
}
