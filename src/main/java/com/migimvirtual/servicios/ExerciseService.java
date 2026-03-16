package com.migimvirtual.servicios;

import com.migimvirtual.entidades.Exercise;
import com.migimvirtual.entidades.GrupoMuscular;
import com.migimvirtual.entidades.Imagen;
import com.migimvirtual.entidades.Usuario;
import com.migimvirtual.excepciones.ResourceNotFoundException;
import com.migimvirtual.repositorios.ExerciseRepository;
import com.migimvirtual.repositorios.ImagenRepository;
import com.migimvirtual.repositorios.ProfesorRepository;
import com.migimvirtual.repositorios.SerieEjercicioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ExerciseService {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseService.class);

    private final ExerciseRepository exerciseRepository;
    private final ImagenRepository imagenRepository;
    private final ImagenServicio imagenServicio;
    private final ProfesorRepository profesorRepository;
    private final SerieEjercicioRepository serieEjercicioRepository;

    public ExerciseService(ExerciseRepository exerciseRepository,
            ImagenRepository imagenRepository,
            ImagenServicio imagenServicio,
            ProfesorRepository profesorRepository,
            SerieEjercicioRepository serieEjercicioRepository) {
        this.exerciseRepository = exerciseRepository;
        this.imagenRepository = imagenRepository;
        this.imagenServicio = imagenServicio;
        this.profesorRepository = profesorRepository;
        this.serieEjercicioRepository = serieEjercicioRepository;
    }

    public List<Exercise> findAllExercises() {
        return exerciseRepository.findAll();
    }
    
    /**
     * Obtiene todos los ejercicios con imágenes cargadas (para admin / export)
     */
    public List<Exercise> findAllExercisesWithImages() {
        return exerciseRepository.findAllWithImages();
    }

    /** Ejercicio del sistema (sin profesor) con ese nombre, para evitar duplicados al importar. Usa findFirst para tolerar duplicados en BD. */
    public Optional<Exercise> findByNameAndProfesorNull(String name) {
        return exerciseRepository.findFirstByNameAndProfesorIsNull(name);
    }

    public Exercise findById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado con ID: " + id));
    }
    
    /**
     * Obtiene un ejercicio por ID con su imagen cargada
     */
    public Exercise findByIdWithImage(Long id) {
        return exerciseRepository.findByIdWithImage(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado con ID: " + id));
    }

    /**
     * Para formulario de edición: carga ejercicio con imagen y grupos y evita LazyInitializationException en la vista.
     */
    public Exercise findByIdWithImageAndGrupos(Long id) {
        return exerciseRepository.findByIdWithImageAndGrupos(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado con ID: " + id));
    }

    @Transactional
    public void saveExercise(Exercise exercise, MultipartFile imageFile) {
        saveExercise(exercise, imageFile, null);
    }
    
    @Transactional
    public void saveExercise(Exercise exercise, MultipartFile imageFile, Usuario usuarioActual) {
        logger.debug("Iniciando guardado de ejercicio: {}", exercise.getName());
        
        // Validar permisos si es edición
        if (exercise.getId() != null && usuarioActual != null) {
            Exercise existente = findById(exercise.getId());
            if (!existente.puedeSerEditadoPor(usuarioActual)) {
                logger.warn("Intento de editar ejercicio sin permisos. Usuario: {}, Ejercicio: {}", 
                           usuarioActual.getCorreo(), exercise.getName());
                throw new SecurityException("No tiene permisos para editar este ejercicio. Solo el administrador puede editar ejercicios predeterminados.");
            }
        }
        
        // Validar que solo admin puede crear ejercicios predeterminados
        if (exercise.isPredeterminado() && usuarioActual != null && !"ADMIN".equals(usuarioActual.getRol())) {
            logger.warn("Intento de crear ejercicio predeterminado por no-admin: {}", usuarioActual.getCorreo());
            throw new SecurityException("Solo el administrador puede crear ejercicios predeterminados.");
        }
        
        // Validar duplicado por nombre + profesor (no global)
        if (exercise.getProfesor() != null) {
            boolean existeDuplicado = exerciseRepository.findByProfesor_Id(exercise.getProfesor().getId())
                    .stream()
                    .anyMatch(e -> e.getName().equalsIgnoreCase(exercise.getName()) 
                            && (exercise.getId() == null || !e.getId().equals(exercise.getId())));
            
            if (existeDuplicado) {
                logger.warn("Intento de guardar ejercicio duplicado para el profesor {}: {}", 
                           exercise.getProfesor().getNombre(), exercise.getName());
                throw new IllegalArgumentException("Ya existe un ejercicio con el mismo nombre para este profesor.");
            }
        } else {
            // Para ejercicios predeterminados (sin profesor), validar globalmente
            List<Exercise> predeterminados = exerciseRepository.findEjerciciosPredeterminados();
            boolean existeDuplicado = predeterminados.stream()
                    .anyMatch(e -> e.getName().equalsIgnoreCase(exercise.getName()) 
                            && (exercise.getId() == null || !e.getId().equals(exercise.getId())));
            if (existeDuplicado) {
                logger.warn("Intento de guardar ejercicio predeterminado duplicado: {}", exercise.getName());
                throw new IllegalArgumentException("Ya existe un ejercicio predeterminado con el mismo nombre.");
            }
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            logger.debug("Procesando imagen para ejercicio: {}", exercise.getName());
            Imagen imagen = imagenServicio.guardar(imageFile);
            exercise.setImagen(imagen);
            logger.debug("Imagen guardada con ID: {}", imagen.getId());
        } else {
            logger.debug("No se proporcionó imagen para el ejercicio: {}", exercise.getName());
        }

        exerciseRepository.save(exercise);
        logger.info("Ejercicio guardado exitosamente: {} con ID: {}", exercise.getName(), exercise.getId());
    }

    /**
     * Guarda un ejercicio sin validar duplicados ni permisos. Solo para restore de backup (suplantar).
     * La imagen ya viene asignada en exercise desde ImagenServicio.guardarParaRestore; imageFile se ignora.
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.MANDATORY)
    public void saveExerciseForRestore(Exercise exercise, MultipartFile imageFile) {
        if (imageFile != null && !imageFile.isEmpty()) {
            Imagen imagen = imagenServicio.guardar(imageFile);
            exercise.setImagen(imagen);
        }
        exerciseRepository.save(exercise);
        logger.debug("Ejercicio guardado (restore): {} con ID: {}", exercise.getName(), exercise.getId());
    }

    @Transactional
    public void modifyExercise(Long exerciseId, Exercise exerciseDetails, MultipartFile imageFile,
            Set<GrupoMuscular> grupos) {
        modifyExercise(exerciseId, exerciseDetails, imageFile, grupos, null);
    }
    
    @Transactional
    public void modifyExercise(Long exerciseId, Exercise exerciseDetails, MultipartFile imageFile,
            Set<GrupoMuscular> grupos, Usuario usuarioActual) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado con ID: " + exerciseId));

        // Validar permisos de edición
        if (usuarioActual != null && !exercise.puedeSerEditadoPor(usuarioActual)) {
            logger.warn("Intento de modificar ejercicio sin permisos. Usuario: {}, Ejercicio: {}", 
                       usuarioActual.getCorreo(), exercise.getName());
            throw new SecurityException("No tiene permisos para editar este ejercicio. Solo el administrador puede editar ejercicios predeterminados.");
        }

        // Validar duplicado por nombre y profesor
        if (!exercise.getName().equals(exerciseDetails.getName())) {
            if (exercise.getProfesor() != null) {
                boolean existe = exerciseRepository.findByProfesor_Id(exercise.getProfesor().getId())
                        .stream()
                        .anyMatch(e -> e.getName().equalsIgnoreCase(exerciseDetails.getName())
                                && !e.getId().equals(exerciseId));
                if (existe) {
                    throw new IllegalArgumentException("Ya existe un ejercicio con el mismo nombre para este profesor.");
                }
            } else {
                // Para predeterminados, validar globalmente
                List<Exercise> predeterminados = exerciseRepository.findEjerciciosPredeterminados();
                boolean existe = predeterminados.stream()
                        .anyMatch(e -> e.getName().equalsIgnoreCase(exerciseDetails.getName())
                                && !e.getId().equals(exerciseId));
                if (existe) {
                    throw new IllegalArgumentException("Ya existe un ejercicio predeterminado con el mismo nombre.");
                }
            }
        }

        exercise.setName(exerciseDetails.getName());
        exercise.setDescription(exerciseDetails.getDescription());
        exercise.setType(exerciseDetails.getType());
        exercise.setVideoUrl(exerciseDetails.getVideoUrl());
        exercise.setInstructions(exerciseDetails.getInstructions());
        exercise.setBenefits(exerciseDetails.getBenefits());
        exercise.setContraindications(exerciseDetails.getContraindications());
        exercise.setGrupos(grupos != null ? grupos : new java.util.HashSet<>());

        if (imageFile != null && !imageFile.isEmpty()) {
            Imagen newImagen = imagenServicio.guardar(imageFile);
            if (exercise.getImagen() != null) {
                imagenRepository.delete(exercise.getImagen());
            }
            exercise.setImagen(newImagen);
        }

        exerciseRepository.save(exercise);
    }

    @Transactional
    public void updateExercise(Exercise exercise) {
        exerciseRepository.save(exercise);
        logger.info("Ejercicio actualizado: {} con ID: {}", exercise.getName(), exercise.getId());
    }

    @Transactional
    public void deleteExercise(Long id) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado con ID: " + id));

        // Eliminar referencias en serie_ejercicio para no violar FK al borrar el ejercicio
        int eliminados = serieEjercicioRepository.deleteByExerciseId(id);
        if (eliminados > 0) {
            logger.info("Eliminadas {} referencias de SerieEjercicio para el ejercicio id={}", eliminados, id);
        }

        if (exercise.getImagen() != null) {
            Imagen imagen = exercise.getImagen();
            exercise.setImagen(null);
            exerciseRepository.save(exercise);
            if (!"not_imagen.png".equals(imagen.getNombre())) {
                try {
                    imagenServicio.eliminarImagen(imagen.getId());
                } catch (Exception e) {
                    logger.warn("No se pudo eliminar archivo físico de imagen {}: {}", imagen.getId(), e.getMessage());
                    imagenRepository.delete(imagen);
                }
            } else {
                imagenRepository.delete(imagen);
            }
        }
        exerciseRepository.deleteById(id);
    }

    @Transactional
    public void updateImage(Long id, Imagen nuevaImagen) {
        Exercise exercise = exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ejercicio no encontrado con ID: " + id));

        exercise.setImagen(nuevaImagen);
        exerciseRepository.save(exercise);

    }

    public List<Exercise> findExercisesByGrupo(GrupoMuscular grupo) {
        return exerciseRepository.findByGruposContaining(grupo);
    }

    public List<Exercise> findExercisesByGrupoId(Long grupoId) {
        if (grupoId == null) return List.of();
        return exerciseRepository.findByGrupoId(grupoId);
    }

    @Transactional
    public void deleteAllExercises() {
        try {
            logger.info("Iniciando limpieza completa de ejercicios...");
            
            // Primero eliminar todas las imágenes asociadas
            List<Exercise> exercises = exerciseRepository.findAll();
            logger.info("Encontrados {} ejercicios para eliminar", exercises.size());
            
            for (Exercise exercise : exercises) {
                if (exercise.getImagen() != null) {
                    Imagen imagen = exercise.getImagen();
                    if (!"not_imagen.png".equals(imagen.getNombre())) {
                        imagenRepository.delete(imagen);
                        logger.debug("Imagen eliminada: {}", imagen.getNombre());
                    }
                }
            }
            
            // Luego eliminar todos los ejercicios
            exerciseRepository.deleteAll();
            logger.info("Limpieza completa de ejercicios finalizada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error durante la limpieza de ejercicios: {}", e.getMessage(), e);
            throw new RuntimeException("Error al limpiar ejercicios: " + e.getMessage(), e);
        }
    }

    public List<Exercise> findExercisesByIds(List<Long> ids) {
        return exerciseRepository.findAllById(ids);
    }

    /**
     * DEPRECADO: Los ejercicios predeterminados ahora son compartidos (profesor = null)
     * Los profesores ya tienen acceso automático a todos los ejercicios predeterminados
     * Este método ya no copia ejercicios porque no es necesario
     * 
     * @param profesor Profesor al que se le "asignarían" ejercicios (ya no necesario)
     */
    @Deprecated
    @Transactional
    public void asignarEjerciciosPredefinidosAProfesor(com.migimvirtual.entidades.Profesor profesor) {
        logger.warn("=== MÉTODO DEPRECADO: asignarEjerciciosPredefinidosAProfesor ===");
        logger.warn("Los ejercicios predeterminados ahora son compartidos (profesor = null)");
        logger.warn("Los profesores ya tienen acceso automático a todos los ejercicios predeterminados");
        logger.warn("No es necesario copiar ejercicios. Use findEjerciciosDisponiblesParaProfesor() para obtener ejercicios.");
        
        logger.info("Verificando acceso del profesor {} (ID: {}) a ejercicios predeterminados", 
                   profesor.getCorreo(), profesor.getId());
        
        // Verificar que hay ejercicios predeterminados disponibles
        long countPredeterminados = exerciseRepository.countEjerciciosPredeterminados();
        logger.info("Ejercicios predeterminados disponibles en el sistema: {}", countPredeterminados);
        
        if (countPredeterminados == 0) {
            throw new RuntimeException("No hay ejercicios predeterminados en el sistema. Cargue ejercicios predeterminados primero.");
        }
        
        logger.info("El profesor {} ya tiene acceso a {} ejercicios predeterminados compartidos", 
                   profesor.getCorreo(), countPredeterminados);
        logger.info("No se copian ejercicios porque son compartidos (profesor = null)");
        
        // No hacer nada más - los ejercicios predeterminados ya están disponibles para todos los profesores
    }

    public List<Exercise> findExercisesByProfesorId(Long profesorId) {
        return exerciseRepository.findByProfesor_Id(profesorId);
    }
    
    public List<Exercise> findExercisesByProfesorIdWithImages(Long profesorId) {
        return exerciseRepository.findByProfesor_IdWithImages(profesorId);
    }
    
    public List<Exercise> findExercisesByProfesorIdWithoutImages(Long profesorId) {
        return exerciseRepository.findByProfesor_IdWithoutImages(profesorId);
    }
    
    // ============================================
    // NUEVOS MÉTODOS PARA EJERCICIOS PREDETERMINADOS
    // ============================================
    
    /**
     * Obtiene todos los ejercicios predeterminados
     */
    public List<Exercise> findEjerciciosPredeterminados() {
        return exerciseRepository.findEjerciciosPredeterminados();
    }
    
    /**
     * Obtiene ejercicios disponibles para un profesor (predeterminados + propios)
     * Este es el método principal que deben usar los profesores para ver ejercicios disponibles
     */
    public List<Exercise> findEjerciciosDisponiblesParaProfesor(Long profesorId) {
        return exerciseRepository.findEjerciciosDisponiblesParaProfesor(profesorId);
    }
    
    /**
     * Obtiene ejercicios disponibles para un profesor con imagen y grupos ya cargados.
     * Inicializa grupos dentro de la transacción para que la vista no provoque LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public List<Exercise> findEjerciciosDisponiblesParaProfesorWithImages(Long profesorId) {
        List<Exercise> list = exerciseRepository.findEjerciciosDisponiblesParaProfesorWithImages(profesorId);
        for (Exercise e : list) {
            if (e.getGrupos() != null) {
                e.getGrupos().size();
            }
        }
        return list;
    }
    
    /**
     * Obtiene solo los ejercicios propios del profesor (excluyendo predeterminados)
     */
    public List<Exercise> findEjerciciosPropiosDelProfesor(Long profesorId) {
        return exerciseRepository.findEjerciciosPropiosDelProfesor(profesorId);
    }
    
    /**
     * Obtiene solo los ejercicios propios del profesor con imágenes
     */
    public List<Exercise> findEjerciciosPropiosDelProfesorWithImages(Long profesorId) {
        return exerciseRepository.findEjerciciosPropiosDelProfesorWithImages(profesorId);
    }
    
    /**
     * Cuenta ejercicios predeterminados
     */
    public long countEjerciciosPredeterminados() {
        return exerciseRepository.countEjerciciosPredeterminados();
    }
    
    /**
     * Cuenta ejercicios propios de un profesor (excluyendo predeterminados)
     */
    public long countEjerciciosPropiosDelProfesor(Long profesorId) {
        return exerciseRepository.countEjerciciosPropiosDelProfesor(profesorId);
    }

    /**
     * Cuenta ejercicios disponibles para un profesor (predeterminados + propios)
     */
    public long countEjerciciosDisponiblesParaProfesor(Long profesorId) {
        return exerciseRepository.countEjerciciosDisponiblesParaProfesor(profesorId);
    }
    
    /**
     * Verifica si un usuario puede editar un ejercicio
     */
    public boolean canEditExercise(Long exerciseId, Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        Exercise exercise = findById(exerciseId);
        return exercise.puedeSerEditadoPor(usuario);
    }
}

