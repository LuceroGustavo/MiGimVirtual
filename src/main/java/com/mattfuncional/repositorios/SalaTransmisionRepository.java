package com.mattfuncional.repositorios;

import com.mattfuncional.entidades.SalaTransmision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalaTransmisionRepository extends JpaRepository<SalaTransmision, Long> {

    Optional<SalaTransmision> findByProfesorId(Long profesorId);

    Optional<SalaTransmision> findByToken(String token);

    boolean existsByToken(String token);
}
