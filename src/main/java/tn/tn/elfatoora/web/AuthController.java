package tn.tn.elfatoora.web;

import tn.tn.elfatoora.service.OtpService;
import tn.tn.elfatoora.service.PasswordResetService;
import tn.tn.elfatoora.service.SmsService;
import tn.tn.elfatoora.service.UserService;

import java.net.URLEncoder;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;
    private final PasswordResetService passwordResetService;

    public AuthController(UserService userService, OtpService otpService, PasswordResetService passwordResetService) {
        this.userService = userService;
        this.otpService = otpService;
        this.passwordResetService = passwordResetService;
    }

 

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String phoneNumber,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           Model model) {
        try {
            if (!password.equals(confirmPassword)) {
                model.addAttribute("error", "Les mots de passe ne correspondent pas.");
                return "auth/register";
            }
            userService.registerClient(email, phoneNumber, password);

             otpService.sendOtp(email, phoneNumber);

            model.addAttribute("email", email.toLowerCase());
            model.addAttribute("phoneNumber", phoneNumber);
            model.addAttribute("success", "Un code vous a été envoyé par SMS.");
            return "auth/verify-otp";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/verify")
    public String verifyPage(@RequestParam(required = false) String email,
                             @RequestParam String phoneNumber,
                             Model model) {
        if (email != null) model.addAttribute("email", email);
        if (phoneNumber != null) model.addAttribute("phoneNumber", phoneNumber);
        return "auth/verify-otp";
    }

    @PostMapping("/verify")
    public String verify(@RequestParam String email,
                         @RequestParam String phoneNumber,
                         @RequestParam String otp,
                         Model model) {
        try {
            boolean ok = otpService.verify(email, otp);
            if (!ok) {
                model.addAttribute("email", email);
                model.addAttribute("phoneNumber", phoneNumber);
                model.addAttribute("error", "Code incorrect.");
                return "auth/verify-otp";
            }

            userService.markEmailVerified(email);
            model.addAttribute("success", "Email vérifié. Vous pouvez vous connecter.");
            return "auth/login";

        } catch (Exception e) {
            model.addAttribute("email", email);
            model.addAttribute("phoneNumber", phoneNumber);
            model.addAttribute("error", e.getMessage());
            return "auth/verify-otp";
        }
    }

    @PostMapping("/resend")
    public String resend(@RequestParam String email,
                         @RequestParam String phoneNumber,
                         Model model) {
        try {
            otpService.sendOtp(email, phoneNumber);
            model.addAttribute("email", email);
            model.addAttribute("phoneNumber", phoneNumber);
            model.addAttribute("success", "Nouveau code envoyé.");
            return "auth/verify-otp";
        } catch (Exception e) {
            model.addAttribute("email", email);
            model.addAttribute("error", e.getMessage());
            return "auth/verify-otp";
        }
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }
    
    @GetMapping("/forgot")
    public String forgotPage() {
        return "auth/forgot-password";
    }

    @PostMapping("/forgot")
    public String forgotSubmit(@RequestParam String email,
                               @RequestParam String phoneNumber,
                               Model model) {
        String normalized = (email == null) ? "" : email.trim().toLowerCase();

        if (normalized.isEmpty()) {
            model.addAttribute("error", "Veuillez saisir une adresse email.");
            return "auth/forgot-password";
        }

        try {
            otpService.sendOtp(normalized, phoneNumber);
        } catch (Exception ignored) {
            // anti-énumération
        }

        try {
            model.addAttribute("phoneNumber", phoneNumber); // Crucial pour l'affichage
            return "redirect:/auth/reset/otp?email=" + URLEncoder.encode(normalized, "UTF-8")
                    + "&phoneNumber=" + phoneNumber;

        } catch (Exception e) {
            model.addAttribute("success", true);
            return "auth/forgot-password";
        }
    }

    @GetMapping("/reset/otp")
    public String resetOtpPage(@RequestParam(required = false) String email, Model model) {
        if (email != null) model.addAttribute("email", email.trim().toLowerCase());
        return "auth/reset-verify-otp";
    }

    @PostMapping("/reset/otp")
    public String resetOtp(@RequestParam String email,
                           @RequestParam String otp,
                           Model model) {

        String normalized = (email == null) ? "" : email.trim().toLowerCase();
        String code = (otp == null) ? "" : otp.trim();

        if (normalized.isEmpty()) {
            model.addAttribute("error", "Email obligatoire.");
            return "auth/reset-verify-otp";
        }
        if (code.isEmpty()) {
            model.addAttribute("email", normalized);
            model.addAttribute("error", "Code obligatoire.");
            return "auth/reset-verify-otp";
        }

        try {
            boolean ok = otpService.verify(normalized, code);
            if (!ok) {
                model.addAttribute("email", normalized);
                model.addAttribute("error", "Code incorrect.");
                return "auth/reset-verify-otp";
            }

            String token = passwordResetService.createResetToken(normalized);

            // anti-énumération : si email inconnu, on renvoie un message neutre
            if (token == null) {
                model.addAttribute("success",
                        "Si l’adresse existe, vous recevrez la suite de la procédure.");
                return "auth/login";
            }

            return "redirect:/auth/reset?token=" + token;

        } catch (Exception e) {
            model.addAttribute("email", normalized);
            model.addAttribute("error", "Erreur lors de la vérification.");
            return "auth/reset-verify-otp";
        }
    }

    @GetMapping("/reset")
    public String resetPage(@RequestParam String token, Model model) {
        boolean ok = passwordResetService.isTokenUsable(token);
        if (!ok) {
            model.addAttribute("error", "Lien de réinitialisation invalide ou expiré.");
            return "auth/login";
        }
        model.addAttribute("token", token);
        return "auth/reset-password";
    }

    @PostMapping("/reset")
    public String resetSubmit(@RequestParam String token,
                              @RequestParam String password,
                              @RequestParam String confirmPassword,
                              Model model) {

        String p1 = (password == null) ? "" : password.trim();
        String p2 = (confirmPassword == null) ? "" : confirmPassword.trim();

        if (p1.isEmpty()) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Mot de passe obligatoire.");
            return "auth/reset-password";
        }
        if (!p1.equals(p2)) {
            model.addAttribute("token", token);
            model.addAttribute("error", "Les mots de passe ne correspondent pas.");
            return "auth/reset-password";
        }

        try {
            passwordResetService.resetPassword(token, p1);
            model.addAttribute("success", "Mot de passe modifié. Vous pouvez vous connecter.");
            return "auth/login";
        } catch (Exception e) {
            model.addAttribute("token", token);
            model.addAttribute("error", e.getMessage());
            return "auth/reset-password";
        }
    }

}
