package com.migimvirtual.controladores;

import com.migimvirtual.entidades.Profesor;
import com.migimvirtual.entidades.Serie;
import com.migimvirtual.entidades.Usuario;
import com.migimvirtual.servicios.ProfesorService;
import com.migimvirtual.servicios.UsuarioService;
import com.migimvirtual.servicios.RutinaService;
import com.migimvirtual.servicios.SerieService;
import com.migimvirtual.entidades.MedicionFisica;
import com.migimvirtual.servicios.MedicionFisicaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;
import com.migimvirtual.entidades.Exercise;
import com.migimvirtual.entidades.GrupoMuscular;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/profesor")
public class ProfesorController {

    private static final Logger logger = LoggerFactory.getLogger(ProfesorController.class);

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ProfesorService profesorService;

    @Autowired
    private RutinaService rutinaService;

    @Autowired
    private SerieService serieService;

    @Autowired
    private MedicionFisicaService medicionFisicaService;

    @Autowired
    private com.migimvirtual.servicios.ExerciseService exerciseService;

    @Autowired
    private com.migimvirtual.servicios.GrupoMuscularService grupoMuscularService;

    @Autowired
    private com.migimvirtual.servicios.CategoriaService categoriaService;

    @Autowired
    private com.migimvirtual.servicios.RegistroProgresoService registroProgresoService;

    @Autowired
    private com.migimvirtual.servicios.ExerciseCargaDefaultOptimizado exerciseCargaDefaultOptimizado;

    /** Obtiene el Profesor asociado al usuario actual (el único rol del panel es ADMIN). */
    private Profesor getProfesorParaUsuarioActual(Usuario usuarioActual) {
        if (usuarioActual == null) return null;
        if ("DEVELOPER".equals(usuarioActual.getRol())) {
            return profesorService.getProfesorByCorreo("profesor@migymvirtual.com");
        }
        if (usuarioActual.getProfesor() != null) return usuarioActual.getProfesor();
        return profesorService.getProfesorByCorreo(usuarioActual.getCorreo());
    }

    /** Carga en el modelo todos los datos del dashboard (alumnos, series, rutinas, etc.). Siempre pone listas no null para que la vista no falle. */
    private void cargarModeloDashboard(Profesor profesor, Model model) {
        usuarioService.evictAlumnosByProfesorId(profesor.getId());
        List<Usuario> usuarios = usuarioService.getAlumnosByProfesorId(profesor.getId());
        List<Serie> series = serieService.obtenerSeriesPlantillaPorProfesor(profesor.getId());
        List<com.migimvirtual.entidades.Rutina> rutinas = rutinaService.obtenerRutinasPlantillaPorProfesorWithSeries(profesor.getId());
        List<com.migimvirtual.entidades.Rutina> rutinasAsignadas = rutinaService
                .obtenerRutinasAsignadasPorProfesor(profesor.getId());

        if (usuarios == null) usuarios = java.util.Collections.emptyList();
        if (series == null) series = java.util.Collections.emptyList();
        if (rutinas == null) rutinas = java.util.Collections.emptyList();
        if (rutinasAsignadas == null) rutinasAsignadas = java.util.Collections.emptyList();

        java.time.LocalDate hoy = java.time.LocalDate.now();
        String fechaHoyFormateada = hoy.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        long cantidadEjercicios = exerciseService.countEjerciciosDisponiblesParaProfesor(profesor.getId());

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("series", series);
        model.addAttribute("rutinas", rutinas);
        model.addAttribute("rutinasAsignadas", rutinasAsignadas);
        model.addAttribute("fechaHoyFormateada", fechaHoyFormateada);
        model.addAttribute("cantidadEjercicios", cantidadEjercicios);
        model.addAttribute("profesor", profesor);
        model.addAttribute("usuario", usuarioService.getUsuarioActual());
    }

    /**
     * Panel del profesor por URL /profesor/dashboard.
     * Renderiza el dashboard directamente con los datos del profesor en sesión (mismo modelo que /profesor/{id})
     * para que escritorio y móvil muestren siempre los mismos datos y se eviten problemas de caché o redirección.
     */
    @GetMapping("/dashboard")
    public String dashboardProfesor(Model model, jakarta.servlet.http.HttpServletRequest request) {
        Usuario usuarioActual = usuarioService.getUsuarioActual();
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        cargarModeloDashboard(profesor, model);
        return "profesor/dashboard";
    }

    @GetMapping("/manual")
    public String manualUsuario(@RequestParam(name = "fragment", required = false) String fragment) {
        if (fragment != null && !fragment.isEmpty()) {
            return "profesor/manual-usuario :: contenido";
        }
        return "profesor/manual-usuario";
    }

    /** Panel del profesor por URL /profesor/{id}. Misma vista y datos que /profesor/dashboard. */
    @GetMapping("/{id}")
    public String dashboardProfesorPorId(@PathVariable Long id, Model model) {
        Profesor profesor = profesorService.getProfesorById(id);
        if (profesor == null) {
            model.addAttribute("errorMessage", "Profesor no encontrado.");
            return "redirect:/profesor/dashboard";
        }
        cargarModeloDashboard(profesor, model);
        return "profesor/dashboard";
    }

    // --- GESTIÓN DE ALUMNOS (USUARIOS) ---

    // FORMULARIO NUEVO ALUMNO
    @GetMapping("/alumnos/nuevo")
    public String nuevoAlumnoForm(Model model, @AuthenticationPrincipal Usuario profesorUsuario) {
        Usuario nuevoUsuario = new Usuario();
        model.addAttribute("usuario", nuevoUsuario);
        model.addAttribute("usuarioActual", profesorUsuario);
        return "profesor/nuevoalumno";
    }

    // GUARDAR NUEVO ALUMNO
    @PostMapping("/alumnos/nuevo")
    public String crearAlumno(@ModelAttribute Usuario alumno,
            Model model,
            @AuthenticationPrincipal Usuario profesorUsuario) {
        model.addAttribute("usuarioActual", profesorUsuario);
        try {
            // Normalizar correo vacío a null (correo es opcional para alumnos)
            String correoNorm = (alumno.getCorreo() != null && !alumno.getCorreo().trim().isEmpty())
                ? alumno.getCorreo().trim() : null;
            alumno.setCorreo(correoNorm);

            // Validar duplicados solo si se proporcionó correo
            if (correoNorm != null) {
                Optional<Usuario> usuarioExistente = usuarioService.getAllUsuarios().stream()
                    .filter(u -> u.getCorreo() != null && u.getCorreo().equals(correoNorm))
                    .findFirst();
                if (usuarioExistente.isPresent()) {
                    throw new IllegalArgumentException("Ya existe un usuario con el correo: " + correoNorm);
                }
                Profesor profesorExistente = profesorService.getProfesorByCorreo(correoNorm);
                if (profesorExistente != null) {
                    throw new IllegalArgumentException("Ya existe un profesor con el correo: " + correoNorm);
                }
            }

            Profesor profesor = getProfesorParaUsuarioActual(profesorUsuario);
            if (profesor == null) return "redirect:/login";
            alumno.setProfesor(profesor);

            usuarioService.crearAlumno(alumno);

            return "redirect:/profesor/" + profesor.getId();

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear el alumno: " + e.getMessage());
            model.addAttribute("usuario", alumno);
            return "profesor/nuevoalumno";
        }
    }

