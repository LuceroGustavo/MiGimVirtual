package com.migimvirtual.controladores;

import com.migimvirtual.entidades.Profesor;
import com.migimvirtual.entidades.Usuario;
import com.migimvirtual.servicios.AlumnoJsonBackupService;
import com.migimvirtual.servicios.BackupStorageService;
import com.migimvirtual.servicios.ExerciseZipBackupService;
import com.migimvirtual.servicios.ProfesorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/profesor")
public class AdminPanelController {

    private static final Logger logger = LoggerFactory.getLogger(AdminPanelController.class);

    private final ExerciseZipBackupService exerciseZipBackupService;
    private final ProfesorService profesorService;
    private final AlumnoJsonBackupService alumnoJsonBackupService;
    private final BackupStorageService backupStorageService;

    public AdminPanelController(ExerciseZipBackupService exerciseZipBackupService,
                                ProfesorService profesorService,
                                AlumnoJsonBackupService alumnoJsonBackupService,
                                BackupStorageService backupStorageService) {
        this.exerciseZipBackupService = exerciseZipBackupService;
        this.profesorService = profesorService;
        this.alumnoJsonBackupService = alumnoJsonBackupService;
        this.backupStorageService = backupStorageService;
    }

    private Profesor getProfesorParaUsuario(Usuario usuario) {
        if (usuario == null) return null;
        if ("DEVELOPER".equals(usuario.getRol())) {
            return profesorService.getProfesorByCorreo("profesor@migymvirtual.com");
        }
        if (usuario.getProfesor() != null) return usuario.getProfesor();
        return usuario.getCorreo() != null ? profesorService.getProfesorByCorreo(usuario.getCorreo()) : null;
    }

    @GetMapping("/administracion")
    public String panelAdministracion(@AuthenticationPrincipal Usuario usuarioActual, RedirectAttributes redirectAttributes) {
        if (usuarioActual == null || (!"ADMIN".equals(usuarioActual.getRol()) && !"DEVELOPER".equals(usuarioActual.getRol()))) {
            redirectAttributes.addFlashAttribute("mensajeRestriccionAdmin", true);
            Profesor profesor = getProfesorParaUsuario(usuarioActual);
            if (profesor == null) return "redirect:/profesor/dashboard";
            return "redirect:/profesor/" + profesor.getId();
        }
        return "profesor/administracion";
    }

    @GetMapping("/backup")
    public String paginaBackup(@AuthenticationPrincipal Usuario usuarioActual,
                              @RequestParam(name = "fragment", required = false) String fragment,
                              Model model) {
        if (usuarioActual == null || (!"ADMIN".equals(usuarioActual.getRol()) && !"DEVELOPER".equals(usuarioActual.getRol()))) {
            return "redirect:/profesor/dashboard";
        }
        model.addAttribute("backupsContenido", backupStorageService.listarContenido());
        model.addAttribute("backupsAlumnos", backupStorageService.listarAlumnos());
        model.addAttribute("backupDirectorioAbs", backupStorageService.resolveRoot().toAbsolutePath().toString());
        if (fragment != null && !fragment.isEmpty()) {
            return "profesor/backup :: contenido";
        }
        return "profesor/backup";
    }

