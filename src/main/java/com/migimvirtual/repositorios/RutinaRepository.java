package com.migimvirtual.repositorios;

import com.migimvirtual.entidades.Rutina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutinaRepository extends JpaRepository<Rutina, Long> {

    /** Carga la rutina con sus series para evitar LazyInitializationException al editar. (El orden se aplica en el servicio.) */
    @Query("SELECT DISTINCT r FROM Rutina r LEFT JOIN FETCH r.series WHERE r.id = :id")
    Optional<Rutina> findByIdWithSeries(@Param("id") Long id);

    List<Rutina> findByUsuarioId(Long usuarioId); // ← ESTA ES LA CLAVE

    List<Rutina> findByProfesorId(Long profesorId);

    List<Rutina> findByUsuarioIdAndEstado(Long usuarioId, String estado);

    List<Rutina> findByProfesorIdAndEstado(Long profesorId, String estado);

    List<Rutina> findByEstado(String estado);

    // Buscar rutinas plantilla por profesor
    List<Rutina> findByProfesorIdAndEsPlantillaTrue(Long profesorId);

    // Buscar rutinas plantilla por profesor y categoría
    List<Rutina> findByProfesorIdAndEsPlantillaTrueAndCategoria(Long profesorId, String categoria);

    // Buscar rutinas plantilla por profesor y nombre
    List<Rutina> findByProfesorIdAndEsPlantillaTrueAndNombreContainingIgnoreCase(Long profesorId, String nombre);

    Optional<Rutina> findByNombreAndEsPlantillaTrueAndProfesorId(String nombre, Long profesorId);

    /** Evita "Query did not return unique result" si hay rutinas duplicadas. */
    Optional<Rutina> findFirstByNombreAndEsPlantillaTrueAndProfesorId(String nombre, Long profesorId);

    // Buscar todas las rutinas plantilla
    List<Rutina> findByEsPlantillaTrue();

    // Buscar rutinas plantilla por creador
    List<Rutina> findByCreadorAndEsPlantillaTrue(String creador);

    // Buscar rutinas asignadas a usuarios por profesor
    List<Rutina> findByProfesorIdAndEsPlantillaFalse(Long profesorId);

    // Buscar rutinas asignadas a un usuario específico (no plantillas)
    List<Rutina> findByUsuarioIdAndEsPlantillaFalse(Long usuarioId);

    Optional<Rutina> findByTokenPublico(String tokenPublico);

    /** Carga la rutina por token con sus series (sin serieEjercicios, para combinar con carga por serie). */
    @Query("SELECT DISTINCT r FROM Rutina r LEFT JOIN FETCH r.series WHERE r.tokenPublico = :token")
    Optional<Rutina> findByTokenPublicoWithSeries(@Param("token") String tokenPublico);

    boolean existsByTokenPublico(String tokenPublico);

}