    // ELIMINAR ALUMNO
    @GetMapping("/alumnos/eliminar/{id}")
    public String eliminarAlumno(@PathVariable Long id, @AuthenticationPrincipal Usuario profesorUsuario) {
        if (profesorUsuario == null) {
            return "redirect:/login";
        }

        Usuario alumno = usuarioService.getUsuarioById(id);
        Profesor profesor = getProfesorParaUsuarioActual(profesorUsuario);
        if (profesor == null) return "redirect:/login";

        boolean esPropietario = alumno != null && alumno.getProfesor() != null
                && alumno.getProfesor().getId().equals(profesor.getId());

        if (esPropietario) {
            usuarioService.eliminarUsuario(id);
            return "redirect:/profesor/" + profesor.getId();
        }
        return "redirect:/profesor/" + profesor.getId() + "?error=permiso";
    }

    // --- FORMULARIO EDITAR ALUMNO (GET) ---
    @GetMapping("/alumnos/editar/{id}")
    public String editarAlumnoForm(@PathVariable Long id, Model model,
            @AuthenticationPrincipal Usuario profesorUsuario) {
        if (profesorUsuario == null) return "redirect:/login";

        Profesor profesor = getProfesorParaUsuarioActual(profesorUsuario);
        if (profesor == null) return "redirect:/login";

        Usuario alumno = usuarioService.getUsuarioByIdParaFicha(id);
        boolean esPropietario = alumno != null && alumno.getProfesor() != null
                && alumno.getProfesor().getId().equals(profesor.getId());

        if (esPropietario) {
            model.addAttribute("usuario", alumno);
            model.addAttribute("usuarioActual", profesorUsuario);
            model.addAttribute("editMode", true);
            return "profesor/nuevoalumno";
        }
        return "redirect:/profesor/" + profesor.getId() + "?error=permiso";
    }

    // --- PROCESAR EDICIÓN ALUMNO (POST) ---
    @PostMapping("/alumnos/editar/{id}")
    public String procesarEditarAlumno(@PathVariable Long id,
            @ModelAttribute Usuario alumno,
            Model model,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal Usuario profesorUsuario) {
        Profesor profesor = getProfesorParaUsuarioActual(profesorUsuario);
        if (profesor == null) return "redirect:/login";

        Usuario alumnoAEditar = usuarioService.getUsuarioById(id);
        boolean esPropietario = alumnoAEditar != null && alumnoAEditar.getProfesor() != null
                && alumnoAEditar.getProfesor().getId().equals(profesor.getId());

        if (esPropietario) {
            try {
                alumno.setId(id);
                alumno.setProfesor(profesor);

                // Normalizar correo vacío a null (correo es opcional para alumnos)
                String correoNorm = (alumno.getCorreo() != null && !alumno.getCorreo().trim().isEmpty())
                    ? alumno.getCorreo().trim() : null;
                alumno.setCorreo(correoNorm);

                // Validar duplicados solo si se proporcionó correo (excluir al alumno actual)
                if (correoNorm != null) {
                    Optional<Usuario> usuarioExistente = usuarioService.getAllUsuarios().stream()
                        .filter(u -> !u.getId().equals(id) && u.getCorreo() != null && u.getCorreo().equals(correoNorm))
                        .findFirst();
                    if (usuarioExistente.isPresent()) {
                        throw new IllegalArgumentException("Ya existe un usuario con el correo: " + correoNorm);
                    }
                    Profesor profesorExistente = profesorService.getProfesorByCorreo(correoNorm);
                    if (profesorExistente != null) {
                        throw new IllegalArgumentException("Ya existe un profesor con el correo: " + correoNorm);
                    }
                }

                usuarioService.actualizarUsuario(alumno);
                redirectAttributes.addFlashAttribute("mensajeSuccess", "Datos del alumno actualizados correctamente.");
                return "redirect:/profesor/alumnos/" + id;
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Error al actualizar el alumno: " + e.getMessage());
                model.addAttribute("usuario", alumno);
                model.addAttribute("usuarioActual", profesorUsuario);
                model.addAttribute("editMode", true);
                return "profesor/nuevoalumno";
            }
        } else {
            model.addAttribute("errorMessage", "No tiene permisos para editar este usuario.");
            return "profesor/nuevoalumno";
        }
    }

