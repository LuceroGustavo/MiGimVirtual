package com.migimvirtual.controladores;

import com.migimvirtual.entidades.Exercise;
import com.migimvirtual.entidades.GrupoMuscular;
import com.migimvirtual.entidades.Imagen;
import com.migimvirtual.servicios.ExerciseService;
import com.migimvirtual.servicios.GrupoMuscularService;
import com.migimvirtual.servicios.ImagenServicio;
import com.migimvirtual.servicios.ProfesorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
public class ExerciseController {

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private ImagenServicio imagenServicio;

    @Autowired
    private ProfesorService profesorService;

    @Autowired
    private GrupoMuscularService grupoMuscularService;

    // Método para listar todos los ejercicios
    @GetMapping("/exercise/lista")
    public String getExerciseList(@RequestParam(name = "grupoId", required = false) Long grupoId,
            Model model, @AuthenticationPrincipal com.migimvirtual.entidades.Usuario usuarioActual) {
        List<Exercise> exercises;
        List<GrupoMuscular> gruposMusculares;
        if (usuarioActual != null && isAdminOrDeveloper(usuarioActual)) {
            com.migimvirtual.entidades.Profesor profesor = getProfesorAcceso(usuarioActual);
            Long profesorId = profesor != null ? profesor.getId() : null;
            if (profesorId == null) {
                gruposMusculares = grupoMuscularService.findGruposSistema();
                exercises = exerciseService.findEjerciciosPredeterminados();
            } else {
                gruposMusculares = grupoMuscularService.findDisponiblesParaProfesor(profesorId);
                exercises = exerciseService.findEjerciciosDisponiblesParaProfesor(profesorId);
            }
        } else {
            gruposMusculares = grupoMuscularService.findGruposSistema();
            exercises = exerciseService.findEjerciciosPredeterminados();
        }
        if (grupoId != null) {
            exercises = exercises.stream()
                    .filter(e -> e.getGrupos() != null && e.getGrupos().stream().anyMatch(g -> grupoId.equals(g.getId())))
                    .toList();
        }
        model.addAttribute("exercises", exercises);
        model.addAttribute("gruposMusculares", gruposMusculares);
        model.addAttribute("selectedGrupoId", grupoId);
        return "ejercicios/exercise-lista";
    }

    // Formulario y guardado de ejercicios unificados en /profesor/mis-ejercicios (una sola base para todos)
    @GetMapping("/ejercicios/nuevo")
    public String cargarFormularioEjercicio() {
        return "redirect:/profesor/mis-ejercicios/nuevo";
    }

    @PostMapping("/ejercicios/nuevo")
    public String guardarEjercicio() {
        return "redirect:/profesor/mis-ejercicios";
    }

    // ABM de ejercicios no se usa en esta app; todo se hace desde Mis Ejercicios (/profesor/mis-ejercicios).
    // Redirigir para que enlaces antiguos lleven al profesor a su lista.
    @GetMapping("/exercise/editar")
    public String listarEjercicios() {
        return "redirect:/profesor/mis-ejercicios";
    }

    @GetMapping("/ejercicios/abm")
    public String abmEjercicios() {
        return "redirect:/profesor/mis-ejercicios";
    }

    @GetMapping("/ejercicios/modificar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Long id) {
        return "redirect:/profesor/mis-ejercicios/editar/" + id;
    }

    @PostMapping("/ejercicios/modificar/{id}")
    public String procesarFormularioEdicion(@PathVariable("id") Long id) {
        return "redirect:/profesor/mis-ejercicios/editar/" + id;
    }

    @GetMapping("/ejercicios/eliminar/{id}")
    public String eliminarEjercicio(@PathVariable("id") Long id,
            @AuthenticationPrincipal com.migimvirtual.entidades.Usuario usuarioActual) {
        Exercise exercise = exerciseService.findById(id);
        // Solo el admin, developer o el profesor dueño puede eliminar
        if (usuarioActual != null && isAdminOrDeveloper(usuarioActual) && !isDeveloper(usuarioActual)) {
            if (exercise.getProfesor() == null
                    || usuarioActual.getProfesor() == null
                    || !exercise.getProfesor().getId().equals(usuarioActual.getProfesor().getId())) {
                return "redirect:/profesor/mis-ejercicios?error=permiso";
            }
        }
        exerciseService.deleteExercise(id);
        return "redirect:/profesor/mis-ejercicios";
    }

