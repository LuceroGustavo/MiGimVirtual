package com.migimvirtual.controladores;

import com.migimvirtual.entidades.Rutina;
import com.migimvirtual.entidades.Usuario;
import com.migimvirtual.entidades.Serie;
import com.migimvirtual.servicios.RutinaService;
import com.migimvirtual.servicios.UsuarioService;
import com.migimvirtual.servicios.SerieService;
import com.migimvirtual.servicios.ProfesorService;
import com.migimvirtual.excepciones.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;

@Controller
@RequestMapping("/rutinas")
public class RutinaControlador {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RutinaService rutinaService;

    @Autowired
    private SerieService serieService;

    @Autowired
    private ProfesorService profesorService;

    // GET: Mostrar formulario de creación de rutina plantilla
    @GetMapping("/crear")
    public String crearRutina(Model model) {
        Usuario usuarioActual = usuarioService.getUsuarioActual();
        com.migimvirtual.entidades.Profesor profesor = getProfesorAcceso(usuarioActual);
        if (profesor == null) {
            // Si no es un profesor, no debería estar aquí. Redirigir.
            return "redirect:/login";
        }

        // Cargar las series plantilla del profesor logueado
        Long profesorId = profesor.getId();
        List<Serie> seriesDelProfesor = serieService.findByProfesorId(profesorId);

        // Filtrar en Java para obtener solo las plantillas
        List<Serie> seriesPlantilla = seriesDelProfesor.stream()
                .filter(Serie::isEsPlantilla)
                .collect(java.util.stream.Collectors.toList());

        model.addAttribute("seriesPlantilla", seriesPlantilla);
        model.addAttribute("rutina", new Rutina());
        model.addAttribute("usuario", usuarioActual);

        return "rutinas/crearRutina";
    }

    // POST: Crear rutina plantilla
    @PostMapping("/crear-plantilla")
    public String crearRutinaPlantilla(@RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam String categoria,
            @RequestParam(required = false) List<Long> selectedSeries,
            @RequestParam Map<String, String> allParams,
            Model model) {
        Usuario usuarioActual = usuarioService.getUsuarioActual();
        com.migimvirtual.entidades.Profesor profesor = getProfesorAcceso(usuarioActual);
        if (profesor == null) {
            return "redirect:/login";
        }
        Long profesorId = profesor.getId();
        try {
            Rutina rutina = rutinaService.crearRutinaPlantilla(profesorId, nombre, descripcion, categoria);

            // Si hay series seleccionadas, agregarlas a la rutina con sus repeticiones
            if (selectedSeries != null && !selectedSeries.isEmpty()) {
                for (Long serieId : selectedSeries) {
                    // Obtener las repeticiones para esta serie
                    String repeticionesKey = "repeticiones_" + serieId;
                    int repeticiones = 1; // valor por defecto
                    if (allParams.containsKey(repeticionesKey)) {
                        try {
                            repeticiones = Integer.parseInt(allParams.get(repeticionesKey));
                        } catch (NumberFormatException e) {
                            // Si no se puede parsear, usar valor por defecto
                        }
                    }

                    // Agregar la serie a la rutina con sus repeticiones
                    rutinaService.agregarSerieARutina(rutina.getId(), serieId, repeticiones);
                }
            }

            return "redirect:/profesor/dashboard?tab=rutinas&success=Rutina creada exitosamente";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear la rutina: " + e.getMessage());
            return "redirect:/rutinas/crear";
        }
    }