    // Mostrar ficha de alumno con historial físico (carga profesor, rutinas y horarios de asistencia)
    @GetMapping("/alumnos/{id}")
    public String verAlumno(@PathVariable Long id, @RequestParam(required = false) Long editarProgreso, Model model, @AuthenticationPrincipal Usuario usuarioActual) {
        Usuario alumno = usuarioService.getUsuarioByIdParaFicha(id);
        if (alumno == null) {
            return "redirect:/profesor/dashboard";
        }
        if (alumno.getProfesor() == null) {
            return "redirect:/profesor/dashboard?error=alumno_sin_profesor";
        }
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        if (!profesor.getId().equals(alumno.getProfesor().getId())) {
            return "redirect:/profesor/dashboard?error=No+tiene+permiso";
        }
        model.addAttribute("alumno", alumno);
        model.addAttribute("usuariosSistema", usuarioService.getUsuariosSistema());
        model.addAttribute("historialEstadoFormateado", formatearFechasEnHistorialEstado(alumno.getHistorialEstado()));
        model.addAttribute("gruposMusculares", grupoMuscularService.findDisponiblesParaProfesor(alumno.getProfesor().getId()));
        java.util.List<com.migimvirtual.entidades.RegistroProgreso> registrosProgreso = registroProgresoService.obtenerRegistrosPorAlumno(id);
        com.migimvirtual.entidades.RegistroProgreso ultimoProgreso = registrosProgreso.isEmpty() ? null : registrosProgreso.get(0);
        model.addAttribute("registrosProgreso", registrosProgreso);
        model.addAttribute("ultimoProgreso", ultimoProgreso);
        model.addAttribute("ultimoProgresoFormateado", registroProgresoService.formatearUltimoRegistro(ultimoProgreso));
        com.migimvirtual.entidades.RegistroProgreso progresoAEditar = null;
        if (editarProgreso != null) {
            progresoAEditar = registrosProgreso.stream().filter(r -> r.getId().equals(editarProgreso)).findFirst().orElse(null);
        }
        model.addAttribute("progresoAEditar", progresoAEditar);
        List<com.migimvirtual.entidades.Rutina> rutinasDelAlumno = rutinaService.obtenerRutinasAsignadasPorUsuario(id);
        List<com.migimvirtual.entidades.Rutina> rutinasAsignadas = rutinasDelAlumno.stream()
                .sorted(java.util.Comparator
                        .comparing(com.migimvirtual.entidades.Rutina::getFechaCreacion,
                                java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()))
                        .reversed())
                .limit(3)
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("rutinasAsignadas", rutinasAsignadas);
        return "profesor/alumno-detalle";
    }

    /** Registra progreso del alumno (fecha, grupos musculares, observaciones). */
    @PostMapping("/alumnos/{id}/progreso")
    public String registrarProgreso(@PathVariable Long id,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) java.util.List<Long> gruposMuscularesIds,
            @RequestParam(required = false) String observaciones,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal Usuario usuarioActual) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (profesor == null) return "redirect:/login";

        Usuario alumno = usuarioService.getUsuarioById(id);
        if (alumno == null || alumno.getProfesor() == null || !alumno.getProfesor().getId().equals(profesor.getId())) {
            return "redirect:/profesor/dashboard?error=No+tiene+permiso";
        }
        String gruposStr = null;
        if (gruposMuscularesIds != null && !gruposMuscularesIds.isEmpty()) {
            gruposStr = gruposMuscularesIds.stream()
                    .map(gid -> grupoMuscularService.findById(gid).map(g -> g.getNombre()).orElse(null))
                    .filter(n -> n != null)
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        java.time.LocalDate fechaParsed = registroProgresoService.parseFecha(fecha);
        registroProgresoService.guardar(id, alumno, fechaParsed, gruposStr, observaciones);
        redirectAttributes.addFlashAttribute("mensajeSuccess", "Progreso guardado correctamente.");
        return "redirect:/profesor/alumnos/" + id + "?progreso=ok";
    }

    /** Actualiza un registro de progreso existente. */
    @PostMapping("/alumnos/{id}/progreso/editar/{registroId}")
    public String editarProgreso(@PathVariable Long id, @PathVariable Long registroId,
            @RequestParam(required = false) String fecha,
            @RequestParam(required = false) java.util.List<Long> gruposMuscularesIds,
            @RequestParam(required = false) String observaciones,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal Usuario usuarioActual) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (profesor == null) return "redirect:/login";

        Usuario alumno = usuarioService.getUsuarioById(id);
        if (alumno == null || alumno.getProfesor() == null || !alumno.getProfesor().getId().equals(profesor.getId())) {
            return "redirect:/profesor/dashboard?error=No+tiene+permiso";
        }
        String gruposStr = null;
        if (gruposMuscularesIds != null && !gruposMuscularesIds.isEmpty()) {
            gruposStr = gruposMuscularesIds.stream()
                    .map(gid -> grupoMuscularService.findById(gid).map(g -> g.getNombre()).orElse(null))
                    .filter(n -> n != null)
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        java.time.LocalDate fechaParsed = registroProgresoService.parseFecha(fecha);
        registroProgresoService.actualizar(registroId, id, alumno, fechaParsed, gruposStr, observaciones);
        redirectAttributes.addFlashAttribute("mensajeSuccess", "Progreso actualizado correctamente.");
        return "redirect:/profesor/alumnos/" + id;
    }

    /** Elimina un registro de progreso. */
    @GetMapping("/alumnos/{id}/progreso/eliminar/{registroId}")
    public String eliminarProgreso(@PathVariable Long id, @PathVariable Long registroId,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal Usuario usuarioActual) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (profesor == null) return "redirect:/login";

        Usuario alumno = usuarioService.getUsuarioById(id);
        if (alumno == null || alumno.getProfesor() == null || !alumno.getProfesor().getId().equals(profesor.getId())) {
            return "redirect:/profesor/dashboard?error=No+tiene+permiso";
        }
        registroProgresoService.eliminar(registroId, id);
        redirectAttributes.addFlashAttribute("mensajeSuccess", "Progreso eliminado.");
        return "redirect:/profesor/alumnos/" + id;
    }

    /** Actualiza solo las notas privadas del profesor para un alumno. */
    @PostMapping("/alumnos/{id}/notas")
    public String actualizarNotasProfesor(@PathVariable Long id,
            @RequestParam(required = false) String notasProfesor,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal Usuario usuarioActual) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (profesor == null) return "redirect:/login";

        Usuario alumno = usuarioService.getUsuarioById(id);
        if (alumno == null || alumno.getProfesor() == null || !alumno.getProfesor().getId().equals(profesor.getId())) {
            return "redirect:/profesor/dashboard?error=No+tiene+permiso";
        }
        usuarioService.actualizarNotasProfesor(id, notasProfesor);
        redirectAttributes.addFlashAttribute("mensajeSuccess", "Notas actualizadas correctamente.");
        return "redirect:/profesor/alumnos/" + id;
    }

    /** Inactiva todas las rutinas asignadas al alumno. Solo si el alumno pertenece al profesor. */
    @GetMapping("/alumnos/{id}/rutinas/inactivar-todas")
    public String inactivarTodasRutinasDelAlumno(@PathVariable Long id, @AuthenticationPrincipal Usuario usuarioActual) {
        Usuario alumno = usuarioService.getUsuarioById(id);
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (alumno == null || profesor == null || alumno.getProfesor() == null || !alumno.getProfesor().getId().equals(profesor.getId())) {
            return "redirect:/profesor/dashboard?error=No+tiene+permiso";
        }
        int inactivadas = rutinaService.inactivarTodasRutinasDelAlumno(id);
        return "redirect:/profesor/alumnos/" + id + "?success=" + (inactivadas > 0 ? "Rutinas+inactivadas" : "Sin+rutinas+activas");
    }

    // Agregar nueva medición física
    @PostMapping("/alumnos/{id}/medicion/nueva")
    public String agregarMedicionFisica(@PathVariable Long id, @RequestParam(required = false) String fecha,
            @RequestParam(required = false) Double peso,
            @RequestParam(required = false) Double altura,
            @RequestParam(required = false) Double cintura,
            @RequestParam(required = false) Double pecho,
            @RequestParam(required = false) Double cadera,
            @RequestParam(required = false) Double biceps,
            @RequestParam(required = false) Double muslo) {
        MedicionFisica medicion = new MedicionFisica();
        medicion.setUsuario(usuarioService.getUsuarioById(id));
        if (fecha != null && !fecha.isEmpty()) {
            medicion.setFecha(java.time.LocalDate.parse(fecha));
        }
        medicion.setPeso(peso);
        medicion.setAltura(altura);
        medicion.setCintura(cintura);
        medicion.setPecho(pecho);
        medicion.setCadera(cadera);
        medicion.setBiceps(biceps);
        medicion.setMuslo(muslo);
        medicionFisicaService.guardarMedicion(medicion);
        return "redirect:/profesor/alumnos/" + id;
    }

    // --- GESTIÓN DE EJERCICIOS DEL PROFESOR ---

    @PostMapping("/mis-ejercicios/cargar-predeterminados")
    public String cargarEjerciciosPredeterminados(@AuthenticationPrincipal Usuario usuarioActual) {
        if (usuarioActual == null || getProfesorParaUsuarioActual(usuarioActual) == null) {
            return "redirect:/login?error=true";
        }
        try {
            int cargados = exerciseCargaDefaultOptimizado.saveDefaultExercisesOptimizado();
            logger.info("Ejercicios predeterminados cargados: {} por usuario {}", cargados, usuarioActual.getCorreo());
            return "redirect:/profesor/mis-ejercicios?success=predeterminados_cargados&total=" + cargados;
        } catch (Exception e) {
            logger.error("Error cargando ejercicios predeterminados: {}", e.getMessage());
            return "redirect:/profesor/mis-ejercicios?error=carga_predeterminados";
        }
    }

    @GetMapping("/mis-ejercicios")
    public String listarEjerciciosProfesor(@AuthenticationPrincipal Usuario usuarioActual, Model model,
            @RequestParam(name = "imagenesActualizadas", required = false) Integer imagenesActualizadas) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }

        // Solo asegurar los 60 predeterminados si hay 0 (primera vez / instalación nueva).
        // Si el usuario eliminó alguno predeterminado, no re-agregarlo al listar.
        try {
            if (exerciseService.countEjerciciosPredeterminados() == 0) {
                exerciseCargaDefaultOptimizado.asegurarEjerciciosPredeterminados();
            }
        } catch (Exception e) {
            logger.warn("No se pudieron asegurar ejercicios predeterminados: {}", e.getMessage());
        }

        if (imagenesActualizadas != null) {
            model.addAttribute("imagenesActualizadas", imagenesActualizadas);
        }

        Long profesorId = profesor.getId();
        // Usar método con imágenes cargadas para evitar LazyInitializationException en la vista (ejercicio.imagen)
        List<com.migimvirtual.entidades.Exercise> ejercicios = exerciseService.findEjerciciosDisponiblesParaProfesorWithImages(profesorId);
        
        // Separar predeterminados y propios para estadísticas
        List<com.migimvirtual.entidades.Exercise> ejerciciosPropios = exerciseService.findEjerciciosPropiosDelProfesor(profesorId);
        long ejerciciosPredeterminados = exerciseService.countEjerciciosPredeterminados();
        
        // Calcular estadísticas
        long totalEjercicios = ejercicios.size();
        long totalEjerciciosPropios = ejerciciosPropios.size();
        long ejerciciosConVideo = ejercicios.stream()
            .filter(e -> e.getVideoUrl() != null && !e.getVideoUrl().isEmpty())
            .count();
        
        // Grupos musculares disponibles para el profesor (sistema + propios)
        List<com.migimvirtual.entidades.GrupoMuscular> gruposMusculares = grupoMuscularService.findDisponiblesParaProfesor(profesorId);
        long totalGruposMusculares = gruposMusculares.size();

        model.addAttribute("ejercicios", ejercicios);
        model.addAttribute("ejerciciosPropios", ejerciciosPropios);
        model.addAttribute("totalEjercicios", totalEjercicios);
        model.addAttribute("totalEjerciciosPropios", totalEjerciciosPropios);
        model.addAttribute("ejerciciosPredeterminados", ejerciciosPredeterminados);
        model.addAttribute("ejerciciosConVideo", ejerciciosConVideo);
        model.addAttribute("totalGruposMusculares", totalGruposMusculares);
        model.addAttribute("gruposMusculares", gruposMusculares);
        model.addAttribute("profesor", profesor);

        return "profesor/ejercicios-lista";
    }

    /**
     * Actualiza las imágenes de los ejercicios predeterminados desde uploads/ejercicios/ (1.webp, 2.webp, …).
     * Redirige a Mis Ejercicios con el número de imágenes actualizadas.
     * GET con confirm=1 evita usar formulario en la vista y que Thymeleaf corte la respuesta.
     */
    @GetMapping("/mis-ejercicios/actualizar-imagenes")
    public String actualizarImagenesEjerciciosGet(
            @RequestParam(name = "confirm", required = false) String confirm,
            @AuthenticationPrincipal Usuario usuarioActual) {
        if (!"1".equals(confirm)) {
            return "redirect:/profesor/mis-ejercicios";
        }
        if (usuarioActual == null || getProfesorParaUsuarioActual(usuarioActual) == null) {
            return "redirect:/login?error=true";
        }
        if (!"DEVELOPER".equals(usuarioActual.getRol())) {
            return "redirect:/profesor/mis-ejercicios";
        }
        int n = exerciseCargaDefaultOptimizado.actualizarImagenesDesdeCarpeta();
        return "redirect:/profesor/mis-ejercicios?imagenesActualizadas=" + n;
    }

    @PostMapping("/mis-ejercicios/actualizar-imagenes")
    public String actualizarImagenesEjercicios(@AuthenticationPrincipal Usuario usuarioActual) {
        if (usuarioActual == null || getProfesorParaUsuarioActual(usuarioActual) == null) {
            return "redirect:/login?error=true";
        }
        if (!"DEVELOPER".equals(usuarioActual.getRol())) {
            return "redirect:/profesor/mis-ejercicios";
        }
        int n = exerciseCargaDefaultOptimizado.actualizarImagenesDesdeCarpeta();
        return "redirect:/profesor/mis-ejercicios?imagenesActualizadas=" + n;
    }

    @GetMapping("/mis-ejercicios/nuevo")
    public String nuevoEjercicioForm(@AuthenticationPrincipal Usuario usuarioActual, Model model) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }

        model.addAttribute("exercise", new com.migimvirtual.entidades.Exercise());
        model.addAttribute("gruposMusculares", grupoMuscularService.findDisponiblesParaProfesor(profesor.getId()));
        model.addAttribute("profesor", profesor);
        model.addAttribute("usuario", usuarioActual);
        return "ejercicios/formulario-ejercicio";
    }

    @PostMapping("/mis-ejercicios/nuevo")
    public String guardarEjercicioProfesor(@Valid @ModelAttribute("exercise") com.migimvirtual.entidades.Exercise exercise,
                                         BindingResult bindingResult,
                                         @RequestParam(name = "grupoIds", required = false) List<Long> grupoIds,
                                         @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                         @AuthenticationPrincipal Usuario usuarioActual,
                                         Model model) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("gruposMusculares", grupoMuscularService.findDisponiblesParaProfesor(profesor.getId()));
            model.addAttribute("profesor", profesor);
            model.addAttribute("usuario", usuarioActual);
            return "ejercicios/formulario-ejercicio";
        }
        try {
            exercise.setProfesor(profesor);
            exercise.setEsPredeterminado(false);
            exercise.setGrupos(new HashSet<>(grupoMuscularService.resolveGruposByIds(grupoIds != null ? grupoIds : List.of())));
            exerciseService.saveExercise(exercise, imageFile, usuarioActual);
            return "redirect:/profesor/mis-ejercicios?success=ejercicio_creado";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al crear el ejercicio: " + e.getMessage());
            model.addAttribute("gruposMusculares", grupoMuscularService.findDisponiblesParaProfesor(profesor.getId()));
            model.addAttribute("profesor", profesor);
            model.addAttribute("usuario", usuarioActual);
            return "ejercicios/formulario-ejercicio";
        }
    }

    @GetMapping("/mis-ejercicios/editar/{id}")
    public String editarEjercicioForm(@PathVariable Long id, 
                                    @AuthenticationPrincipal Usuario usuarioActual, 
                                    Model model) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }

        com.migimvirtual.entidades.Exercise ejercicio = exerciseService.findByIdWithImageAndGrupos(id);
        if (ejercicio == null) {
            return "redirect:/profesor/mis-ejercicios?error=ejercicio_no_encontrado";
        }
        
        // Validar permisos de edición usando el nuevo método
        if (!ejercicio.puedeSerEditadoPor(usuarioActual)) {
            return "redirect:/profesor/mis-ejercicios?error=sin_permisos_editar";
        }

        // IDs de grupos del ejercicio (evita acceder a exercise.grupos en la vista y posibles LazyInitializationException)
        Set<Long> ejercicioGrupoIds = new HashSet<>();
        if (ejercicio.getGrupos() != null) {
            for (GrupoMuscular g : ejercicio.getGrupos()) {
                if (g != null && g.getId() != null) ejercicioGrupoIds.add(g.getId());
            }
        }
        String urlImagenActual = "";
        if (ejercicio.getImagen() != null) {
            String u = ejercicio.getImagen().getUrl();
            urlImagenActual = (u != null) ? u : "";
        }
        model.addAttribute("exercise", ejercicio);
        model.addAttribute("ejercicioGrupoIds", ejercicioGrupoIds);
        model.addAttribute("urlImagenActual", urlImagenActual);
        model.addAttribute("returnUrlEditar", "/profesor/mis-ejercicios/editar/" + id);
        model.addAttribute("gruposMusculares", grupoMuscularService.findDisponiblesParaProfesor(profesor.getId()));
        model.addAttribute("profesor", profesor);
        model.addAttribute("usuario", usuarioActual);
        return "ejercicios/formulario-modificar-ejercicio";
    }

    @PostMapping("/mis-ejercicios/editar/{id}")
    public String actualizarEjercicioProfesor(@PathVariable Long id,
                                            @Valid @ModelAttribute("exercise") com.migimvirtual.entidades.Exercise exercise,
                                            BindingResult bindingResult,
                                            @RequestParam(name = "grupoIds", required = false) List<Long> grupoIds,
                                            @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                            @AuthenticationPrincipal Usuario usuarioActual,
                                            Model model) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        com.migimvirtual.entidades.Exercise ejercicioExistente = exerciseService.findById(id);
        if (ejercicioExistente == null) {
            return "redirect:/profesor/ejercicios?error=ejercicio_no_encontrado";
        }
        if (!ejercicioExistente.puedeSerEditadoPor(usuarioActual)) {
            return "redirect:/profesor/ejercicios?error=sin_permisos_editar";
        }
        if (bindingResult.hasErrors()) {
            exercise.setId(id);
            com.migimvirtual.entidades.Exercise ejConImagen = exerciseService.findByIdWithImageAndGrupos(id);
            model.addAttribute("ejercicioGrupoIds", grupoIds != null ? new java.util.HashSet<>(grupoIds) : new java.util.HashSet<Long>());
            String u = (ejConImagen != null && ejConImagen.getImagen() != null) ? ejConImagen.getImagen().getUrl() : null;
            model.addAttribute("urlImagenActual", (u != null) ? u : "");
            model.addAttribute("returnUrlEditar", "/profesor/mis-ejercicios/editar/" + id);
            model.addAttribute("gruposMusculares", grupoMuscularService.findDisponiblesParaProfesor(profesor.getId()));
            model.addAttribute("profesor", profesor);
            model.addAttribute("usuario", usuarioActual);
            return "ejercicios/formulario-modificar-ejercicio";
        }
        try {
            exercise.setProfesor(ejercicioExistente.getProfesor());
            exercise.setEsPredeterminado(ejercicioExistente.getEsPredeterminado());
            exercise.setImagen(ejercicioExistente.getImagen());
            java.util.Set<com.migimvirtual.entidades.GrupoMuscular> grupos = grupoMuscularService.resolveGruposByIds(grupoIds != null ? grupoIds : List.of());
            exercise.setGrupos(grupos);
            exerciseService.modifyExercise(id, exercise, imageFile, grupos, usuarioActual);
            return "redirect:/profesor/mis-ejercicios?success=ejercicio_actualizado";
        } catch (Exception e) {
            exercise.setId(id);
            com.migimvirtual.entidades.Exercise ej = exerciseService.findByIdWithImageAndGrupos(id);
            model.addAttribute("errorMessage", "Error al actualizar el ejercicio: " + e.getMessage());
            model.addAttribute("ejercicioGrupoIds", grupoIds != null ? new java.util.HashSet<>(grupoIds) : new java.util.HashSet<Long>());
            String u = (ej != null && ej.getImagen() != null) ? ej.getImagen().getUrl() : null;
            model.addAttribute("urlImagenActual", (u != null) ? u : "");
            model.addAttribute("returnUrlEditar", "/profesor/mis-ejercicios/editar/" + id);
            model.addAttribute("gruposMusculares", grupoMuscularService.findDisponiblesParaProfesor(profesor.getId()));
            model.addAttribute("profesor", profesor);
            model.addAttribute("usuario", usuarioActual);
            return "ejercicios/formulario-modificar-ejercicio";
        }
    }

    @GetMapping("/mis-ejercicios/eliminar/{id}")
    public String eliminarEjercicioProfesor(@PathVariable Long id,
                                           @AuthenticationPrincipal Usuario usuarioActual) {
        try {
            Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
            if (usuarioActual == null || profesor == null) {
                return "redirect:/profesor/mis-ejercicios?error=no_autorizado";
            }

            com.migimvirtual.entidades.Exercise ejercicio = exerciseService.findById(id);
            if (ejercicio == null) {
                return "redirect:/profesor/mis-ejercicios?error=ejercicio_no_encontrado";
            }
            // Permitir eliminar cualquier ejercicio (predeterminados y propios) al profesor
            if (ejercicio.getProfesor() != null && !ejercicio.getProfesor().getId().equals(profesor.getId())) {
                return "redirect:/profesor/mis-ejercicios?error=sin_permisos";
            }

            exerciseService.deleteExercise(id);
            return "redirect:/profesor/mis-ejercicios?success=ejercicio_eliminado";
        } catch (Exception e) {
            logger.error("Error al eliminar ejercicio {}: {}", id, e.getMessage(), e);
            return "redirect:/profesor/mis-ejercicios?error=error_eliminar";
        }
    }

    // ========== MIS GRUPOS MUSCULARES (ABM) ==========

    @GetMapping("/mis-grupos-musculares")
    public String listarGruposMusculares(@AuthenticationPrincipal Usuario usuarioActual,
                                         @RequestParam(required = false) String returnUrl,
                                         Model model) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        List<com.migimvirtual.entidades.GrupoMuscular> gruposSistema = grupoMuscularService.findGruposSistema();
        List<com.migimvirtual.entidades.GrupoMuscular> misGrupos = grupoMuscularService.findByProfesorId(profesor.getId());
        model.addAttribute("gruposSistema", gruposSistema);
        model.addAttribute("misGrupos", misGrupos);
        model.addAttribute("profesor", profesor);
        if (!model.containsAttribute("grupo")) {
            model.addAttribute("grupo", new com.migimvirtual.entidades.GrupoMuscular());
        }
        if (returnUrl != null && !returnUrl.isBlank() && returnUrl.startsWith("/profesor/")) {
            model.addAttribute("returnUrl", returnUrl);
        }
        model.addAttribute("usuario", usuarioActual);
        return "profesor/grupos-musculares-lista";
    }

    @GetMapping("/mis-grupos-musculares/nuevo")
    public String nuevoGrupoMuscularForm(@AuthenticationPrincipal Usuario usuarioActual,
                                         @RequestParam(required = false) String returnUrl) {
        StringBuilder url = new StringBuilder("redirect:/profesor/mis-grupos-musculares");
        if (returnUrl != null && !returnUrl.isBlank() && returnUrl.startsWith("/profesor/")) {
            url.append("?returnUrl=").append(URLEncoder.encode(returnUrl, StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    @PostMapping("/mis-grupos-musculares/nuevo")
    public String crearGrupoMuscular(@Valid @ModelAttribute("grupo") com.migimvirtual.entidades.GrupoMuscular grupo,
                                    BindingResult bindingResult,
                                    @AuthenticationPrincipal Usuario usuarioActual,
                                    @RequestParam(required = false) String returnUrl,
                                    RedirectAttributes redirectAttributes) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        String redirectList = "redirect:/profesor/mis-grupos-musculares";
        if (returnUrl != null && !returnUrl.isBlank() && returnUrl.startsWith("/profesor/")) {
            redirectList += "?returnUrl=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("grupo", grupo);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.grupo", bindingResult);
            return redirectList;
        }
        String nombre = grupo.getNombre() != null ? grupo.getNombre().trim().toUpperCase() : "";
        if (grupoMuscularService.findByNombreSistema(nombre).isPresent()) {
            redirectAttributes.addFlashAttribute("grupo", grupo);
            redirectAttributes.addFlashAttribute("errorMessage", "Ya existe un grupo del sistema con ese nombre. Elige otro.");
            return redirectList;
        }
        if (grupoMuscularService.existeNombreParaProfesor(nombre, profesor.getId())) {
            redirectAttributes.addFlashAttribute("grupo", grupo);
            redirectAttributes.addFlashAttribute("errorMessage", "Ya tienes un grupo muscular con ese nombre.");
            return redirectList;
        }
        grupo.setNombre(nombre);
        grupo.setProfesor(profesor);
        grupoMuscularService.guardar(grupo);
        if (returnUrl != null && !returnUrl.isBlank() && returnUrl.startsWith("/profesor/")) {
            return "redirect:" + returnUrl + (returnUrl.contains("?") ? "&" : "?") + "success=grupo_creado";
        }
        return "redirect:/profesor/mis-grupos-musculares?success=grupo_creado";
    }

    @GetMapping("/mis-grupos-musculares/editar/{id}")
    public String editarGrupoMuscularForm(@PathVariable Long id,
                                          @AuthenticationPrincipal Usuario usuarioActual,
                                          Model model) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        if (!grupoMuscularService.puedeSerEditadoPorProfesor(id, profesor.getId())) {
            return "redirect:/profesor/mis-grupos-musculares?error=sin_permisos";
        }
        com.migimvirtual.entidades.GrupoMuscular grupo = grupoMuscularService.findById(id)
                .orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
        model.addAttribute("grupo", grupo);
        model.addAttribute("profesor", profesor);
        model.addAttribute("esEdicion", true);
        model.addAttribute("usuario", usuarioActual);
        return "profesor/grupo-muscular-form";
    }

    @PostMapping("/mis-grupos-musculares/editar/{id}")
    public String actualizarGrupoMuscular(@PathVariable Long id,
                                         @Valid @ModelAttribute("grupo") com.migimvirtual.entidades.GrupoMuscular grupo,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal Usuario usuarioActual,
                                         Model model) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        if (!grupoMuscularService.puedeSerEditadoPorProfesor(id, profesor.getId())) {
            return "redirect:/profesor/mis-grupos-musculares?error=sin_permisos";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("profesor", profesor);
            model.addAttribute("esEdicion", true);
            model.addAttribute("usuario", usuarioActual);
            return "profesor/grupo-muscular-form";
        }
        com.migimvirtual.entidades.GrupoMuscular existente = grupoMuscularService.findById(id).orElseThrow();
        String nombre = grupo.getNombre() != null ? grupo.getNombre().trim().toUpperCase() : "";
        if (grupoMuscularService.findByNombreSistema(nombre).isPresent() && !nombre.equals(existente.getNombre())) {
            model.addAttribute("errorMessage", "Ya existe un grupo del sistema con ese nombre. Elige otro.");
            model.addAttribute("profesor", profesor);
            model.addAttribute("esEdicion", true);
            model.addAttribute("usuario", usuarioActual);
            return "profesor/grupo-muscular-form";
        }
        if (!nombre.equals(existente.getNombre()) && grupoMuscularService.existeNombreParaProfesor(nombre, profesor.getId())) {
            model.addAttribute("errorMessage", "Ya tienes un grupo muscular con ese nombre.");
            model.addAttribute("profesor", profesor);
            model.addAttribute("esEdicion", true);
            model.addAttribute("usuario", usuarioActual);
            return "profesor/grupo-muscular-form";
        }
        existente.setNombre(nombre);
        grupoMuscularService.guardar(existente);
        return "redirect:/profesor/mis-grupos-musculares?success=grupo_actualizado";
    }

    @GetMapping("/mis-grupos-musculares/eliminar/{id}")
    public String eliminarGrupoMuscular(@PathVariable Long id,
                                       @AuthenticationPrincipal Usuario usuarioActual) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        if (!grupoMuscularService.puedeSerEditadoPorProfesor(id, profesor.getId())) {
            return "redirect:/profesor/mis-grupos-musculares?error=sin_permisos";
        }
        if (!exerciseService.findExercisesByGrupoId(id).isEmpty()) {
            return "redirect:/profesor/mis-grupos-musculares?error=grupo_en_uso";
        }
        grupoMuscularService.eliminar(id);
        return "redirect:/profesor/mis-grupos-musculares?success=grupo_eliminado";
    }

    // ========== MIS CATEGORÍAS (ABM) ==========

    @GetMapping("/mis-categorias")
    public String listarCategorias(@AuthenticationPrincipal Usuario usuarioActual,
                                  @RequestParam(required = false) String returnUrl,
                                  Model model) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        List<com.migimvirtual.entidades.Categoria> categoriasSistema = categoriaService.findCategoriasSistema();
        List<com.migimvirtual.entidades.Categoria> misCategorias = categoriaService.findByProfesorId(profesor.getId());
        model.addAttribute("categoriasSistema", categoriasSistema);
        model.addAttribute("misCategorias", misCategorias);
        model.addAttribute("profesor", profesor);
        if (!model.containsAttribute("categoria")) {
            model.addAttribute("categoria", new com.migimvirtual.entidades.Categoria());
        }
        if (returnUrl != null && !returnUrl.isBlank() && returnUrl.startsWith("/")) {
            model.addAttribute("returnUrl", returnUrl);
        }
        model.addAttribute("usuario", usuarioActual);
        return "profesor/categorias-lista";
    }

    @GetMapping("/mis-categorias/nuevo")
    public String nuevoCategoriaForm(@AuthenticationPrincipal Usuario usuarioActual,
                                    @RequestParam(required = false) String returnUrl) {
        StringBuilder url = new StringBuilder("redirect:/profesor/mis-categorias");
        if (returnUrl != null && !returnUrl.isBlank() && returnUrl.startsWith("/")) {
            url.append("?returnUrl=").append(URLEncoder.encode(returnUrl, StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    @PostMapping("/mis-categorias/nuevo")
    public String crearCategoria(@Valid @ModelAttribute("categoria") com.migimvirtual.entidades.Categoria categoria,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal Usuario usuarioActual,
                                @RequestParam(required = false) String returnUrl,
                                RedirectAttributes redirectAttributes) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        String redirectList = "redirect:/profesor/mis-categorias";
        if (returnUrl != null && !returnUrl.isBlank() && returnUrl.startsWith("/")) {
            redirectList += "?returnUrl=" + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("categoria", categoria);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categoria", bindingResult);
            return redirectList;
        }
        String nombre = categoria.getNombre() != null ? categoria.getNombre().trim().toUpperCase() : "";
        if (categoriaService.findByNombreSistema(nombre).isPresent()) {
            redirectAttributes.addFlashAttribute("categoria", categoria);
            redirectAttributes.addFlashAttribute("errorMessage", "Ya existe una categoría del sistema con ese nombre. Elige otro.");
            return redirectList;
        }
        if (categoriaService.existeNombreParaProfesor(nombre, profesor.getId())) {
            redirectAttributes.addFlashAttribute("categoria", categoria);
            redirectAttributes.addFlashAttribute("errorMessage", "Ya tienes una categoría con ese nombre.");
            return redirectList;
        }
        categoria.setNombre(nombre);
        categoria.setProfesor(profesor);
        categoriaService.guardar(categoria);
        if (returnUrl != null && !returnUrl.isBlank() && returnUrl.startsWith("/")) {
            return "redirect:" + returnUrl + (returnUrl.contains("?") ? "&" : "?") + "success=categoria_creada";
        }
        return "redirect:/profesor/mis-categorias?success=categoria_creada";
    }

    @GetMapping("/mis-categorias/editar/{id}")
    public String editarCategoriaForm(@PathVariable Long id,
                                     @AuthenticationPrincipal Usuario usuarioActual,
                                     Model model) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        if (!categoriaService.puedeSerEditadoPorProfesor(id, profesor.getId())) {
            return "redirect:/profesor/mis-categorias?error=sin_permisos";
        }
        com.migimvirtual.entidades.Categoria categoria = categoriaService.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        model.addAttribute("categoria", categoria);
        model.addAttribute("profesor", profesor);
        model.addAttribute("esEdicion", true);
        model.addAttribute("usuario", usuarioActual);
        return "profesor/categoria-form";
    }

    @PostMapping("/mis-categorias/editar/{id}")
    public String actualizarCategoria(@PathVariable Long id,
                                     @Valid @ModelAttribute("categoria") com.migimvirtual.entidades.Categoria categoria,
                                     BindingResult bindingResult,
                                     @AuthenticationPrincipal Usuario usuarioActual,
                                     Model model) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        if (!categoriaService.puedeSerEditadoPorProfesor(id, profesor.getId())) {
            return "redirect:/profesor/mis-categorias?error=sin_permisos";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("profesor", profesor);
            model.addAttribute("esEdicion", true);
            model.addAttribute("usuario", usuarioActual);
            return "profesor/categoria-form";
        }
        com.migimvirtual.entidades.Categoria existente = categoriaService.findById(id).orElseThrow();
        String nombre = categoria.getNombre() != null ? categoria.getNombre().trim().toUpperCase() : "";
        if (categoriaService.findByNombreSistema(nombre).isPresent() && !nombre.equals(existente.getNombre())) {
            model.addAttribute("errorMessage", "Ya existe una categoría del sistema con ese nombre. Elige otro.");
            model.addAttribute("profesor", profesor);
            model.addAttribute("esEdicion", true);
            model.addAttribute("usuario", usuarioActual);
            return "profesor/categoria-form";
        }
        if (!nombre.equals(existente.getNombre()) && categoriaService.existeNombreParaProfesor(nombre, profesor.getId())) {
            model.addAttribute("errorMessage", "Ya tienes una categoría con ese nombre.");
            model.addAttribute("profesor", profesor);
            model.addAttribute("esEdicion", true);
            model.addAttribute("usuario", usuarioActual);
            return "profesor/categoria-form";
        }
        existente.setNombre(nombre);
        categoriaService.guardar(existente);
        return "redirect:/profesor/mis-categorias?success=categoria_actualizada";
    }

    @GetMapping("/mis-categorias/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Long id,
                                   @AuthenticationPrincipal Usuario usuarioActual) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }
        if (!categoriaService.puedeSerEditadoPorProfesor(id, profesor.getId())) {
            return "redirect:/profesor/mis-categorias?error=sin_permisos";
        }
        if (categoriaService.hayRutinasEnUso(id)) {
            return "redirect:/profesor/mis-categorias?error=categoria_en_uso";
        }
        categoriaService.eliminar(id);
        return "redirect:/profesor/mis-categorias?success=categoria_eliminada";
    }

    @GetMapping("/mis-ejercicios/debug")
    public String debugEjercicios(@AuthenticationPrincipal Usuario usuarioActual, Model model) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null) {
            return "redirect:/login?error=true";
        }

        Long profesorId = profesor.getId();
        logger.debug("DEBUG mis-ejercicios: Profesor ID: {}", profesorId);

        List<Exercise> ejercicios = exerciseService.findExercisesByProfesorId(profesorId);
        logger.debug("DEBUG mis-ejercicios: ejercicios encontrados: {}", ejercicios.size());

        for (int i = 0; i < ejercicios.size(); i++) {
            Exercise e = ejercicios.get(i);
            logger.debug("DEBUG mis-ejercicios: [{}] ID={} Nombre={} Profesor={}",
                    i, e.getId(), e.getName(),
                    e.getProfesor() != null ? e.getProfesor().getId() : "NULL");
        }

        logger.debug("DEBUG mis-ejercicios: agregando {} ejercicios al modelo", ejercicios.size());
        
        model.addAttribute("ejercicios", ejercicios);
        model.addAttribute("totalEjercicios", ejercicios.size());
        model.addAttribute("profesor", profesor);
        model.addAttribute("debugInfo", "Endpoint de debug - Ejercicios: " + ejercicios.size());

        return "profesor/ejercicios-lista";
    }

    /**
     * Asignar rutina a alumno. Vista refactorizada Mar 2026: tabla con búsqueda, modal detalle, responsive.
     * Modelo: alumno, profesor, rutinasPlantilla, rutinasAsignadas, nombresRutinasAsignadasAlAlumno.
     * Ver CHANGELOG [2026-03-18], GUIA_RESPONSIVE §5.4.
     */
    @GetMapping("/asignar-rutina/{id}")
    public String asignarRutinaAAlumno(@PathVariable Long id, Model model, @AuthenticationPrincipal Usuario usuarioActual) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null || profesor.getId() == null) {
            return "redirect:/login?error=true";
        }

        try {
            // Obtener el alumno
            Usuario alumno = usuarioService.getUsuarioById(id);
            if (alumno == null) {
                model.addAttribute("errorMessage", "Alumno no encontrado.");
                return "redirect:/profesor/" + profesor.getId();
            }

            // Verificar que el alumno pertenezca al profesor
            if (!alumno.getProfesor().getId().equals(profesor.getId())) {
                model.addAttribute("errorMessage", "No tienes permisos para asignar rutinas a este alumno.");
                return "redirect:/profesor/" + profesor.getId();
            }

            // Obtener las rutinas plantilla del profesor
            List<com.migimvirtual.entidades.Rutina> rutinasPlantilla = rutinaService.obtenerRutinasPlantillaPorProfesor(profesor.getId());
            
            // Obtener las rutinas ya asignadas al alumno
            List<com.migimvirtual.entidades.Rutina> rutinasAsignadas = rutinaService.obtenerRutinasAsignadasPorUsuario(id);
            // Nombres de rutinas ya asignadas (las copias tienen el mismo nombre que la plantilla)
            Set<String> nombresRutinasAsignadasAlAlumno = rutinasAsignadas.stream()
                    .map(com.migimvirtual.entidades.Rutina::getNombre)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            model.addAttribute("alumno", alumno);
            model.addAttribute("profesor", profesor);
            model.addAttribute("rutinasPlantilla", rutinasPlantilla);
            model.addAttribute("rutinasAsignadas", rutinasAsignadas);
            model.addAttribute("nombresRutinasAsignadasAlAlumno", nombresRutinasAsignadasAlAlumno);
            
            return "profesor/asignar-rutina";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al cargar la página de asignación: " + e.getMessage());
            return "redirect:/profesor/" + profesor.getId();
        }
    }

    // POST: ASIGNAR RUTINA A ALUMNO
    @PostMapping("/asignar-rutina/{id}")
    public String asignarRutinaAAlumnoPost(@PathVariable Long id,
                                          @RequestParam Long rutinaPlantillaId,
                                          @RequestParam(required = false) String notaParaAlumno,
                                          @AuthenticationPrincipal Usuario usuarioActual,
                                          Model model) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (usuarioActual == null || profesor == null || profesor.getId() == null) {
            return "redirect:/login?error=true";
        }

        try {
            Usuario alumno = usuarioService.getUsuarioById(id);
            if (alumno == null || !alumno.getProfesor().getId().equals(profesor.getId())) {
                model.addAttribute("errorMessage", "No tienes permisos para asignar rutinas a este alumno.");
                return "redirect:/profesor/" + profesor.getId();
            }

            rutinaService.asignarRutinaPlantillaAUsuario(rutinaPlantillaId, id, profesor.getId(), notaParaAlumno);
            return "redirect:/profesor/alumnos/" + id + "?success=rutina_asignada";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error al asignar la rutina: " + e.getMessage());
            return "redirect:/profesor/asignar-rutina/" + id;
        }
    }

    // POST: Actualizar nota/reseña para el alumno (rutina asignada)
    @PostMapping("/rutinas/{rutinaId}/nota")
    public String actualizarNotaRutina(@PathVariable Long rutinaId, @RequestParam(required = false) String nota,
                                      @AuthenticationPrincipal Usuario usuarioActual) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (profesor == null) return "redirect:/login";
        try {
            rutinaService.actualizarNotaParaAlumno(rutinaId, profesor.getId(), nota);
            com.migimvirtual.entidades.Rutina r = rutinaService.obtenerRutinaPorId(rutinaId);
            Long alumnoId = r.getUsuario() != null ? r.getUsuario().getId() : null;
            if (alumnoId != null) {
                return "redirect:/profesor/alumnos/" + alumnoId + "?nota_actualizada=ok";
            }
        } catch (Exception e) {
            // redirigir al panel si falla
        }
        return "redirect:/profesor/dashboard";
    }

    /** Vista privada de una rutina (requiere sesión). Usado desde Mis Rutinas; no expone el enlace público. */
    @GetMapping("/rutinas/ver/{id}")
    public String verRutinaPrivada(@PathVariable Long id, Model model, @AuthenticationPrincipal Usuario usuarioActual,
                                  jakarta.servlet.http.HttpServletRequest request) {
        Profesor profesor = getProfesorParaUsuarioActual(usuarioActual);
        if (profesor == null) return "redirect:/login";
        try {
            com.migimvirtual.entidades.Rutina rutina = rutinaService.obtenerRutinaPorIdParaVista(id);
            if (rutina.getProfesor() == null || !rutina.getProfesor().getId().equals(profesor.getId())) {
                return "redirect:/profesor/dashboard?tab=rutinas&error=No+tiene+permiso+para+ver+esta+rutina";
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
            int port = request.getServerPort();
            String baseUrl = request.getScheme() + "://" + request.getServerName()
                    + (port != 80 && port != 443 ? ":" + port : "");
            model.addAttribute("ogImageUrl", baseUrl + "/img/mgvirtual_logo1.png");
            model.addAttribute("ogPageUrl", baseUrl + "/profesor/rutinas/ver/" + id);
            model.addAttribute("esVistaEscritorio", false); // Responsive: misma vista que enlace compartido (1 col móvil)
            return "rutinas/verRutina";
        } catch (com.migimvirtual.excepciones.ResourceNotFoundException e) {
            return "redirect:/profesor/dashboard?tab=rutinas&error=Rutina+no+encontrada";
        }
    }

    /**
     * Convierte fechas en formato yyyy-mm-dd del historial de estado a dd/MM/yyyy.
     */
    private static String formatearFechasEnHistorialEstado(String historialEstado) {
        if (historialEstado == null || historialEstado.isEmpty()) {
            return historialEstado;
        }
        Pattern p = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Matcher m = p.matcher(historialEstado);
        StringBuffer sb = new StringBuffer();
        DateTimeFormatter out = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        while (m.find()) {
            try {
                LocalDate d = LocalDate.parse(m.group());
                m.appendReplacement(sb, Matcher.quoteReplacement(d.format(out)));
            } catch (DateTimeParseException e) {
                // dejar la fecha original
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