    // Método para cambiar la imagen de un ejercicio
    @PostMapping("/ejercicios/cambiar-imagen/{id}")
    public String cambiarImagen(@PathVariable("id") Long id,
            @RequestParam("image") MultipartFile imageFile,
            Model model) {
        try {
            if (!imageFile.isEmpty()) {
                // Obtener el ejercicio actual
                Exercise exercise = exerciseService.findById(id);
                if (exercise == null) {
                    throw new RuntimeException("Ejercicio no encontrado con ID: " + id);
                }

                if (exercise.getImagen() != null) {
                    // Actualizar la imagen existente
                    Imagen nuevaImagen = imagenServicio.actualizar(imageFile, exercise.getImagen().getId());
                    exerciseService.updateImage(id, nuevaImagen);
                } else {
                    // Crear una nueva imagen y asociarla al ejercicio
                    Imagen nuevaImagen = imagenServicio.guardar(imageFile);
                    exerciseService.updateImage(id, nuevaImagen);
                }
            }
            return "redirect:/profesor/mis-ejercicios";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            return "redirect:/profesor/mis-ejercicios";
        }
    }

    // ========== MÉTODOS ESPECÍFICOS PARA PROFESOR ==========

    // Lista de ejercicios del profesor - REDIRIGIDO A NUEVA VISTA OPTIMIZADA
    @GetMapping("/profesor/ejercicios")
    public String getProfesorExerciseList(@RequestParam(name = "muscleGroup", required = false) String muscleGroupStr,
            @RequestParam(name = "search", required = false) String search,
            @AuthenticationPrincipal com.migimvirtual.entidades.Usuario usuarioActual) {
        
        // Redirigir a la nueva vista optimizada con tabla
        return "redirect:/profesor/mis-ejercicios";
    }

    // Formulario para crear nuevo ejercicio (profesor) - REDIRIGIDO A NUEVA VISTA
    @GetMapping("/profesor/ejercicios/nuevo")
    public String cargarFormularioEjercicioProfesor(@AuthenticationPrincipal com.migimvirtual.entidades.Usuario usuarioActual) {
        
        // Redirigir a la nueva vista optimizada
        return "redirect:/profesor/mis-ejercicios/nuevo";
    }

    // Guardar nuevo ejercicio (profesor)
    @PostMapping("/profesor/ejercicios/nuevo")
    public String guardarEjercicioProfesor(@Valid @ModelAttribute("exercise") Exercise exercise,
            BindingResult bindingResult,
            @RequestParam(name = "grupoIds", required = false) List<Long> grupoIds,
            @RequestParam("image") MultipartFile imageFile,
            Model model,
            @AuthenticationPrincipal com.migimvirtual.entidades.Usuario usuarioActual) {

        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/login";
        }

