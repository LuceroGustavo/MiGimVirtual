package com.mattfuncional.servicios;

import com.mattfuncional.entidades.Rutina;
import com.mattfuncional.repositorios.AsistenciaRepository;
import com.mattfuncional.repositorios.RutinaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para depuración de datos antiguos: asistencias y rutinas asignadas.
 * Permite eliminar registros anteriores a una fecha para mantener la base de datos ligera.
 */
@Service
public class DepuracionService {

    private static final Logger logger = LoggerFactory.getLogger(DepuracionService.class);

    private final AsistenciaRepository asistenciaRepository;
    private final RutinaRepository rutinaRepository;

    public DepuracionService(AsistenciaRepository asistenciaRepository, RutinaRepository rutinaRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.rutinaRepository = rutinaRepository;
    }

    /**
     * Cuenta cuántas asistencias tienen fecha anterior a la indicada.
     * Útil para mostrar al usuario antes de ejecutar la depuración.
     */
    @Transactional(readOnly = true)
    public long contarAsistenciasAntesDe(LocalDate fecha) {
        return asistenciaRepository.countByFechaBefore(fecha);
    }

    /**
     * Elimina todas las asistencias con fecha anterior a la indicada.
     * Ejemplo: si fecha = 2025-12-12, se borran todos los registros con fecha &lt; 2025-12-12.
     *
     * @return Cantidad de registros eliminados
     */
    @Transactional
    public int depurarAsistenciasAntesDe(LocalDate fecha) {
        int eliminados = asistenciaRepository.deleteByFechaBefore(fecha);
        logger.info("Depuración asistencias: eliminados {} registros con fecha anterior a {}", eliminados, fecha);
        return eliminados;
    }

    /**
     * Cuenta cuántas rutinas asignadas (no plantillas) fueron creadas antes de la fecha indicada.
     */
    @Transactional(readOnly = true)
    public long contarRutinasAsignadasAntesDe(LocalDate fecha) {
        LocalDateTime limite = fecha.atStartOfDay();
        return rutinaRepository.findByEsPlantillaFalseAndFechaCreacionBefore(limite).size();
    }

    /**
     * Elimina todas las rutinas asignadas a alumnos cuya fecha de creación es anterior a la indicada.
     * Se eliminan en cascada sus series y serieEjercicios.
     *
     * @return Cantidad de rutinas eliminadas
     */
    @Transactional
    public int depurarRutinasAsignadasAntesDe(LocalDate fecha) {
        LocalDateTime limite = fecha.atStartOfDay();
        List<Rutina> aEliminar = rutinaRepository.findByEsPlantillaFalseAndFechaCreacionBefore(limite);
        int count = aEliminar.size();
        for (Rutina r : aEliminar) {
            rutinaRepository.delete(r);
        }
        logger.info("Depuración rutinas asignadas: eliminadas {} rutinas creadas antes de {}", count, fecha);
        return count;
    }
}
