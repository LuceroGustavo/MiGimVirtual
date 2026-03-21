package com.migimvirtual.controladores;

import com.migimvirtual.entidades.Profesor;
import com.migimvirtual.entidades.Usuario;
import com.migimvirtual.servicios.DuplicadosCheckService;
import com.migimvirtual.servicios.ProfesorService;
import com.migimvirtual.servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/profesor/usuarios-sistema")
public class UsuariosSistemaController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProfesorService profesorService;

    @Autowired
    private DuplicadosCheckService duplicadosCheckService;

    @GetMapping
    public String verUsuariosSistema(@AuthenticationPrincipal Usuario usuarioActual,
                                     @RequestParam(name = "fragment", required = false) String fragment,
                                     HttpServletRequest request,
                                     Model model) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        model.addAttribute("usuariosSistema", usuarioService.getUsuariosSistemaPara(usuarioActual));
        model.addAttribute("usuarioActual", usuarioActual);
        model.addAttribute("puedeCrearUsuario", isDeveloper(usuarioActual));
        model.addAttribute("puedeGestionarOtros", isDeveloper(usuarioActual));
        model.addAttribute("soloVistaProfesor", !isDeveloper(usuarioActual));
        List<String> correosDuplicados = duplicadosCheckService.getMensajesCorreosDuplicados();
        boolean hayDuplicadosReales = !correosDuplicados.isEmpty();
        if (hayDuplicadosReales && correosDuplicados.size() == 1 && correosDuplicados.get(0).startsWith("No se pudo verificar")) {
            hayDuplicadosReales = false;
        }
        if (hayDuplicadosReales) {
            model.addAttribute("correosDuplicados", correosDuplicados);
        }
        if (fragment != null && !fragment.isEmpty()) {
            String errorMessage = request.getParameter("errorMessage");
            if (errorMessage != null && !errorMessage.isEmpty()) {
                model.addAttribute("errorMessage", errorMessage);
            }
            return "profesor/usuarios-sistema :: contenido";
        }
        return "redirect:/profesor/administracion";
    }

    @GetMapping("/crear")
    public String formularioCrearUsuario(@AuthenticationPrincipal Usuario usuarioActual, Model model) {
        if (usuarioActual == null || !isDeveloper(usuarioActual)) {
            return "redirect:/profesor/administracion?error=solo-developer-crea";
        }
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("usuarioActual", usuarioActual);
        model.addAttribute("editMode", false);
        return "profesor/usuario-sistema-form";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditarUsuario(@PathVariable Long id, @AuthenticationPrincipal Usuario usuarioActual, Model model) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        Usuario usuario = usuarioService.getUsuarioById(id);
        if (usuario == null) {
            return "redirect:/profesor/administracion?error=notfound";
        }
        if (isDeveloper(usuario) && !isDeveloper(usuarioActual)) {
            return "redirect:/profesor/administracion?error=developer-locked";
        }
        boolean esAdminEditandoSoloPerfil = !isDeveloper(usuarioActual) && usuarioActual.getId() != null && usuarioActual.getId().equals(id);
        if (!isDeveloper(usuarioActual) && !esAdminEditandoSoloPerfil) {
            return "redirect:/profesor/administracion?error=solo-developer-edita";
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("usuarioActual", usuarioActual);
        model.addAttribute("editMode", true);
        model.addAttribute("soloPerfilYPassword", esAdminEditandoSoloPerfil);
        return "profesor/usuario-sistema-form";
    }

    @PostMapping("/crear")
    public String crearUsuarioSistema(@AuthenticationPrincipal Usuario usuarioActual,
                                      @RequestParam String nombre,
                                      @RequestParam String correo,
                                      @RequestParam String password,
                                      @RequestParam String rol,
                                      Model model) {
        if (usuarioActual == null || !isDeveloper(usuarioActual)) {
            return "redirect:/profesor/administracion?error=solo-developer-crea";
        }
        try {
            Profesor profesor = usuarioActual.getProfesor() != null
                    ? usuarioActual.getProfesor()
                    : profesorService.getProfesorByCorreo(usuarioActual.getCorreo());
            if (profesor == null) {
                profesor = profesorService.getProfesorByCorreo("profesor@migymvirtual.com");
            }
            usuarioService.crearUsuarioSistema(nombre, correo, password, rol, profesor);
            return "redirect:/profesor/administracion?ok=creado";
        } catch (Exception e) {
            try {
                String msg = e.getMessage() != null ? e.getMessage() : "Error al crear usuario";
                return "redirect:/profesor/administracion?errorMessage=" + URLEncoder.encode(msg, StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException ex) {
                return "redirect:/profesor/administracion?error=crear";
            }
        }
    }

    @PostMapping("/eliminar")
    public String eliminarUsuario(@AuthenticationPrincipal Usuario usuarioActual,
                                  @RequestParam Long usuarioId) {
        if (usuarioActual == null || !isDeveloper(usuarioActual)) {
            return "redirect:/profesor/administracion?error=solo-developer-elimina";
        }
        boolean eliminado = usuarioService.eliminarUsuarioSistema(usuarioId, usuarioActual);
        if (eliminado) {
            if (usuarioActual.getId() != null && usuarioActual.getId().equals(usuarioId)) {
                return "redirect:/login?logout";
            }
            return "redirect:/profesor/administracion?ok=eliminado";
        }
        return "redirect:/profesor/administracion?error=no-permitido";
    }

    @PostMapping("/rol")
    public String actualizarRol(@AuthenticationPrincipal Usuario usuarioActual,
                                @RequestParam Long usuarioId,
                                @RequestParam String rol) {
        if (usuarioActual == null || !isDeveloper(usuarioActual)) {
            return "redirect:/profesor/administracion?error=solo-developer-rol";
        }
        Usuario objetivo = usuarioService.getUsuarioById(usuarioId);
        if (objetivo == null) {
            return "redirect:/profesor/administracion?error=notfound";
        }
        if (isDeveloper(objetivo)) {
            return "redirect:/profesor/administracion?error=developer-locked";
        }
        if (usuarioActual.getId() != null && usuarioActual.getId().equals(usuarioId) && !"ADMIN".equalsIgnoreCase(rol)) {
            return "redirect:/profesor/administracion?error=self-rol";
        }
        usuarioService.actualizarRolUsuario(usuarioId, rol);
        return "redirect:/profesor/administracion?ok=rol";
    }

    @PostMapping("/password")
    public String actualizarPassword(@AuthenticationPrincipal Usuario usuarioActual,
                                     @RequestParam Long usuarioId,
                                     @RequestParam String password) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        Usuario objetivo = usuarioService.getUsuarioById(usuarioId);
        if (objetivo == null) {
            return "redirect:/profesor/administracion?error=notfound";
        }
        if (isDeveloper(objetivo) && !isDeveloper(usuarioActual)) {
            return "redirect:/profesor/administracion?error=developer-locked";
        }
        usuarioService.cambiarPasswordUsuario(usuarioId, password);
        return "redirect:/profesor/administracion?ok=password";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@AuthenticationPrincipal Usuario usuarioActual,
                                   @RequestParam String nombre,
                                   @RequestParam String correo) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        usuarioService.actualizarDatosUsuarioSistema(usuarioActual.getId(), nombre, correo);
        usuarioService.refrescarPrincipalEnSesionSiCorresponde(usuarioActual.getId());
        return "redirect:/profesor/administracion?ok=perfil";
    }

    @PostMapping("/editar")
    public String actualizarUsuarioSistema(@AuthenticationPrincipal Usuario usuarioActual,
                                           @RequestParam Long usuarioId,
                                           @RequestParam String nombre,
                                           @RequestParam String correo,
                                           @RequestParam(required = false) String rol,
                                           @RequestParam(required = false) String password) {
        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/profesor/dashboard";
        }
        boolean esAdminEditandoSoloPerfil = !isDeveloper(usuarioActual) && usuarioActual.getId() != null && usuarioActual.getId().equals(usuarioId);
        if (!isDeveloper(usuarioActual) && !esAdminEditandoSoloPerfil) {
            return "redirect:/profesor/administracion?error=solo-developer-edita";
        }
        Usuario objetivo = usuarioService.getUsuarioById(usuarioId);
        if (objetivo == null) {
            return "redirect:/profesor/administracion?error=notfound";
        }
        if (isDeveloper(objetivo) && !isDeveloper(usuarioActual)) {
            return "redirect:/profesor/administracion?error=developer-locked";
        }
        usuarioService.actualizarDatosUsuarioSistema(usuarioId, nombre, correo);
        if (!esAdminEditandoSoloPerfil && rol != null && !rol.isBlank() && !isDeveloper(objetivo)) {
            if (usuarioActual.getId() != null && usuarioActual.getId().equals(usuarioId) && !"ADMIN".equalsIgnoreCase(rol)) {
                return "redirect:/profesor/administracion?error=self-rol";
            }
            usuarioService.actualizarRolUsuario(usuarioId, rol);
        }
        if (password != null && !password.isBlank()) {
            usuarioService.cambiarPasswordUsuario(usuarioId, password);
        }
        usuarioService.refrescarPrincipalEnSesionSiCorresponde(usuarioId);
        return "redirect:/profesor/administracion?ok=datos";
    }

    private boolean isAdminOrDeveloper(Usuario usuario) {
        return usuario != null && ("ADMIN".equals(usuario.getRol()) || "DEVELOPER".equals(usuario.getRol()));
    }

    private boolean isDeveloper(Usuario usuario) {
        return usuario != null && "DEVELOPER".equals(usuario.getRol());
    }
}
