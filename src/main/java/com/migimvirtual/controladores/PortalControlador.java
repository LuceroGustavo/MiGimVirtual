package com.migimvirtual.controladores;

import com.migimvirtual.entidades.Exercise;
import com.migimvirtual.servicios.ConfiguracionPaginaPublicaService;
import com.migimvirtual.servicios.PlanPublicoService;
import com.migimvirtual.servicios.UsuarioService;
import com.migimvirtual.servicios.ExerciseService;
import com.migimvirtual.servicios.ProfesorService;
import com.migimvirtual.config.OpenGraphBrandLogo;
import com.migimvirtual.config.PublicBaseUrlResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

@Controller
@RequestMapping("/")
public class PortalControlador {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private ProfesorService profesorService;

    @Autowired
    private PlanPublicoService planPublicoService;

    @Autowired
    private ConfiguracionPaginaPublicaService configuracionPaginaPublicaService;

    @Autowired
    private PublicBaseUrlResolver publicBaseUrlResolver;

    /** Página de inicio: landing pública (estilo RedFit). Acceso a la parte privada por ícono de login arriba. */
    @GetMapping("/")
    public String index(Model model, HttpServletRequest request) {
        try {
            com.migimvirtual.entidades.Usuario usuarioActual = usuarioService.getUsuarioActual();
            if (usuarioActual != null) {
                model.addAttribute("usuarioActual", usuarioActual);
            }
        } catch (Exception e) {
            // Usuario no autenticado
        }
        configuracionPaginaPublicaService.rellenarModeloPaginaPublica(model);
        rellenarOpenGraph(model, request, "/",
                "MiGymVirtual — Envío de rutinas y seguimiento virtual",
                "Plataforma para profesores: rutinas personalizadas, seguimiento de alumnos y entrenamiento online donde estés.");
        return "index-publica";
    }

    /** Ruta alternativa que también muestra la misma página pública (por si se enlaza desde algún lado). */
    @GetMapping("/publica")
    public String indexPublica(Model model, HttpServletRequest request) {
        return index(model, request);
    }

    /** Página de planes (pública). Cards con precios, servicios, días/horarios y formulario de consulta. */
    @GetMapping("/planes")
    public String planes(Model model, HttpServletRequest request) {
        configuracionPaginaPublicaService.rellenarModeloPaginaPublica(model);
        model.addAttribute("planes", planPublicoService.getPlanesActivosParaPublica());
        rellenarOpenGraph(model, request, "/planes",
                "Planes y servicios — MiGymVirtual",
                "Consultá opciones de entrenamiento, seguimiento virtual y rutinas personalizadas para vos o tu equipo.");
        return "planes-publica";
    }

    /** URLs absolutas y textos para Open Graph / WhatsApp (imagen liviana vía {@link com.migimvirtual.config.OpenGraphBrandLogo}, no el PNG grande del navbar). */
    private void rellenarOpenGraph(Model model, HttpServletRequest request, String pathRelativo,
                                         String ogTitle, String ogDescription) {
        String baseUrl = publicBaseUrlResolver.resolvePublicBaseUrl(request);
        String path = pathRelativo.startsWith("/") ? pathRelativo : "/" + pathRelativo;
        model.addAttribute("ogTitle", ogTitle);
        model.addAttribute("ogDescription", ogDescription);
        OpenGraphBrandLogo.addLogoToModel(model, baseUrl);
        model.addAttribute("ogPageUrl", baseUrl + path);
    }

    /* GET /login lo maneja WebMvcConfig (view "login") para mostrar siempre la plantilla Iniciar Sesión. */

    @GetMapping("/demo")
    public String demo() {
        return "demo";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            com.migimvirtual.entidades.Usuario usuarioActual = usuarioService.getUsuarioActual();
            if (usuarioActual != null && ("ADMIN".equals(usuarioActual.getRol()) || "AYUDANTE".equals(usuarioActual.getRol()) || "DEVELOPER".equals(usuarioActual.getRol()))) {
                com.migimvirtual.entidades.Profesor p = usuarioActual.getProfesor() != null ? usuarioActual.getProfesor() : profesorService.getProfesorByCorreo(usuarioActual.getCorreo());
                if (p == null && "DEVELOPER".equals(usuarioActual.getRol())) {
                    p = profesorService.getProfesorByCorreo("profesor@migymvirtual.com");
                }
                if (p != null) return "redirect:/profesor/" + p.getId();
                return "redirect:/profesor/dashboard";
            }
        } catch (Exception e) {
            // Usuario no autenticado
        }
        
        // Fallback: redirigir a la página principal
        return "redirect:/";
    }

    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            // Verificar profesor (único gestor del panel)
            com.migimvirtual.entidades.Profesor profesorPrincipal = profesorService.getProfesorByCorreo("profesor@migymvirtual.com");
            status.put("profesorPrincipalExiste", profesorPrincipal != null);
            if (profesorPrincipal != null) {
                status.put("profesorPrincipalId", profesorPrincipal.getId());
                status.put("profesorPrincipalNombre", profesorPrincipal.getNombre());
                List<Exercise> ejerciciosProfesor = exerciseService.findExercisesByProfesorId(profesorPrincipal.getId());
                status.put("ejerciciosProfesorCount", ejerciciosProfesor.size());
            }
            
            // Verificar archivos estáticos
            try {
                Resource logoResource = new ClassPathResource("static/img/mgvirtual_logo1.png");
                status.put("logoExiste", logoResource.exists());
                status.put("logoPath", "/img/mgvirtual_logo1.png");
                Resource ogShare = new ClassPathResource("static/img/og-share-migymvirtual.jpg");
                status.put("ogShareImageExiste", ogShare.exists());
                status.put("ogShareImagePath", "/img/og-share-migymvirtual.jpg");
                Resource ogRutina = new ClassPathResource("static/img/envio_rutina.png");
                status.put("ogRutinaShareImageExiste", ogRutina.exists());
                status.put("ogRutinaShareImagePath", "/img/envio_rutina.png");
            } catch (Exception e) {
                status.put("logoExiste", false);
                status.put("logoError", e.getMessage());
            }
            
            status.put("status", "OK");
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            status.put("status", "ERROR");
            status.put("error", e.getMessage());
            return ResponseEntity.status(500).body(status);
        }
    }
}
