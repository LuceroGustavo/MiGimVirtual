package com.mattfuncional.servicios;

import com.mattfuncional.entidades.CalendarioExcepcion;
import com.mattfuncional.entidades.Profesor;
import com.mattfuncional.entidades.Usuario;
import com.mattfuncional.repositorios.CalendarioExcepcionRepository;
import com.mattfuncional.repositorios.ProfesorRepository;
import com.mattfuncional.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class CalendarioExcepcionService {

    @Autowired
    private CalendarioExcepcionRepository calendarioExcepcionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    public CalendarioExcepcion crearExcepcion(Long profesorId, Long usuarioId, LocalDate fecha, LocalTime horaInicio,
            LocalTime horaFin, String motivo) {
        if (profesorId == null || usuarioId == null || fecha == null || horaInicio == null || horaFin == null) {
            return null;
        }
        if (calendarioExcepcionRepository.existsByUsuario_IdAndFechaAndHoraInicioAndHoraFin(
                usuarioId, fecha, horaInicio, horaFin)) {
            return null;
        }
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        Profesor profesor = profesorRepository.findById(profesorId).orElse(null);
        if (usuario == null || profesor == null) {
            return null;
        }
        CalendarioExcepcion excepcion = new CalendarioExcepcion(usuario, profesor, fecha, horaInicio, horaFin, motivo);
        return calendarioExcepcionRepository.save(excepcion);
    }

    public List<CalendarioExcepcion> obtenerExcepcionesSemana(Long profesorId, LocalDate desde, LocalDate hasta) {
        if (profesorId == null || desde == null || hasta == null) {
            return java.util.Collections.emptyList();
        }
        return calendarioExcepcionRepository.findByProfesor_IdAndFechaBetween(profesorId, desde, hasta);
    }
}
