package com.mattfuncional.repositorios;

import com.mattfuncional.entidades.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
    /** Primer profesor con ese correo (evita error "unique result" si hay duplicados en BD). */
    Optional<Profesor> findFirstByCorreo(String correo);

    // --- CONSULTAS OPTIMIZADAS PARA FASE 3 ---

    // Obtener profesores con usuarios cargados
    @Query("SELECT p FROM Profesor p LEFT JOIN FETCH p.usuarios")
    List<Profesor> findAllWithUsuarios();

    // Obtener profesor específico con todas las relaciones (DISTINCT evita filas duplicadas por el JOIN)
    @Query("SELECT DISTINCT p FROM Profesor p LEFT JOIN FETCH p.usuarios WHERE p.id = :id")
    Profesor findByIdWithRelations(@Param("id") Long id);

    // Obtener profesores con conteo de alumnos
    @Query("SELECT p, COUNT(u) as alumnoCount FROM Profesor p LEFT JOIN p.usuarios u GROUP BY p")
    List<Object[]> findAllWithAlumnoCount();

    /** Detecta correos duplicados en profesores. Para avisos de calidad de datos. */
    @Query(value = "SELECT correo, COUNT(*) AS cnt FROM profesor GROUP BY correo HAVING COUNT(*) > 1", nativeQuery = true)
    List<Object[]> findCorreosDuplicados();
}
