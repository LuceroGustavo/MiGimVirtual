package com.migimvirtual.servicios;

import com.migimvirtual.entidades.Rutina;
import com.migimvirtual.repositorios.RutinaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para depuración de datos antiguos: rutinas asignadas.
 * Permite eliminar registros anteriores a una fecha para mantener la base de datos ligera.
 */
@Service
public class DepuracionService {

    private static final Logger logger = LoggerFactory.getLogger(DepuracionService.class);

    private final RutinaRepository rutinaRepository;

    public DepuracionService(RutinaRepository rutinaRepository) {
        this.rutinaRepository = rutinaRepository;
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
