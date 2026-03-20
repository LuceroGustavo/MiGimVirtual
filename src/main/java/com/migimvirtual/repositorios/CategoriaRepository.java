package com.migimvirtual.repositorios;

import com.migimvirtual.entidades.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /** Categorías del sistema (profesor_id null). */
    List<Categoria> findByProfesorIsNullOrderByNombreAsc();

    /** Categorías creadas por un profesor. */
    List<Categoria> findByProfesorIdOrderByNombreAsc(Long profesorId);

    /** Categorías disponibles para un profesor: del sistema + las suyas. */
    @Query("SELECT c FROM Categoria c WHERE c.profesor IS NULL OR c.profesor.id = :profesorId ORDER BY c.nombre")
    List<Categoria> findDisponiblesParaProfesor(@Param("profesorId") Long profesorId);

    Optional<Categoria> findFirstByNombreAndProfesorIsNull(String nombre);

    Optional<Categoria> findFirstByNombreAndProfesorId(String nombre, Long profesorId);
}
