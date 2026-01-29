package tn.tn.elfatoora.web;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {

        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Integer status = null;
        if (statusObj != null) {
            try { status = Integer.valueOf(statusObj.toString()); } catch (Exception ignored) {}
        }

        String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        String requestId = (String) request.getAttribute("requestId");
        if (requestId == null) {
            requestId = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            request.setAttribute("requestId", requestId);
        }

        model.addAttribute("timestamp", LocalDateTime.now());
        model.addAttribute("status", status != null ? status : 500);
        model.addAttribute("path", path != null ? path : "");
        model.addAttribute("message", message);
        model.addAttribute("requestId", requestId);

        if (status == null) return "error/500";

        switch (status) {
            case 400: return "error/400";
            case 401: return "error/4xx";   
            case 403: return "error/403";
            case 404: return "error/404";
            case 405: return "error/4xx";
            case 409: return "error/4xx";
            case 429: return "error/429";
            case 503: return "error/503";
            default:
                if (status >= 500) return "error/5xx";  // fallback serveur
                if (status >= 400) return "error/4xx";  // fallback client
                return "error/500";
        }
    }
}
