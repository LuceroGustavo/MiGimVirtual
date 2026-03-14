package com.mattfuncional.servicios;

import com.mattfuncional.entidades.Asistencia;
import com.mattfuncional.dto.AsistenciaVistaDTO;
import com.mattfuncional.entidades.GrupoMuscular;
import com.mattfuncional.entidades.Usuario;
import com.mattfuncional.repositorios.AsistenciaRepository;
import com.mattfuncional.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AsistenciaService {
    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Asistencia registrarAsistencia(Usuario usuario, LocalDate fecha, boolean presente, String observaciones, Usuario registradoPor) {
        List<Asistencia> existentes = asistenciaRepository.findByUsuarioAndFecha(usuario, fecha);
        if (existentes != null && !existentes.isEmpty()) {
            return null;
        }
        Asistencia asistencia = new Asistencia(fecha, presente, observaciones, usuario);
        asistencia.setRegistradoPor(registradoPor);
        return asistenciaRepository.save(asistencia);
    }

    public Asistencia registrarAsistencia(Usuario usuario, LocalDate fecha, boolean presente, String observaciones) {
        return registrarAsistencia(usuario, fecha, presente, observaciones, null);
    }

    public List<Asistencia> obtenerAsistenciasPorUsuario(Usuario usuario) {
        return asistenciaRepository.findByUsuarioOrderByFechaDesc(usuario);
    }

    public List<Asistencia> obtenerAsistenciasPorUsuarioId(Long usuarioId) {
        if (usuarioId == null) {
            return java.util.Collections.emptyList();
        }
        return asistenciaRepository.findByUsuario_IdOrderByFechaDesc(usuarioId);
    }

    public List<Asistencia> obtenerAsistenciaPorUsuarioYFecha(Usuario usuario, LocalDate fecha) {
        return asistenciaRepository.findByUsuarioAndFecha(usuario, fecha);
    }

    /**
     * Crea o actualiza el registro de progreso/asistencia para un alumno en una fecha.
     * Si ya existe registro, actualiza observaciones y grupos; presente se actualiza solo si viene explícito (no null).
     * Si presenteParam es null al actualizar, se preserva el valor existente para no pisar "presente" al solo agregar progreso.
     */
    @Transactional
    public Asistencia guardarOActualizarProgreso(Usuario alumno, LocalDate fecha, Boolean presenteParam, String observaciones, Set<GrupoMuscular> gruposTrabajados, Usuario registradoPor) {
        List<Asistencia> existentes = asistenciaRepository.findByUsuarioAndFecha(alumno, fecha);
        Asistencia a;
        boolean presenteVal;
        if (existentes != null && !existentes.isEmpty()) {
            a = existentes.get(0);
            presenteVal = presenteParam != null ? presenteParam : a.isPresente();
        } else {
            presenteVal = presenteParam != null ? presenteParam : false;
            a = new Asistencia(fecha, presenteVal, observaciones != null ? observaciones.trim() : null, alumno);
        }
        a.setPresente(presenteVal);
        a.setObservaciones(observaciones != null && !observaciones.isBlank() ? observaciones.trim() : null);
        a.setGruposTrabajados(gruposTrabajados != null ? gruposTrabajados : new java.util.HashSet<>());
        if (registradoPor != null) {
            a.setRegistradoPor(registradoPor);
        }
        return asistenciaRepository.save(a);
    }

    public Asistencia guardarOActualizarProgreso(Usuario alumno, LocalDate fecha, Boolean presenteParam, String observaciones, Set<GrupoMuscular> gruposTrabajados) {
        return guardarOActualizarProgreso(alumno, fecha, presenteParam, observaciones, gruposTrabajados, null);
    }

    /** Sobrecarga con presente primitivo para llamadas que siempre envían valor (p. ej. calendario API). */
    @Transactional
    public Asistencia guardarOActualizarProgreso(Usuario alumno, LocalDate fecha, boolean presente, String observaciones, Set<GrupoMuscular> gruposTrabajados, Usuario registradoPor) {
        return guardarOActualizarProgreso(alumno, fecha, Boolean.valueOf(presente), observaciones, gruposTrabajados, registradoPor);
    }

    public Asistencia guardarOActualizarProgreso(Usuario alumno, LocalDate fecha, boolean presente, String observaciones, Set<GrupoMuscular> gruposTrabajados) {
        return guardarOActualizarProgreso(alumno, fecha, presente, observaciones, gruposTrabajados, null);
    }

    @Transactional(readOnly = true)
    public List<AsistenciaVistaDTO> obtenerAsistenciasVistaParaAlumno(Usuario alumno) {
        if (alumno == null) {
            return java.util.Collections.emptyList();
        }
        List<Asistencia> registros = asistenciaRepository.findByUsuario_IdOrderByFechaDesc(alumno.getId());
        Map<LocalDate, AsistenciaVistaDTO> map = new HashMap<>();

        // Pendientes: días del mes actual según horarios del alumno (hasta hoy)
        java.util.Set<DayOfWeek> dias = new java.util.HashSet<>();
        if (alumno.getDiasHorariosAsistencia() != null) {
            for (com.mattfuncional.entidades.DiaHorarioAsistencia dh : alumno.getDiasHorariosAsistencia()) {
                if (dh != null && dh.getDia() != null) {
                    dias.add(mapearDiaSemana(dh.getDia()));
                }
            }
        }
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);
        for (LocalDate d = inicioMes; !d.isAfter(hoy); d = d.plusDays(1)) {
            if (dias.contains(d.getDayOfWeek())) {
                AsistenciaVistaDTO dto = new AsistenciaVistaDTO();
                dto.setFecha(d);
                dto.setPendiente(true);
                map.put(d, dto);
            }
        }

        // Registros existentes (incluye excepciones fuera del horario)
        java.time.format.DateTimeFormatter fmtShow = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Asistencia a : registros) {
            if (a == null || a.getFecha() == null) continue;
            AsistenciaVistaDTO dto = map.getOrDefault(a.getFecha(), new AsistenciaVistaDTO());
            dto.setId(a.getId());
            dto.setFecha(a.getFecha());
            dto.setFechaFormateada(a.getFecha().format(fmtShow));
            dto.setPresente(a.isPresente());
            dto.setPendiente(false);
            dto.setObservaciones(a.getObservaciones() != null ? a.getObservaciones() : "");
            java.util.List<String> grupos = new java.util.ArrayList<>();
            if (a.getGruposTrabajados() != null) {
                for (GrupoMuscular g : a.getGruposTrabajados()) {
                    if (g != null && g.getNombre() != null) grupos.add(g.getNombre());
                }
            }
            dto.setGruposTrabajados(grupos);
            if (a.getRegistradoPor() != null) {
                dto.setRegistradoPorId(a.getRegistradoPor().getId());
                dto.setRegistradoPorNombre(a.getRegistradoPor().getNombre());
            }
            map.put(a.getFecha(), dto);
        }

        java.util.List<AsistenciaVistaDTO> out = new java.util.ArrayList<>(map.values());
        out.sort((a, b) -> {
            LocalDate fa = a.getFecha();
            LocalDate fb = b.getFecha();
            if (fa == null && fb == null) return 0;
            if (fa == null) return 1;
            if (fb == null) return -1;
            return fb.compareTo(fa);
        });
        return out;
    }

    private DayOfWeek mapearDiaSemana(com.mattfuncional.enums.DiaSemana dia) {
        if (dia == null) return null;
        switch (dia) {
            case LUNES:
                return DayOfWeek.MONDAY;
            case MARTES:
                return DayOfWeek.TUESDAY;
            case MIERCOLES:
                return DayOfWeek.WEDNESDAY;
            case JUEVES:
                return DayOfWeek.THURSDAY;
            case VIERNES:
                return DayOfWeek.FRIDAY;
            case SABADO:
                return DayOfWeek.SATURDAY;
            case DOMINGO:
                return DayOfWeek.SUNDAY;
            default:
                return null;
        }
    }

    public boolean eliminarAsistenciaDeHoy(Usuario usuario) {
        return eliminarRegistroAsistencia(usuario, LocalDate.now());
    }

    @Transactional
    public void actualizarRegistradoPor(Long asistenciaId, Long registradoPorId) {
        Asistencia asistencia = asistenciaRepository.findById(asistenciaId)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));
        Usuario registradoPor = usuarioRepository.findById(registradoPorId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        asistencia.setRegistradoPor(registradoPor);
        asistenciaRepository.save(asistencia);
    }

    /**
     * Elimina el registro de asistencia para (usuario, fecha). Deja el estado como "pendiente" (sin registro).
     * Útil cuando el profesor quiere marcar explícitamente pendiente (ej. feriado, no hubo clase).
     */
    @Transactional
    public boolean eliminarRegistroAsistencia(Usuario usuario, LocalDate fecha) {
        if (usuario == null || fecha == null) return false;
        List<Asistencia> existentes = asistenciaRepository.findByUsuarioAndFecha(usuario, fecha);
        if (existentes != null && !existentes.isEmpty()) {
            asistenciaRepository.deleteAll(existentes);
            return true;
        }
        return true; // ya no había registro
    }

    /**
     * Registra ausente para (usuario, fecha) solo si aún no existe ningún registro.
     * No sobrescribe registros existentes (presente o ausente).
     */
    @Transactional
    public void registrarAusenteSiNoExiste(Usuario usuario, LocalDate fecha) {
        List<Asistencia> existentes = asistenciaRepository.findByUsuarioAndFecha(usuario, fecha);
        if (existentes == null || existentes.isEmpty()) {
            Asistencia a = new Asistencia(fecha, false, null, usuario);
            asistenciaRepository.save(a);
        }
    }

    /**
     * Devuelve un mapa clave "usuarioId_fecha" (ej. "5_2026-02-17") -> presente (true/false)
     * para todas las asistencias en el rango [inicio, fin]. Útil para pintar el calendario.
     * Usa query con JOIN FETCH de usuario para que el mapa se construya correctamente al volver a entrar.
     */
    @Transactional(readOnly = true)
    public Map<String, Boolean> getMapaPresentePorUsuarioYFecha(LocalDate inicio, LocalDate fin) {
        List<Asistencia> list = asistenciaRepository.findByFechaBetweenWithUsuario(inicio, fin);
        Map<String, Boolean> out = new HashMap<>();
        if (list != null) {
            for (Asistencia a : list) {
                if (a.getUsuario() != null && a.getUsuario().getId() != null && a.getFecha() != null) {
                    out.put(String.valueOf(a.getUsuario().getId()) + "_" + a.getFecha().toString(), a.isPresente());
                }
            }
        }
        return out;
    }
} 