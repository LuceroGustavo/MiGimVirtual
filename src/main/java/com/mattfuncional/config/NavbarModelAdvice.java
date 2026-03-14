package com.mattfuncional.config;

import com.mattfuncional.entidades.Usuario;
import com.mattfuncional.servicios.ConsultaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NavbarModelAdvice {

    private final ConsultaService consultaService;

    public NavbarModelAdvice(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    /**
     * Añade el contador de consultas recibidas (formulario público) al modelo
     * para mostrarlo en el navbar del panel profesor (solo ADMIN y DEVELOPER).
     */
    @ModelAttribute
    public void addConsultaCount(Model model, HttpServletRequest request) {
        if (request == null || !request.getRequestURI().startsWith("/profesor")) {
            return;
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof Usuario)) {
            return;
        }
        Usuario u = (Usuario) auth.getPrincipal();
        if ("ADMIN".equals(u.getRol()) || "DEVELOPER".equals(u.getRol())) {
            model.addAttribute("cantidadConsultas", consultaService.contarNoVistas());
        }
    }
}
