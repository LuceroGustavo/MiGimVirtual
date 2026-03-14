package com.mattfuncional.repositorios;

import com.mattfuncional.entidades.SlotConfig;
import com.mattfuncional.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.Optional;

@Repository
public interface SlotConfigRepository extends JpaRepository<SlotConfig, Long> {
    /** Primer slot con ese (dia, horaInicio). Evita "Query did not return a unique result" si hay duplicados en BD. */
    Optional<SlotConfig> findFirstByDiaAndHoraInicio(DiaSemana dia, LocalTime horaInicio);

    /** Todos los slots con ese (dia, horaInicio). Para detectar y limpiar duplicados. */
    java.util.List<SlotConfig> findAllByDiaAndHoraInicio(DiaSemana dia, LocalTime horaInicio);
} 