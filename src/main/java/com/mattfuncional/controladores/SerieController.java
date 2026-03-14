package com.mattfuncional.controladores;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mattfuncional.dto.SerieDTO;
import com.mattfuncional.entidades.Exercise;
import com.mattfuncional.entidades.Usuario;
import com.mattfuncional.entidades.Serie;
import com.mattfuncional.entidades.GrupoMuscular;
import com.mattfuncional.servicios.ExerciseService;
import com.mattfuncional.servicios.GrupoMuscularService;
import com.mattfuncional.servicios.SerieService;
import com.mattfuncional.servicios.UsuarioService;
import com.mattfuncional.servicios.ProfesorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/series")
public class SerieController {

    @Autowired
    private SerieService serieService;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private GrupoMuscularService grupoMuscularService;

    @Autowired
    private ProfesorService profesorService;

    // GET: Mostrar el formulario para crear una nueva serie plantilla
    @GetMapping("/crear")
    public String mostrarFormularioCrearSerie(
            Model model,
            @AuthenticationPrincipal com.mattfuncional.entidades.Usuario usuarioActual,
            @RequestParam(name = "grupoId", required = false) Long grupoId,
            @RequestParam(name = "search", required = false) String search) {
        com.mattfuncional.entidades.Profesor profesor = getProfesorAcceso(usuarioActual);
        if (profesor == null) {
            return "redirect:/login";
        }
        Long profesorId = profesor.getId();
        List<Exercise> ejercicios = exerciseService.findEjerciciosDisponiblesParaProfesorWithImages(profesorId);
        if (grupoId != null) {
            ejercicios = ejercicios.stream()
                    .filter(e -> e.getGrupos() != null && e.getGrupos().stream().anyMatch(g -> grupoId.equals(g.getId())))
                    .toList();
        }
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            ejercicios = ejercicios.stream()
                    .filter(e -> e.getName().toLowerCase().contains(searchLower) ||
                            (e.getDescription() != null && e.getDescription().toLowerCase().contains(searchLower)))
                    .toList();
        }
        List<GrupoMuscular> gruposMusculares = grupoMuscularService.findDisponiblesParaProfesor(profesorId);
        model.addAttribute("ejercicios", ejercicios);
        model.addAttribute("serieDTO", new com.mattfuncional.dto.SerieDTO());
        model.addAttribute("usuario", usuarioActual);
        model.addAttribute("gruposMusculares", gruposMusculares);
        model.addAttribute("selectedGrupoId", grupoId);
        model.addAttribute("editMode", false);
        model.addAttribute("serieDTOJson", "null");
        return "series/crearSerie";
    }

    // POST: Recibe los datos del formulario y crea la serie plantilla
    @PostMapping("/crear-plantilla")
    @ResponseBody
    public ResponseEntity<?> crearSeriePlantilla(@RequestBody SerieDTO serieDTO,
            @AuthenticationPrincipal Usuario profesorUsuario) {
        try {
            // Obtenemos el usuario logueado (que es el profesor)
            com.mattfuncional.entidades.Profesor profesor = getProfesorAcceso(profesorUsuario);
            if (profesor == null) {
                return ResponseEntity.badRequest().body("Error: No se pudo identificar al profesor.");
            }

            // Asignamos el ID del profesor al DTO
            serieDTO.setProfesorId(profesor.getId());

            // Llamamos al servicio para crear la serie
            serieService.crearSeriePlantilla(serieDTO);

            // Devolvemos una respuesta exitosa
            return ResponseEntity.ok().body("Serie creada exitosamente");
        } catch (Exception e) {
            // En caso de error, devolvemos un mensaje
            return ResponseEntity.badRequest().body("Error al crear la serie: " + e.getMessage());
        }
    }

    // GET: Mostrar el formulario para editar una serie plantilla
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditarSerie(@PathVariable Long id, Model model,
            @AuthenticationPrincipal Usuario profesorUsuario) {
        // 1. Obtener la serie CON sus ejercicios cargados (evita LazyInitialization y muestra la tabla)
        Serie serie = serieService.obtenerSeriePorIdConEjercicios(id);

        if (!isDeveloper(profesorUsuario)
                && (profesorUsuario.getProfesor() == null
                || !serie.getProfesor().getId().equals(profesorUsuario.getProfesor().getId()))) {
            return "redirect:/profesor/dashboard?tab=series&error=permiso_serie";
        }

        // 2. Convertir la entidad a DTO para el formulario (ya con ejercicios cargados)
        SerieDTO serieDTO = serieService.convertirSerieADTO(serie);

        // 3. Preparar el modelo para la vista
        Long profesorId = profesorUsuario.getProfesor() != null ? profesorUsuario.getProfesor().getId() : null;
        if (profesorId == null && isDeveloper(profesorUsuario)) {
            com.mattfuncional.entidades.Profesor profesor = getProfesorAcceso(profesorUsuario);
            profesorId = profesor != null ? profesor.getId() : null;
        }
        List<Exercise> ejercicios;
        if (profesorId != null) {
            ejercicios = exerciseService.findEjerciciosDisponiblesParaProfesorWithImages(profesorId);
        } else {
            ejercicios = exerciseService.findAllExercisesWithImages();
        }
        model.addAttribute("ejercicios", ejercicios);
        model.addAttribute("serieDTO", serieDTO);
        model.addAttribute("editMode", true);
        model.addAttribute("usuario", profesorUsuario);

        // Pasar el DTO como JSON para que el JS reciba correctamente ejercicios (nombre, valor, unidad, peso)
        try {
            model.addAttribute("serieDTOJson", new ObjectMapper().writeValueAsString(serieDTO));
        } catch (JsonProcessingException e) {
            model.addAttribute("serieDTOJson", "null");
        }

        return "series/crearSerie"; // Reutilizamos la vista de creación
    }

    @GetMapping("/ver/{id}")
    public String verSerie(@PathVariable Long id, Model model,
            @AuthenticationPrincipal Usuario profesorUsuario) {
        Serie serie = serieService.obtenerSeriePorIdConEjercicios(id);
        boolean esPropietario = isDeveloper(profesorUsuario)
                || (profesorUsuario != null
                && profesorUsuario.getProfesor() != null
                && serie.getProfesor() != null
                && serie.getProfesor().getId().equals(profesorUsuario.getProfesor().getId()));
        if (!esPropietario) {
            return "redirect:/profesor/dashboard?tab=series&error=permiso_serie";
        }
        model.addAttribute("serie", serie);
        return "series/verSerie";
    }

    // PUT: Recibe los datos del formulario y actualiza la serie plantilla
    @PutMapping("/editar/{id}")
    @ResponseBody
    public ResponseEntity<?> actualizarSeriePlantilla(@PathVariable Long id, @RequestBody SerieDTO serieDTO,
            @AuthenticationPrincipal Usuario profesorUsuario) {
        try {
            Serie serieExistente = serieService.obtenerSeriePorId(id);

            boolean esPropietario = isDeveloper(profesorUsuario) || (profesorUsuario.getProfesor() != null && serieExistente.getProfesor() != null &&
                    serieExistente.getProfesor().getId().equals(profesorUsuario.getProfesor().getId()));

            if (!esPropietario) {
                return ResponseEntity.status(403).body("No tiene permiso para editar esta serie.");
            }

            serieService.actualizarSeriePlantilla(id, serieDTO);
            return ResponseEntity.ok().body("Serie actualizada exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar la serie: " + e.getMessage());
        }
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarSerie(@PathVariable Long id, @AuthenticationPrincipal Usuario profesorUsuario) {
        Serie serie = serieService.obtenerSeriePorId(id);

        boolean esPropietario = isDeveloper(profesorUsuario) || (profesorUsuario.getProfesor() != null && serie.getProfesor() != null &&
                serie.getProfesor().getId().equals(profesorUsuario.getProfesor().getId()));

        if (esPropietario) {
            serieService.eliminarSerie(id);
            com.mattfuncional.entidades.Profesor profesor = getProfesorAcceso(profesorUsuario);
            if (profesor != null) {
                return "redirect:/profesor/" + profesor.getId() + "?tab=series";
            }
            return "redirect:/profesor/dashboard?tab=series";
        } else {
            return "redirect:/profesor/dashboard?tab=series&error=permiso_serie";
        }
    }

    private com.mattfuncional.entidades.Profesor getProfesorAcceso(Usuario usuarioActual) {
        if (usuarioActual == null) return null;
        if ("DEVELOPER".equals(usuarioActual.getRol())) {
            return profesorService.getProfesorByCorreo("profesor@mattfuncional.com");
        }
        return usuarioActual.getProfesor();
    }

    private boolean isDeveloper(Usuario usuarioActual) {
        return usuarioActual != null && "DEVELOPER".equals(usuarioActual.getRol());
    }
}