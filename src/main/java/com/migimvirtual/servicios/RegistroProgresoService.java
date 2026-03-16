package com.migimvirtual.servicios;

import com.migimvirtual.entidades.RegistroProgreso;
import com.migimvirtual.entidades.Usuario;
import com.migimvirtual.repositorios.RegistroProgresoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class RegistroProgresoService {

    private final RegistroProgresoRepository registroProgresoRepository;

    public RegistroProgresoService(RegistroProgresoRepository registroProgresoRepository) {
        this.registroProgresoRepository = registroProgresoRepository;
    }

    @Transactional(readOnly = true)
    public List<RegistroProgreso> obtenerRegistrosPorAlumno(Long alumnoId) {
        return registroProgresoRepository.findByUsuario_IdOrderByFechaDescIdDesc(alumnoId);
    }

    @Transactional(readOnly = true)
    public RegistroProgreso obtenerUltimoRegistro(Long alumnoId) {
        List<RegistroProgreso> lista = registroProgresoRepository.findByUsuario_IdOrderByFechaDescIdDesc(alumnoId);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Transactional
    public RegistroProgreso guardar(Long alumnoId, Usuario alumno, LocalDate fecha, String gruposMusculares, String observaciones) {
        RegistroProgreso r = new RegistroProgreso();
        r.setUsuario(alumno);
        r.setFecha(fecha != null ? fecha : LocalDate.now());
        r.setGruposMusculares(gruposMusculares != null && !gruposMusculares.trim().isEmpty() ? gruposMusculares.trim() : null);
        r.setObservaciones(observaciones != null && !observaciones.trim().isEmpty() ? observaciones.trim() : null);
        return registroProgresoRepository.save(r);
    }

    @Transactional
    public RegistroProgreso actualizar(Long registroId, Long alumnoId, Usuario alumno, LocalDate fecha, String gruposMusculares, String observaciones) {
        RegistroProgreso r = registroProgresoRepository.findById(registroId).orElseThrow(() -> new RuntimeException("Registro no encontrado"));
        if (!r.getUsuario().getId().equals(alumnoId)) {
            throw new RuntimeException("El registro no pertenece a este alumno");
        }
        r.setFecha(fecha != null ? fecha : LocalDate.now());
        r.setGruposMusculares(gruposMusculares != null && !gruposMusculares.trim().isEmpty() ? gruposMusculares.trim() : null);
        r.setObservaciones(observaciones != null && !observaciones.trim().isEmpty() ? observaciones.trim() : null);
        return registroProgresoRepository.save(r);
    }

    @Transactional
    public void eliminar(Long registroId, Long alumnoId) {
        RegistroProgreso r = registroProgresoRepository.findById(registroId).orElseThrow(() -> new RuntimeException("Registro no encontrado"));
        if (!r.getUsuario().getId().equals(alumnoId)) {
            throw new RuntimeException("El registro no pertenece a este alumno");
        }
        registroProgresoRepository.delete(r);
    }

    /** Formatea el último registro para mostrar en la tarjeta (fecha, grupos, observaciones). */
    public String formatearUltimoRegistro(RegistroProgreso r) {
        if (r == null) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(r.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        if (r.getGruposMusculares() != null && !r.getGruposMusculares().isEmpty()) {
            sb.append(" — ").append(r.getGruposMusculares());
        }
        if (r.getObservaciones() != null && !r.getObservaciones().isEmpty()) {
            sb.append(": ").append(r.getObservaciones().length() > 80 ? r.getObservaciones().substring(0, 80) + "…" : r.getObservaciones());
        }
        return sb.toString();
    }

    public LocalDate parseFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) return LocalDate.now();
        try {
            return LocalDate.parse(fechaStr.trim());
        } catch (DateTimeParseException e) {
            return LocalDate.now();
        }
    }
}
