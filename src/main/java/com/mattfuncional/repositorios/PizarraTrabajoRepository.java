package com.mattfuncional.repositorios;

import com.mattfuncional.entidades.PizarraTrabajo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PizarraTrabajoRepository extends JpaRepository<PizarraTrabajo, Long> {

    Optional<PizarraTrabajo> findByProfesorId(Long profesorId);
}
