package com.mattfuncional.repositorios;

import com.mattfuncional.entidades.GrupoMuscular;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoMuscularRepository extends JpaRepository<GrupoMuscular, Long> {

    /** Grupos del sistema (profesor_id null). */
    List<GrupoMuscular> findByProfesorIsNullOrderByNombreAsc();

    /** Grupos creados por un profesor. */
    List<GrupoMuscular> findByProfesorIdOrderByNombreAsc(Long profesorId);

    /** Grupos disponibles para un profesor: del sistema + los suyos. */
    @Query("SELECT g FROM GrupoMuscular g WHERE g.profesor IS NULL OR g.profesor.id = :profesorId ORDER BY g.nombre")
    List<GrupoMuscular> findDisponiblesParaProfesor(@Param("profesorId") Long profesorId);

    Optional<GrupoMuscular> findByNombreAndProfesorIsNull(String nombre);

    Optional<GrupoMuscular> findByNombreAndProfesorId(String nombre, Long profesorId);

    /** Evita "Query did not return unique result" si hay duplicados en BD. */
    Optional<GrupoMuscular> findFirstByNombreAndProfesorIsNull(String nombre);

    Optional<GrupoMuscular> findFirstByNombreAndProfesorId(String nombre, Long profesorId);
}
