package com.mattfuncional.controladores;

import com.mattfuncional.servicios.ConsultaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/public")
public class PublicoController {

    private final ConsultaService consultaService;

    public PublicoController(ConsultaService consultaService) {
        this.consultaService = consultaService;
    }

    private static final java.util.regex.Pattern EMAIL_PATTERN = java.util.regex.Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /** Formulario de consulta desde la página de planes. Guarda en BD y redirige con mensaje. */
    @PostMapping("/consulta")
    public String enviarConsulta(@RequestParam String nombre,
                                 @RequestParam(required = false) String email,
                                 @RequestParam(required = false) String telefono,
                                 @RequestParam(required = false) String mensaje,
                                 RedirectAttributes redirectAttributes) {
        try {
            String n = nombre != null ? nombre.trim() : "";
            String e = email != null ? email.trim().toLowerCase() : "";
            String t = telefono != null ? telefono.trim() : "";
            if (t.length() > 50) t = t.substring(0, 50);

            if (n.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorConsulta", "Por favor completá tu nombre.");
                return "redirect:/planes#consultanos";
            }
            if (e.isEmpty() && t.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorConsulta", "Ingresá tu email o teléfono para poder contactarte.");
                return "redirect:/planes#consultanos";
            }
            if (!e.isEmpty() && !EMAIL_PATTERN.matcher(e).matches()) {
                redirectAttributes.addFlashAttribute("errorConsulta", "El formato del email no es válido.");
                return "redirect:/planes#consultanos";
            }
            if (!t.isEmpty()) {
                String soloDigitos = t.replaceAll("[^0-9]", "");
                if (soloDigitos.length() < 10) {
                    redirectAttributes.addFlashAttribute("errorConsulta", "El teléfono debe tener al menos 10 dígitos.");
                    return "redirect:/planes#consultanos";
                }
            }

            if (n.length() > 150) n = n.substring(0, 150);
            if (e.length() > 150) e = e.substring(0, 150);
            String telFinal = t.isEmpty() ? null : t;
            String m = mensaje != null && mensaje.length() > 2000 ? mensaje.substring(0, 2000) : (mensaje != null ? mensaje.trim() : "");

            consultaService.guardar(n, e.isEmpty() ? null : e, telFinal, m);
            redirectAttributes.addFlashAttribute("consultaOk", true);
            return "redirect:/planes#planes";
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorConsulta", "No pudimos enviar tu consulta. Por favor intentá de nuevo o contactanos por WhatsApp.");
            return "redirect:/planes#consultanos";
        }
    }
}