        // Buscar el profesor por correo si no está asociado directamente
        com.migimvirtual.entidades.Profesor profesor = getProfesorAcceso(usuarioActual);
        if (profesor == null) {
            return "redirect:/login?error=profesor_no_encontrado";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("gruposMusculares", grupoMuscularService.findDisponiblesParaProfesor(profesor.getId()));
            model.addAttribute("profesor", profesor);
            return "ejercicios/formulario-ejercicio";
        }
        exercise.setProfesor(profesor);
        exercise.setGrupos(grupoMuscularService.resolveGruposByIds(grupoIds != null ? grupoIds : List.of()));
        try {
            exerciseService.saveExercise(exercise, imageFile);
            return "redirect:/profesor/mis-ejercicios";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("gruposMusculares", grupoMuscularService.findDisponiblesParaProfesor(profesor.getId()));
            model.addAttribute("profesor", profesor);
            return "ejercicios/formulario-ejercicio";
        }
    }

    // Formulario para editar ejercicio (profesor) - REDIRIGIDO A NUEVA VISTA
    @GetMapping("/profesor/ejercicios/editar/{id}")
    public String mostrarFormularioEdicionProfesor(@PathVariable("id") Long id,
            @AuthenticationPrincipal com.migimvirtual.entidades.Usuario usuarioActual) {
        
        // Redirigir a la nueva vista optimizada
        return "redirect:/profesor/mis-ejercicios/editar/" + id;
    }

    // Procesar edición de ejercicio (profesor)
    @PostMapping("/profesor/ejercicios/editar/{id}")
    public String procesarFormularioEdicionProfesor(@PathVariable("id") Long id,
            @Valid @ModelAttribute("exercise") Exercise exercise,
            BindingResult bindingResult,
            @RequestParam(name = "grupoIds", required = false) List<Long> grupoIds,
            @RequestParam("image") MultipartFile imageFile,
            Model model,
            @AuthenticationPrincipal com.migimvirtual.entidades.Usuario usuarioActual) {

        if (usuarioActual == null || !isAdminOrDeveloper(usuarioActual)) {
            return "redirect:/login";
        }

        com.migimvirtual.entidades.Profesor profesor = getProfesorAcceso(usuarioActual);
        if (profesor == null) {
            return "redirect:/login?error=profesor_no_encontrado";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("gruposMusculares", grupoMuscularService.findDisponiblesParaProfesor(profesor.getId()));
            model.addAttribute("profesor", profesor);
            return "ejercicios/formulario-modificar-ejercicio";
        }
        Exercise original = exerciseService.findById(id);
        if (original == null) {
            return "redirect:/profesor/mis-ejercicios";
        }
        if (!isDeveloper(usuarioActual) && original.getProfesor() != null && !original.getProfesor().getId().equals(profesor.getId())) {
            return "redirect:/profesor/mis-ejercicios?error=permiso";
        }
        exercise.setProfesor(profesor);
        Set<GrupoMuscular> grupos = grupoMuscularService.resolveGruposByIds(grupoIds != null ? grupoIds : List.of());
        exercise.setGrupos(grupos);
        try {
            exerciseService.modifyExercise(id, exercise, imageFile, grupos);
            return "redirect:/profesor/mis-ejercicios";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("gruposMusculares", grupoMuscularService.findDisponiblesParaProfesor(profesor.getId()));
            model.addAttribute("profesor", profesor);
            return "ejercicios/formulario-modificar-ejercicio";
        }
    }

    // Eliminar ejercicio (profesor) - REDIRIGIDO A NUEVA VISTA
    @GetMapping("/profesor/ejercicios/eliminar/{id}")
    public String eliminarEjercicioProfesor(@PathVariable("id") Long id,
            @AuthenticationPrincipal com.migimvirtual.entidades.Usuario usuarioActual) {
        
        // Redirigir a la nueva vista optimizada
        return "redirect:/profesor/mis-ejercicios";
    }

    private boolean isAdminOrDeveloper(com.migimvirtual.entidades.Usuario usuarioActual) {
        return usuarioActual != null && ("ADMIN".equals(usuarioActual.getRol()) || "DEVELOPER".equals(usuarioActual.getRol()));
    }

    private boolean isDeveloper(com.migimvirtual.entidades.Usuario usuarioActual) {
        return usuarioActual != null && "DEVELOPER".equals(usuarioActual.getRol());
    }

    private com.migimvirtual.entidades.Profesor getProfesorAcceso(com.migimvirtual.entidades.Usuario usuarioActual) {
        if (usuarioActual == null) return null;
        if ("DEVELOPER".equals(usuarioActual.getRol())) {
            return profesorService.getProfesorByCorreo("profesor@migymvirtual.com");
        }
        return usuarioActual.getProfesor() != null ? usuarioActual.getProfesor() : profesorService.getProfesorByCorreo(usuarioActual.getCorreo());
    }
}