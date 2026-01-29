package tn.tn.elfatoora.web;

import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

	@Value("${piece.upload.max-file-size}")
    private String maxFileSizeConfig;

    @ExceptionHandler({EntityNotFoundException.class, IllegalArgumentException.class})
    public String handleNotFound(Exception ex, HttpServletRequest req, Model model) {
        model.addAttribute("status", 404);
        model.addAttribute("path", req.getRequestURI());
        model.addAttribute("message", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleRse(ResponseStatusException ex, HttpServletRequest req, Model model) {
        int status = ex.getStatus().value();
        model.addAttribute("status", status);
        model.addAttribute("path", req.getRequestURI());
        model.addAttribute("message", ex.getReason());
        if (status == 403) return "error/403";
        if (status == 404) return "error/404";
        if (status >= 500) return "error/500";
        return "error/4xx";
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public String handleMaxSize(org.springframework.web.multipart.MaxUploadSizeExceededException ex,
                                HttpServletRequest req,
                                RedirectAttributes redirectAttributes) {

        // Flash attribute -> survive redirect
        redirectAttributes.addFlashAttribute(
                "error",
                "Fichier trop volumineux. Taille max par fichier : " + maxFileSizeConfig + "."
        );

        return "redirect:/adhesion/etape4";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneric(Exception ex, HttpServletRequest req, Model model) {
        model.addAttribute("status", 500);
        model.addAttribute("path", req.getRequestURI());
        model.addAttribute("message", "Une erreur inattendue s’est produite.");
        return "error/500";
    }
}