    // GET: Editar rutina (alumnoId=volver al detalle del alumno; returnTab=rutinas|asignaciones para volver al dashboard)
    @GetMapping("/editar/{id}")
    public String editarRutina(@PathVariable Long id,
            @RequestParam(required = false) Long alumnoId,
            @RequestParam(required = false) String returnTab,
            Model model) {
        try {
            // Obtener la rutina CON sus series cargadas para mostrar las ya asignadas
            Rutina rutina = rutinaService.obtenerRutinaPorIdConSeries(id);

            // Obtener el profesor actual
            Usuario usuarioActual = usuarioService.getUsuarioActual();
            com.migimvirtual.entidades.Profesor profesor = getProfesorAcceso(usuarioActual);
            if (profesor == null) {
                return "redirect:/login";
            }
            Long profesorId = profesor.getId();

            // Verificar que el profesor sea el dueño de la rutina
            if (!isDeveloper(usuarioActual) && !rutina.getProfesor().getId().equals(profesorId)) {
                return "redirect:/profesor/dashboard?tab=rutinas&error=No tiene permiso para editar esta rutina";
            }

            // Obtener todas las series plantilla del profesor
            List<Serie> todasLasSeriesPlantilla = serieService.obtenerSeriesPlantillaPorProfesor(profesorId);

            // IDs a excluir: si la serie en la rutina es copia usamos plantillaId; si es la plantilla en la rutina usamos su id
            Set<Long> idsSeriesEnRutina = rutina.getSeries().stream()
                    .map(s -> s.getPlantillaId() != null ? s.getPlantillaId() : s.getId())
                    .collect(Collectors.toSet());

            // Filtrar plantillas que aún no están en la rutina
            List<Serie> seriesDisponibles = todasLasSeriesPlantilla.stream()
                    .filter(plantilla -> !idsSeriesEnRutina.contains(plantilla.getId()))
                    .collect(Collectors.toList());

            model.addAttribute("rutina", rutina);
            model.addAttribute("seriesDisponibles", seriesDisponibles);
            model.addAttribute("usuario", usuarioActual);
            model.addAttribute("returnAlumnoId", alumnoId);
            model.addAttribute("returnTab", (returnTab != null && !returnTab.isBlank()) ? returnTab : "rutinas");

            return "rutinas/editarRutina";
        } catch (ResourceNotFoundException e) {
            return "redirect:/profesor/dashboard?tab=rutinas&error=Rutina no encontrada";
        }
    }

    private com.migimvirtual.entidades.Profesor getProfesorAcceso(Usuario usuarioActual) {
        if (usuarioActual == null) return null;
        if ("DEVELOPER".equals(usuarioActual.getRol())) {
            return profesorService.getProfesorByCorreo("profesor@migimvirtual.com");
        }
        return usuarioActual.getProfesor();
    }

    private boolean isDeveloper(Usuario usuarioActual) {
        return usuarioActual != null && "DEVELOPER".equals(usuarioActual.getRol());
    }

    // POST: Actualizar rutina (returnAlumnoId=volver al detalle del alumno; returnTab=rutinas|asignaciones para dashboard)
    @PostMapping("/actualizar/{id}")
    public String actualizarRutina(@PathVariable Long id,
            @RequestParam String nombre,
            @RequestParam(required = false) String descripcion,
            @RequestParam String categoria,
            @RequestParam(required = false) String notaParaAlumno,
            @RequestParam(required = false) List<Long> seriesIds,
            @RequestParam(required = false) List<Integer> repeticionesExistentes,
            @RequestParam(required = false) List<Long> nuevasSeriesIds,
            @RequestParam(required = false) List<Integer> repeticionesNuevas,
            @RequestParam(required = false) Long returnAlumnoId,
            @RequestParam(required = false) String returnTab,
            Model model) {
        Usuario usuarioActual = usuarioService.getUsuarioActual();
        com.migimvirtual.entidades.Profesor profesor = getProfesorAcceso(usuarioActual);
        if (profesor == null) {
            return "redirect:/login";
        }
        Rutina rutina;
        try {
            rutina = rutinaService.obtenerRutinaPorId(id);
        } catch (ResourceNotFoundException e) {
            return "redirect:/profesor/dashboard?tab=rutinas&error=Rutina+no+encontrada";
        }
        if (!isDeveloper(usuarioActual) && (rutina.getProfesor() == null || !rutina.getProfesor().getId().equals(profesor.getId()))) {
            return "redirect:/profesor/dashboard?tab=rutinas&error=No+tiene+permiso+para+editar+esta+rutina";
        }
        try {
            // Actualiza la información básica de la rutina
            rutinaService.actualizarInformacionBasicaRutina(id, nombre, descripcion, categoria);

            // Si es rutina asignada (no plantilla), actualizar nota para el alumno
            if (!rutina.isEsPlantilla() && rutina.getProfesor() != null && rutina.getProfesor().getId().equals(profesor.getId())) {
                rutinaService.actualizarNotaParaAlumno(id, profesor.getId(), notaParaAlumno);
            }

            // Lógica para actualizar las series de la rutina
            rutinaService.actualizarSeriesDeRutina(id, seriesIds, repeticionesExistentes, nuevasSeriesIds,
                    repeticionesNuevas);

            String tab = (returnTab != null && !returnTab.isBlank()) ? returnTab : "rutinas";
            if (returnAlumnoId != null) {
                return "redirect:/profesor/alumnos/" + returnAlumnoId + "?success=Rutina actualizada exitosamente";
            }
            return "redirect:/profesor/dashboard?tab=" + tab + "&success=Rutina actualizada exitosamente";
        } catch (Exception e) {
            String redirect = "/rutinas/editar/" + id + "?error=" + e.getMessage();
            if (returnAlumnoId != null) redirect += "&alumnoId=" + returnAlumnoId;
            if (returnTab != null && !returnTab.isBlank()) redirect += "&returnTab=" + returnTab;
            return "redirect:" + redirect;
        }
    }

