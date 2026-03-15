package com.migimvirtual.controladores;

import com.migimvirtual.entidades.Profesor;
import com.migimvirtual.entidades.Usuario;
import com.migimvirtual.servicios.AlumnoExportService;
import com.migimvirtual.servicios.AlumnoJsonBackupService;
import com.migimvirtual.servicios.ExerciseZipBackupService;
import com.migimvirtual.servicios.ProfesorService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/profesor")
public class AdminPanelController {

    private static final Logger logger = LoggerFactory.getLogger(AdminPanelController.class);
    private static final DateTimeFormatter ZIP_FILENAME_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm");

    private final ExerciseZipBackupService exerciseZipBackupService;
    private final ProfesorService profesorService;
    private final AlumnoExportService alumnoExportService;
    private final AlumnoJsonBackupService alumnoJsonBackupService;

    public AdminPanelController(ExerciseZipBackupService exerciseZipBackupService,
                                ProfesorService profesorService,
                                AlumnoExportService alumnoExportService,
                                AlumnoJsonBackupService alumnoJsonBackupService) {
        this.exerciseZipBackupService = exerciseZipBackupService;
        this.profesorService = profesorService;
        this.alumnoExportService = alumnoExportService;
        this.alumnoJsonBackupService = alumnoJsonBackupService;
    }

