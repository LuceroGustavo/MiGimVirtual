package com.migimvirtual.controladores;

import com.migimvirtual.entidades.ConfiguracionPaginaPublica;
import com.migimvirtual.entidades.PlanPublico;
import com.migimvirtual.entidades.Usuario;
import com.migimvirtual.servicios.ConfiguracionPaginaPublicaService;
import com.migimvirtual.servicios.ConsultaService;
import com.migimvirtual.servicios.PlanPublicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/profesor/pagina-publica")
public class PaginaPublicaAdminController {

    @Autowired
    private PlanPublicoService planPublicoService;

    @Autowired
    private ConfiguracionPaginaPublicaService configuracionPaginaPublicaService;

    @Autowired
    private ConsultaService consultaService;

    @GetMapping
    public String verPaginaPublica(@AuthenticationPrincipal Usuario usuarioActual,
                                   @RequestParam(name = "fragment", required = false) String fragment,
                                   Model model) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        model.addAttribute("planes", planPublicoService.getAllPlanes());
        model.addAttribute("config", configuracionPaginaPublicaService.getAllConfig());
        model.addAttribute("consultas", consultaService.getUltimasConsultas(20));
        if (fragment != null && !fragment.isEmpty()) {
            return "profesor/pagina-publica-admin :: contenido";
        }
        return "redirect:/profesor/administracion?seccion=pagina";
    }

    @PostMapping("/plan/guardar")
    public String guardarPlan(@AuthenticationPrincipal Usuario usuarioActual,
                              @RequestParam(required = false) Long id,
                              @RequestParam String nombre,
                              @RequestParam(required = false) String descripcion,
                              @RequestParam Double precio,
                              @RequestParam(required = false) Integer vecesPorSemana,
                              @RequestParam(defaultValue = "0") int orden,
                              RedirectAttributes ra) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        PlanPublico plan = id != null ? planPublicoService.getById(id) : new PlanPublico();
        if (plan == null) plan = new PlanPublico();
        plan.setNombre(nombre);
        plan.setDescripcion(descripcion != null ? descripcion : "");
        plan.setPrecio(precio);
        plan.setVecesPorSemana(vecesPorSemana);
        plan.setOrden(orden);
        plan.setActivo(true);
        planPublicoService.guardar(plan);
        ra.addFlashAttribute("ok", "Plan guardado");
        return "redirect:/profesor/administracion?seccion=pagina";
    }

    @PostMapping("/plan/eliminar")
    public String eliminarPlan(@AuthenticationPrincipal Usuario usuarioActual,
                              @RequestParam Long id,
                              RedirectAttributes ra) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        planPublicoService.eliminar(id);
        ra.addFlashAttribute("ok", "Plan eliminado");
        return "redirect:/profesor/administracion?seccion=pagina";
    }

    @PostMapping("/plan/subir/{id}")
    public String subirPlan(@AuthenticationPrincipal Usuario usuarioActual,
                           @PathVariable Long id,
                           RedirectAttributes ra) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        planPublicoService.moverArriba(id);
        ra.addFlashAttribute("ok", "Orden actualizado");
        return "redirect:/profesor/administracion?seccion=pagina";
    }

    @PostMapping("/plan/bajar/{id}")
    public String bajarPlan(@AuthenticationPrincipal Usuario usuarioActual,
                           @PathVariable Long id,
                           RedirectAttributes ra) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        planPublicoService.moverAbajo(id);
        ra.addFlashAttribute("ok", "Orden actualizado");
        return "redirect:/profesor/administracion?seccion=pagina";
    }

    @PostMapping("/consulta/eliminar")
    public String eliminarConsulta(@AuthenticationPrincipal Usuario usuarioActual,
                                   @RequestParam Long id,
                                   RedirectAttributes ra) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        consultaService.eliminar(id);
        ra.addFlashAttribute("ok", "Consulta eliminada");
        return "redirect:/profesor/administracion?seccion=pagina";
    }

    @PostMapping("/config")
    public String guardarConfig(@AuthenticationPrincipal Usuario usuarioActual,
                                @RequestParam(required = false) String whatsapp,
                                @RequestParam(required = false) String instagram,
                                @RequestParam(required = false) String direccion,
                                @RequestParam(required = false) String dias_horarios,
                                @RequestParam(required = false) String telefono,
                                @RequestParam(required = false) String url_mapa,
                                @RequestParam(required = false) String tiktok,
                                @RequestParam(required = false) String youtube,
                                @RequestParam(required = false) String facebook,
                                @RequestParam(required = false) String linkedin,
                                @RequestParam(required = false) String twitter,
                                @RequestParam(required = false) String email_contacto,
                                @RequestParam(required = false) String eslogan,
                                RedirectAttributes ra) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_WHATSAPP, whatsapp);
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_INSTAGRAM, instagram);
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_DIRECCION, direccion);
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_DIAS_HORARIOS, dias_horarios);
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_TELEFONO, telefono);
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_URL_MAPA, url_mapa);
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_TIKTOK, tiktok);
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_YOUTUBE, youtube);
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_FACEBOOK, facebook);
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_LINKEDIN, linkedin);
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_TWITTER, twitter);
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_EMAIL_CONTACTO, email_contacto);
        configuracionPaginaPublicaService.actualizar(ConfiguracionPaginaPublica.CLAVE_ESLOGAN, eslogan);
        ra.addFlashAttribute("ok", "Configuración guardada");
        return "redirect:/profesor/administracion?seccion=pagina";
    }

    @PostMapping("/consulta/marcar-visto/{id}")
    public String marcarConsultaVistoPost(@AuthenticationPrincipal Usuario usuarioActual,
                                          @PathVariable Long id,
                                          RedirectAttributes ra) {
        return marcarConsultaVisto(usuarioActual, id, ra);
    }

    @GetMapping("/consulta/marcar-visto/{id}")
    public String marcarConsultaVistoGet(@AuthenticationPrincipal Usuario usuarioActual,
                                         @PathVariable Long id,
                                         RedirectAttributes ra) {
        return marcarConsultaVisto(usuarioActual, id, ra);
    }

    private String marcarConsultaVisto(Usuario usuarioActual, Long id, RedirectAttributes ra) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        consultaService.marcarComoVisto(id);
        ra.addFlashAttribute("ok", "Consulta marcada como vista");
        return "redirect:/profesor/administracion?seccion=pagina";
    }

    private boolean isAdminOrDeveloper(Usuario u) {
        return u != null && ("ADMIN".equals(u.getRol()) || "DEVELOPER".equals(u.getRol()));
    }
}