    // GET: Eliminar rutina (tab opcional: rutinas | asignaciones, para redirigir a la pestaña correcta)
    @GetMapping("/eliminar/{id}")
    public String eliminarRutina(@PathVariable Long id, @RequestParam(required = false) String tab) {
        Usuario usuarioActual = usuarioService.getUsuarioActual();
        com.migimvirtual.entidades.Profesor profesor = getProfesorAcceso(usuarioActual);
        if (profesor == null) {
            return "redirect:/login";
        }
        String tabRedirect = "asignaciones".equalsIgnoreCase(tab) ? "asignaciones" : "rutinas";
        Rutina rutina;
        try {
            rutina = rutinaService.obtenerRutinaPorId(id);
        } catch (ResourceNotFoundException e) {
            return "redirect:/profesor/dashboard?tab=" + tabRedirect + "&error=Rutina+no+encontrada";
        }
        if (!isDeveloper(usuarioActual) && (rutina.getProfesor() == null || !rutina.getProfesor().getId().equals(profesor.getId()))) {
            return "redirect:/profesor/dashboard?tab=" + tabRedirect + "&error=No+tiene+permiso+para+eliminar+esta+rutina";
        }
        try {
            rutinaService.eliminarRutina(id);
            return "redirect:/profesor/dashboard?tab=" + tabRedirect + "&success=Rutina+eliminada+exitosamente";
        } catch (ResourceNotFoundException e) {
            return "redirect:/profesor/dashboard?tab=" + tabRedirect + "&error=Rutina+no+encontrada";
        }
    }

    // Cambiar estado de rutina (En proceso/Terminada)
    @PostMapping("/cambiar-estado")
    public String cambiarEstadoRutina(@RequestParam Long rutinaId, @RequestParam Long alumnoId) {
        Rutina rutina = rutinaService.obtenerRutinaPorId(rutinaId);
        if (rutina != null) {
            if ("TERMINADA".equals(rutina.getEstado())) {
                rutinaService.cambiarEstadoRutina(rutinaId, "EN_PROCESO");
            } else {
                rutinaService.cambiarEstadoRutina(rutinaId, "TERMINADA");
            }
        }
        return "redirect:/profesor/alumnos/" + alumnoId;
    }