    @PostMapping("/backup/guardar-contenido")
    public String guardarBackupContenido(@AuthenticationPrincipal Usuario usuarioActual,
                                         RedirectAttributes redirectAttributes) {
        if (usuarioActual == null || (!"ADMIN".equals(usuarioActual.getRol()) && !"DEVELOPER".equals(usuarioActual.getRol()))) {
            return "redirect:/profesor/dashboard";
        }
        Profesor profesor = getProfesorParaUsuario(usuarioActual);
        if (profesor == null) {
            redirectAttributes.addFlashAttribute("backupContenidoErr", "No se pudo determinar el profesor para el backup de contenido.");
            return "redirect:/profesor/administracion?seccion=backup";
        }
        try {
            byte[] zipBytes = exerciseZipBackupService.exportarEjerciciosAZip(profesor.getId());
            String nombre = backupStorageService.guardarContenidoZip(zipBytes);
            redirectAttributes.addFlashAttribute("backupContenidoMsg", "Backup de contenido guardado en el servidor: " + nombre);
        } catch (Exception e) {
            logger.error("Error al guardar backup ZIP: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("backupContenidoErr", "No se pudo guardar el backup: " + e.getMessage());
        }
        return "redirect:/profesor/administracion?seccion=backup";
    }

    @PostMapping("/backup/guardar-alumnos")
    public String guardarBackupAlumnos(@AuthenticationPrincipal Usuario usuarioActual,
                                       RedirectAttributes redirectAttributes) {
        if (usuarioActual == null || (!"ADMIN".equals(usuarioActual.getRol()) && !"DEVELOPER".equals(usuarioActual.getRol()))) {
            return "redirect:/profesor/dashboard";
        }
        Profesor profesor = getProfesorParaUsuario(usuarioActual);
        if (profesor == null) {
            redirectAttributes.addFlashAttribute("backupAlumnosErr", "No se pudo determinar el profesor.");
            return "redirect:/profesor/administracion?seccion=backup";
        }
        try {
            byte[] jsonBytes = alumnoJsonBackupService.exportarAlumnosAJson(profesor.getId());
            String nombre = backupStorageService.guardarAlumnosJson(jsonBytes);
            redirectAttributes.addFlashAttribute("backupAlumnosMsg", "Backup de alumnos guardado en el servidor: " + nombre);
        } catch (Exception e) {
            logger.error("Error al guardar backup alumnos: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("backupAlumnosErr", "No se pudo guardar el backup: " + e.getMessage());
        }
        return "redirect:/profesor/administracion?seccion=backup";
    }

    @PostMapping("/backup/restaurar-contenido")
    public String restaurarBackupContenido(@AuthenticationPrincipal Usuario usuarioActual,
                                           @RequestParam("nombreArchivo") String nombreArchivo,
                                           RedirectAttributes redirectAttributes) {
        if (usuarioActual == null || (!"ADMIN".equals(usuarioActual.getRol()) && !"DEVELOPER".equals(usuarioActual.getRol()))) {
            return "redirect:/profesor/dashboard";
        }
        Profesor profesor = getProfesorParaUsuario(usuarioActual);
        if (profesor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "No se pudo determinar el profesor.");
            redirectAttributes.addFlashAttribute("importResult", err);
            return "redirect:/profesor/administracion?seccion=backup";
        }
        try {
            byte[] zipBytes = backupStorageService.leerContenido(nombreArchivo);
            Map<String, Object> result = exerciseZipBackupService.importarSnapshotCompletoDesdeZipBytes(zipBytes, profesor);
            redirectAttributes.addFlashAttribute("importResult", result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            redirectAttributes.addFlashAttribute("importResult", err);
        } catch (Exception e) {
            logger.error("Error al restaurar ZIP: {}", e.getMessage(), e);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Error al restaurar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("importResult", err);
        }
        return "redirect:/profesor/administracion?seccion=backup";
    }

    @PostMapping("/backup/restaurar-alumnos")
    public String restaurarBackupAlumnos(@AuthenticationPrincipal Usuario usuarioActual,
                                         @RequestParam("nombreArchivo") String nombreArchivo,
                                         RedirectAttributes redirectAttributes) {
        if (usuarioActual == null || (!"ADMIN".equals(usuarioActual.getRol()) && !"DEVELOPER".equals(usuarioActual.getRol()))) {
            return "redirect:/profesor/dashboard";
        }
        Profesor profesor = getProfesorParaUsuario(usuarioActual);
        if (profesor == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "No se pudo determinar el profesor.");
            redirectAttributes.addFlashAttribute("importAlumnosResult", err);
            return "redirect:/profesor/administracion?seccion=backup";
        }
        try {
            byte[] jsonBytes = backupStorageService.leerAlumnos(nombreArchivo);
            Map<String, Object> result = alumnoJsonBackupService.importarSnapshotCompletoDesdeJsonBytes(jsonBytes, profesor);
            redirectAttributes.addFlashAttribute("importAlumnosResult", result);
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            redirectAttributes.addFlashAttribute("importAlumnosResult", err);
        } catch (Exception e) {
            logger.error("Error al restaurar JSON alumnos: {}", e.getMessage(), e);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Error al restaurar: " + e.getMessage());
            redirectAttributes.addFlashAttribute("importAlumnosResult", err);
        }
        return "redirect:/profesor/administracion?seccion=backup";
    }

}
