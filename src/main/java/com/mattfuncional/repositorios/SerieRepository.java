package com.mattfuncional.repositorios;

import com.mattfuncional.entidades.Serie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Long> {

    /** Carga una serie con sus SerieEjercicios y Exercise para evitar LazyInitializationException al editar. */
    @Query("SELECT s FROM Serie s LEFT JOIN FETCH s.serieEjercicios se LEFT JOIN FETCH se.exercise WHERE s.id = :id")
    Optional<Serie> findByIdWithSerieEjercicios(@Param("id") Long id);

    /** Carga varias series por id con serieEjercicios y exercise (para hoja pública de rutina). */
    @Query("SELECT DISTINCT s FROM Serie s LEFT JOIN FETCH s.serieEjercicios se LEFT JOIN FETCH se.exercise WHERE s.id IN :ids")
    List<Serie> findByIdInWithSerieEjercicios(@Param("ids") List<Long> ids);

    // Buscar series por rutina ordenadas por orden
    List<Serie> findByRutinaIdOrderByOrdenAsc(Long rutinaId);

    // Buscar series por rutina
    List<Serie> findByRutinaId(Long rutinaId);

    // Contar series por rutina
    long countByRutinaId(Long rutinaId);

    // Buscar serie por nombre y rutina (puede haber varias si hubo duplicados; usar lista para no fallar con NonUniqueResult)
    Serie findByNombreAndRutinaId(String nombre, Long rutinaId);

    /** Para modo Agregar: comprobar si ya existe alguna serie con ese nombre en la rutina (evitar duplicados). Devuelve lista para no fallar si hay varias. */
    List<Serie> findAllByNombreAndRutinaId(String nombre, Long rutinaId);

    /** Para modo Agregar: comprobar si ya existe alguna serie standalone con ese nombre y profesor. Devuelve lista para no fallar si hay varias. */
    List<Serie> findAllByNombreAndRutinaIsNullAndProfesor_Id(String nombre, Long profesorId);

    // Buscar series plantilla por profesor
    List<Serie> findByProfesorIdAndEsPlantillaTrue(Long profesorId);

    // Buscar series plantilla por profesor y nombre
    List<Serie> findByProfesorIdAndEsPlantillaTrueAndNombreContainingIgnoreCase(Long profesorId, String nombre);

    // Buscar todas las series plantilla
    List<Serie> findByEsPlantillaTrue();

    /** Series plantilla que no pertenecen a ninguna rutina (biblioteca del profesor). */
    List<Serie> findByEsPlantillaTrueAndRutinaIsNull();

    // Buscar series plantilla por creador
    List<Serie> findByCreadorAndEsPlantillaTrue(String creador);

    List<Serie> findByProfesorId(Long profesorId);

    @Transactional
    @Modifying
    void deleteByRutinaId(Long rutinaId);
}