    // HOJA DE RUTINA VISUAL (link público con token)
    @GetMapping("/hoja/{tokenPublico}")
    public String verHojaRutina(@PathVariable String tokenPublico, Model model, jakarta.servlet.http.HttpServletRequest request) {
        try {
            Rutina rutina = rutinaService.obtenerRutinaPorToken(tokenPublico);
            if (rutina.getUsuario() == null) {
                model.addAttribute("mensajeError", "Este enlace no es válido. Solo las rutinas asignadas a un alumno pueden verse aquí.");
                return "rutinas/hoja-no-encontrada";
            }
            if (rutina.getEstado() != null && "INACTIVA".equalsIgnoreCase(rutina.getEstado().trim())) {
                model.addAttribute("mensajeError", "Esta rutina ya no está activa.");
                return "rutinas/hoja-inactiva";
            }
            model.addAttribute("rutina", rutina);
            if (rutina.getFechaCreacion() != null) {
                Locale es = Locale.forLanguageTag("es");
                String dia = rutina.getFechaCreacion().format(DateTimeFormatter.ofPattern("EEEE", es));
                String mes = rutina.getFechaCreacion().format(DateTimeFormatter.ofPattern("MMM", es)).toUpperCase();
                String fechaFormateada = (dia.substring(0, 1).toUpperCase() + dia.substring(1)) + " - "
                        + rutina.getFechaCreacion().getDayOfMonth() + " " + mes + " " + rutina.getFechaCreacion().getYear();
                model.addAttribute("fechaFormateada", fechaFormateada);
            } else {
                model.addAttribute("fechaFormateada", "");
            }
            // URLs absolutas para Open Graph / WhatsApp (Thymeleaf 3.1 no expone #request por defecto)
            int port = request.getServerPort();
            String baseUrl = request.getScheme() + "://" + request.getServerName()
                    + (port != 80 && port != 443 ? ":" + port : "");
            model.addAttribute("ogImageUrl", baseUrl + "/img/logo.png");
            model.addAttribute("ogPageUrl", baseUrl + "/rutinas/hoja/" + rutina.getTokenPublico());
            model.addAttribute("esVistaEscritorio", false); // Responsive: rutina asignada (enlace alumno)
            return "rutinas/verRutina";
        } catch (ResourceNotFoundException e) {
            model.addAttribute("mensajeError", "El enlace de la rutina no es válido o ha expirado.");
            return "rutinas/hoja-no-encontrada";
        }
    }

    // Redirección para evitar pantalla verde si acceden por /rutinas/ver/{rutinaId}
    @GetMapping("/ver/{rutinaId}")
    public String redirigirVerHojaRutina(@PathVariable Long rutinaId) {
        Rutina rutina = rutinaService.obtenerRutinaPorId(rutinaId);
        return "redirect:/rutinas/hoja/" + rutina.getTokenPublico();
    }

    /** Cambiar estado de una rutina asignada (ACTIVA / INACTIVA). Si tab=alumno y alumnoId está presente, redirige al detalle del alumno. */
    @GetMapping("/cambiar-estado-asignacion/{id}")
    public String cambiarEstadoAsignacion(@PathVariable Long id, @RequestParam String estado,
                                         @RequestParam(required = false) String tab,
                                         @RequestParam(required = false) Long alumnoId) {
        Usuario usuarioActual = usuarioService.getUsuarioActual();
        com.migimvirtual.entidades.Profesor profesor = getProfesorAcceso(usuarioActual);
        if (profesor == null) return "redirect:/login";
        boolean redirectToAlumno = alumnoId != null || "alumno".equalsIgnoreCase(tab);
        String tabRedirect = "asignaciones".equalsIgnoreCase(tab) ? "asignaciones" : "rutinas";
        try {
            Rutina rutina = rutinaService.obtenerRutinaPorId(id);
            if (!isDeveloper(usuarioActual) && (rutina.getProfesor() == null || !rutina.getProfesor().getId().equals(profesor.getId()))) {
                if (redirectToAlumno && alumnoId != null) return "redirect:/profesor/alumnos/" + alumnoId + "?error=No+tiene+permiso";
                return "redirect:/profesor/dashboard?tab=" + tabRedirect + "&error=No+tiene+permiso";
            }
            String nuevoEstado = (estado != null && estado.toUpperCase().trim().equals("INACTIVA")) ? "INACTIVA" : "ACTIVA";
            rutinaService.cambiarEstadoRutina(id, nuevoEstado);
            if (redirectToAlumno && alumnoId != null) {
                return "redirect:/profesor/alumnos/" + alumnoId + "?success=Estado+actualizado";
            }
            return "redirect:/profesor/dashboard?tab=" + tabRedirect + "&success=Estado+actualizado";
        } catch (ResourceNotFoundException e) {
            if (redirectToAlumno && alumnoId != null) return "redirect:/profesor/alumnos/" + alumnoId + "?error=Rutina+no+encontrada";
            return "redirect:/profesor/dashboard?tab=" + tabRedirect + "&error=Rutina+no+encontrada";
        }
    }
}