    private Profesor getProfesorParaUsuario(Usuario usuario) {
        if (usuario == null) return null;
        if ("DEVELOPER".equals(usuario.getRol())) {
            return profesorService.getProfesorByCorreo("profesor@migimvirtual.com");
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
        if (fragment != null && !fragment.isEmpty()) {
            return "profesor/backup :: contenido";
        }
        return "profesor/backup";
    }

    /**
     * Importa ejercicios desde ZIP (form submit). Redirige con resultado en flash.
     */
    @PostMapping("/backup/importar")
    public String importarBackupZip(@AuthenticationPrincipal Usuario usuarioActual,
                                    @RequestParam("archivoZip") MultipartFile archivoZip,
                                    @RequestParam(value = "pisarTodos", defaultValue = "false") boolean pisarTodos,
                                    @RequestParam(value = "importarGrupos", defaultValue = "true") boolean importarGrupos,
                                    @RequestParam(value = "importarEjercicios", defaultValue = "true") boolean importarEjercicios,
                                    @RequestParam(value = "importarRutinas", defaultValue = "true") boolean importarRutinas,
                                    @RequestParam(value = "importarSeries", defaultValue = "true") boolean importarSeries,
                                    RedirectAttributes redirectAttributes) {
        if (usuarioActual == null || (!"ADMIN".equals(usuarioActual.getRol()) && !"DEVELOPER".equals(usuarioActual.getRol()))) {
            return "redirect:/profesor/dashboard";
        }
        try {
            Profesor profesor = getProfesorParaUsuario(usuarioActual);
            Map<String, Object> result = exerciseZipBackupService.importarDesdeZip(archivoZip, pisarTodos, profesor,
                    importarGrupos, importarEjercicios, importarRutinas, importarSeries);
            redirectAttributes.addFlashAttribute("importResult", result);
        } catch (IOException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Error al leer el archivo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("importResult", err);
        }
        return "redirect:/profesor/administracion?seccion=backup";
    }

    /**
     * Importa ejercicios desde ZIP. Retorna JSON para uso con fetch (alternativa).
     */
    @PostMapping("/backup/importar-ejercicios")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> importarEjerciciosZip(@AuthenticationPrincipal Usuario usuarioActual,
                                                                      @RequestParam("archivoZip") MultipartFile archivoZip,
                                                                      @RequestParam("pisarTodos") boolean pisarTodos,
                                                                      @RequestParam(value = "importarGrupos", defaultValue = "true") boolean importarGrupos,
                                                                      @RequestParam(value = "importarEjercicios", defaultValue = "true") boolean importarEjercicios,
                                                                      @RequestParam(value = "importarRutinas", defaultValue = "true") boolean importarRutinas,
                                                                      @RequestParam(value = "importarSeries", defaultValue = "true") boolean importarSeries) {
        if (usuarioActual == null || (!"ADMIN".equals(usuarioActual.getRol()) && !"DEVELOPER".equals(usuarioActual.getRol()))) {
            return ResponseEntity.status(403).body(Map.of("success", false, "message", "Sin permiso"));
        }
        try {
            Profesor profesor = getProfesorParaUsuario(usuarioActual);
            Map<String, Object> result = exerciseZipBackupService.importarDesdeZip(archivoZip, pisarTodos, profesor,
                    importarGrupos, importarEjercicios, importarRutinas, importarSeries);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Error al leer el archivo: " + e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    /**
     * Exporta alumnos del profesor a JSON (backup completo: datos, mediciones, asistencias).
     */
    @GetMapping("/backup/exportar-alumnos-json")
    public ResponseEntity<Resource> exportarAlumnosJson(@AuthenticationPrincipal Usuario usuarioActual) {
        if (usuarioActual == null || (!"ADMIN".equals(usuarioActual.getRol()) && !"DEVELOPER".equals(usuarioActual.getRol()))) {
            return ResponseEntity.notFound().build();
        }
        Profesor profesor = getProfesorParaUsuario(usuarioActual);
        if (profesor == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            byte[] jsonBytes = alumnoJsonBackupService.exportarAlumnosAJson(profesor.getId());
            String fileName = "alumnos_backup_" + LocalDateTime.now().format(ZIP_FILENAME_DATE) + ".json";
            Resource resource = new ByteArrayResource(jsonBytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error al exportar alumnos a JSON: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Importa alumnos desde JSON (backup).
     */
    @PostMapping("/backup/importar-alumnos")
    public String importarAlumnosJson(@AuthenticationPrincipal Usuario usuarioActual,
                                      @RequestParam("archivoJson") MultipartFile archivoJson,
                                      @RequestParam(value = "pisarTodos", defaultValue = "false") boolean pisarTodos,
                                      RedirectAttributes redirectAttributes) {
        if (usuarioActual == null || (!"ADMIN".equals(usuarioActual.getRol()) && !"DEVELOPER".equals(usuarioActual.getRol()))) {
            return "redirect:/profesor/dashboard";
        }
        try {
            Profesor profesor = getProfesorParaUsuario(usuarioActual);
            Map<String, Object> result = alumnoJsonBackupService.importarDesdeJson(archivoJson, profesor, pisarTodos);
            redirectAttributes.addFlashAttribute("importAlumnosResult", result);
        } catch (IOException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Error al leer el archivo: " + e.getMessage());
            redirectAttributes.addFlashAttribute("importAlumnosResult", err);
        } catch (Exception e) {
            logger.error("Error al importar alumnos: {}", e.getMessage(), e);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Error al importar: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            redirectAttributes.addFlashAttribute("importAlumnosResult", err);
        }
        return "redirect:/profesor/administracion?seccion=backup";
    }

    /**
     * Exporta alumnos del profesor a Excel (datos, cantidad asignaciones, últimas 3 evoluciones).
     * Solo para reportes; no se usa para importar.
     */
    @GetMapping("/backup/exportar-alumnos-excel")
    public ResponseEntity<Resource> exportarAlumnosExcel(@AuthenticationPrincipal Usuario usuarioActual) {
        if (usuarioActual == null || (!"ADMIN".equals(usuarioActual.getRol()) && !"DEVELOPER".equals(usuarioActual.getRol()))) {
            return ResponseEntity.notFound().build();
        }
        Profesor profesor = getProfesorParaUsuario(usuarioActual);
        if (profesor == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            byte[] excelBytes = alumnoExportService.exportarAlumnosAExcel(profesor.getId());
            String fileName = "alumnos_" + LocalDateTime.now().format(ZIP_FILENAME_DATE) + ".xlsx";
            Resource resource = new ByteArrayResource(excelBytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error al exportar alumnos a Excel: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Exporta todos los ejercicios del sistema a ZIP (ejercicios.json + carpeta imagenes/).
     */
    @GetMapping("/backup/exportar-zip")
    public ResponseEntity<Resource> exportarEjerciciosZip(@AuthenticationPrincipal Usuario usuarioActual) {
        if (usuarioActual == null || (!"ADMIN".equals(usuarioActual.getRol()) && !"DEVELOPER".equals(usuarioActual.getRol()))) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] zipBytes = exerciseZipBackupService.exportarEjerciciosAZip();
            String fileName = "ejercicios_backup_" + LocalDateTime.now().format(ZIP_FILENAME_DATE) + ".zip";
            Resource resource = new ByteArrayResource(zipBytes);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/zip"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);
        } catch (Exception e) {
            logger.error("Error al exportar backup ZIP: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

}
