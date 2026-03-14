package com.mattfuncional.repositorios;

import com.mattfuncional.entidades.Pizarra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PizarraRepository extends JpaRepository<Pizarra, Long> {

    List<Pizarra> findByProfesorIdOrderByFechaModificacionDesc(Long profesorId);

    Optional<Pizarra> findByToken(String token);

    boolean existsByToken(String token);

    @Query("SELECT p FROM Pizarra p LEFT JOIN FETCH p.columnas WHERE p.id = :id")
    Optional<Pizarra> findByIdWithColumnas(@Param("id") Long id);
}
