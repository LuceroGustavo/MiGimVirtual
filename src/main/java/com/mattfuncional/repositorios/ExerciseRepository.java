package com.mattfuncional.repositorios;

import com.mattfuncional.entidades.Exercise;
import com.mattfuncional.entidades.GrupoMuscular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    Optional<Exercise> findByName(String name);

    /** Para saber si ya existe un ejercicio predeterminado con ese nombre (evitar duplicados al asegurar). */
    Optional<Exercise> findByNameAndProfesorIsNull(String name);

    /** Igual que findByNameAndProfesorIsNull pero usa findFirst para evitar "Query did not return unique result" si hay duplicados en BD. */
    Optional<Exercise> findFirstByNameAndProfesorIsNull(String name);

    @Query("SELECT e FROM Exercise e JOIN e.grupos g WHERE g = :grupo")
    List<Exercise> findByGruposContaining(@Param("grupo") GrupoMuscular grupo);

    @Query("SELECT e FROM Exercise e JOIN e.grupos g WHERE g.id = :grupoId")
    List<Exercise> findByGrupoId(@Param("grupoId") Long grupoId);

    List<Exercise> findByProfesorIsNull();

    List<Exercise> findByProfesor_Id(Long profesorId);
    
    @Query("SELECT e FROM Exercise e LEFT JOIN FETCH e.imagen WHERE e.profesor.id = :profesorId")
    List<Exercise> findByProfesor_IdWithImages(@Param("profesorId") Long profesorId);
    
    @Query("SELECT e FROM Exercise e WHERE e.profesor.id = :profesorId")
    List<Exercise> findByProfesor_IdWithoutImages(@Param("profesorId") Long profesorId);
    
    // Métodos para el servicio de asignación optimizada
    @Query("SELECT e.name FROM Exercise e WHERE e.profesor.id = :profesorId")
    List<String> findNombresByProfesorId(@Param("profesorId") Long profesorId);
    
    @Query("SELECT COUNT(e) FROM Exercise e WHERE e.profesor.id = :profesorId")
    long countByProfesor_Id(@Param("profesorId") Long profesorId);
    
    void deleteByProfesor_Id(@Param("profesorId") Long profesorId);
    
    // ============================================
    // NUEVOS MÉTODOS PARA EJERCICIOS PREDETERMINADOS
    // ============================================
    
    /**
     * Obtiene todos los ejercicios predeterminados (sin profesor o con flag activado)
     * Incluye LEFT JOIN FETCH para cargar las imágenes
     */
    @Query("SELECT DISTINCT e FROM Exercise e LEFT JOIN FETCH e.imagen WHERE e.profesor IS NULL OR e.esPredeterminado = true")
    List<Exercise> findEjerciciosPredeterminados();
    
    /**
     * Obtiene ejercicios disponibles para un profesor (predeterminados + propios)
     * Incluye:
     * - Ejercicios predeterminados (profesor IS NULL o esPredeterminado = true)
     * - Ejercicios propios del profesor (profesor.id = :profesorId y esPredeterminado = false)
     */
    @Query("SELECT e FROM Exercise e WHERE " +
           "(e.profesor IS NULL OR e.esPredeterminado = true) OR " +
           "(e.profesor.id = :profesorId AND (e.esPredeterminado = false OR e.esPredeterminado IS NULL))")
    List<Exercise> findEjerciciosDisponiblesParaProfesor(@Param("profesorId") Long profesorId);
    
    /**
     * Obtiene ejercicios disponibles para un profesor con imágenes cargadas.
     * Los grupos se inicializan en el servicio dentro de una transacción para evitar LazyInitializationException.
     */
    @Query("SELECT e FROM Exercise e LEFT JOIN FETCH e.imagen WHERE " +
           "(e.profesor IS NULL OR e.esPredeterminado = true) OR " +
           "(e.profesor.id = :profesorId AND (e.esPredeterminado = false OR e.esPredeterminado IS NULL))")
    List<Exercise> findEjerciciosDisponiblesParaProfesorWithImages(@Param("profesorId") Long profesorId);
    
    /**
     * Obtiene solo los ejercicios propios del profesor (excluyendo predeterminados)
     */
    @Query("SELECT e FROM Exercise e WHERE e.profesor.id = :profesorId AND " +
           "(e.esPredeterminado = false OR e.esPredeterminado IS NULL)")
    List<Exercise> findEjerciciosPropiosDelProfesor(@Param("profesorId") Long profesorId);
    
    /**
     * Obtiene solo los ejercicios propios del profesor con imágenes
     */
    @Query("SELECT e FROM Exercise e LEFT JOIN FETCH e.imagen WHERE e.profesor.id = :profesorId AND " +
           "(e.esPredeterminado = false OR e.esPredeterminado IS NULL)")
    List<Exercise> findEjerciciosPropiosDelProfesorWithImages(@Param("profesorId") Long profesorId);
    
    /**
     * Cuenta ejercicios predeterminados
     */
    @Query("SELECT COUNT(e) FROM Exercise e WHERE e.profesor IS NULL OR e.esPredeterminado = true")
    long countEjerciciosPredeterminados();
    
    /**
     * Obtiene todos los ejercicios con imágenes cargadas (para admin)
     */
    @Query("SELECT DISTINCT e FROM Exercise e LEFT JOIN FETCH e.imagen")
    List<Exercise> findAllWithImages();
    
    /**
     * Obtiene un ejercicio por ID con su imagen cargada
     */
    @Query("SELECT e FROM Exercise e LEFT JOIN FETCH e.imagen WHERE e.id = :id")
    Optional<Exercise> findByIdWithImage(@Param("id") Long id);

    /**
     * Ejercicio por ID con imagen y grupos (para formulario de edición, evita LazyInitializationException).
     */
    @Query("SELECT DISTINCT e FROM Exercise e LEFT JOIN FETCH e.imagen LEFT JOIN FETCH e.grupos WHERE e.id = :id")
    Optional<Exercise> findByIdWithImageAndGrupos(@Param("id") Long id);

    /**
     * Cuenta ejercicios propios de un profesor (excluyendo predeterminados)
     */
    @Query("SELECT COUNT(e) FROM Exercise e WHERE e.profesor.id = :profesorId AND " +
           "(e.esPredeterminado = false OR e.esPredeterminado IS NULL)")
    long countEjerciciosPropiosDelProfesor(@Param("profesorId") Long profesorId);
}