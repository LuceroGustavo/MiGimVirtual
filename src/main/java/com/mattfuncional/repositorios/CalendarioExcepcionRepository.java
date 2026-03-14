package com.mattfuncional.repositorios;

import com.mattfuncional.entidades.CalendarioExcepcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface CalendarioExcepcionRepository extends JpaRepository<CalendarioExcepcion, Long> {
    List<CalendarioExcepcion> findByProfesor_IdAndFechaBetween(Long profesorId, LocalDate desde, LocalDate hasta);

    boolean existsByUsuario_IdAndFechaAndHoraInicioAndHoraFin(Long usuarioId, LocalDate fecha, LocalTime horaInicio,
            LocalTime horaFin);

    void deleteByUsuario_Id(Long usuarioId);
}
