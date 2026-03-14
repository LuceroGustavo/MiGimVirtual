package com.mattfuncional.servicios;

import com.mattfuncional.entidades.GrupoMuscular;
import com.mattfuncional.entidades.Profesor;
import com.mattfuncional.repositorios.GrupoMuscularRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GrupoMuscularService {

    private static final String[] NOMBRES_GRUPOS_SISTEMA = {
        "BRAZOS", "PIERNAS", "PECHO", "ESPALDA", "CARDIO", "ELONGACION"
    };

    private final GrupoMuscularRepository grupoMuscularRepository;

    public GrupoMuscularService(GrupoMuscularRepository grupoMuscularRepository) {
        this.grupoMuscularRepository = grupoMuscularRepository;
    }

    /** Grupos disponibles para un profesor: del sistema + los suyos. */
    public List<GrupoMuscular> findDisponiblesParaProfesor(Long profesorId) {
        return grupoMuscularRepository.findDisponiblesParaProfesor(profesorId);
    }

    public List<GrupoMuscular> findByProfesorId(Long profesorId) {
        return grupoMuscularRepository.findByProfesorIdOrderByNombreAsc(profesorId);
    }

    public List<GrupoMuscular> findGruposSistema() {
        return grupoMuscularRepository.findByProfesorIsNullOrderByNombreAsc();
    }

    /** Todos los grupos (sistema + de todos los profesores). Para backup/export. */
    @Transactional(readOnly = true)
    public List<GrupoMuscular> findAll() {
        return grupoMuscularRepository.findAll().stream()
                .sorted((a, b) -> {
                    boolean sa = a.getProfesor() == null;
                    boolean sb = b.getProfesor() == null;
                    if (sa != sb) return sa ? -1 : 1;
                    return (a.getNombre() != null && b.getNombre() != null)
                            ? a.getNombre().compareToIgnoreCase(b.getNombre()) : 0;
                })
                .collect(Collectors.toList());
    }

    public Optional<GrupoMuscular> findById(Long id) {
        return grupoMuscularRepository.findById(id);
    }

    public Optional<GrupoMuscular> findByNombreSistema(String nombre) {
        return grupoMuscularRepository.findFirstByNombreAndProfesorIsNull(nombre);
    }

    /** Convierte una lista de IDs a Set de GrupoMuscular (ignora IDs no encontrados). */
    @Transactional(readOnly = true)
    public Set<GrupoMuscular> resolveGruposByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        Set<GrupoMuscular> result = new HashSet<>();
        for (Long id : ids) {
            if (id != null) {
                grupoMuscularRepository.findById(id).ifPresent(result::add);
            }
        }
        return result;
    }

    /** Resuelve nombres a Set de GrupoMuscular: primero sistema, luego del profesor si se indica. */
    @Transactional(readOnly = true)
    public Set<GrupoMuscular> resolveGruposByNames(List<String> names, Long profesorId) {
        if (names == null || names.isEmpty()) return new HashSet<>();
        Set<GrupoMuscular> result = new HashSet<>();
        for (String nombre : names) {
            if (nombre == null || nombre.isBlank()) continue;
            String n = nombre.trim();
            Optional<GrupoMuscular> gSistema = grupoMuscularRepository.findFirstByNombreAndProfesorIsNull(n);
            if (gSistema.isPresent()) {
                result.add(gSistema.get());
            } else if (profesorId != null) {
                grupoMuscularRepository.findFirstByNombreAndProfesorId(n, profesorId).ifPresent(result::add);
            }
        }
        return result;
    }

    /** Asegura que existan los 6 grupos del sistema. Idempotente. */
    @Transactional
    public void asegurarGruposSistema() {
        for (String nombre : NOMBRES_GRUPOS_SISTEMA) {
            if (grupoMuscularRepository.findFirstByNombreAndProfesorIsNull(nombre).isEmpty()) {
                GrupoMuscular g = new GrupoMuscular(nombre, null);
                grupoMuscularRepository.save(g);
            }
        }
    }

    @Transactional
    public GrupoMuscular guardar(GrupoMuscular grupo) {
        return grupoMuscularRepository.save(grupo);
    }

    /** Asegura que exista un grupo con ese nombre (sistema o del profesor). Idempotente para backup/import. */
    @Transactional
    public GrupoMuscular ensureGrupoExiste(String nombre, boolean esSistema, Profesor profesor) {
        if (nombre == null || nombre.isBlank()) return null;
        String n = nombre.trim();
        if (esSistema) {
            return grupoMuscularRepository.findFirstByNombreAndProfesorIsNull(n)
                    .orElseGet(() -> grupoMuscularRepository.save(new GrupoMuscular(n, null)));
        }
        if (profesor == null) return null;
        return grupoMuscularRepository.findFirstByNombreAndProfesorId(n, profesor.getId())
                .orElseGet(() -> grupoMuscularRepository.save(new GrupoMuscular(n, profesor)));
    }

    @Transactional
    public void eliminar(Long id) {
        grupoMuscularRepository.deleteById(id);
    }

    public boolean existeNombreParaProfesor(String nombre, Long profesorId) {
        if (profesorId == null) {
            return grupoMuscularRepository.findFirstByNombreAndProfesorIsNull(nombre).isPresent();
        }
        return grupoMuscularRepository.findFirstByNombreAndProfesorId(nombre, profesorId).isPresent();
    }

    /** Solo los grupos con profesor no nulo (creados por el profesor) pueden editarse/eliminarse por ese profesor. */
    public boolean puedeSerEditadoPorProfesor(Long grupoId, Long profesorId) {
        if (grupoId == null || profesorId == null) return false;
        return grupoMuscularRepository.findById(grupoId)
                .filter(g -> g.getProfesor() != null && g.getProfesor().getId().equals(profesorId))
                .isPresent();
    }
}
