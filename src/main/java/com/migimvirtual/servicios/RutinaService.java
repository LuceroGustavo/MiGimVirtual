package com.migimvirtual.servicios;

import com.migimvirtual.dto.EstadisticasRutinaDTO;
import com.migimvirtual.entidades.Rutina;
import com.migimvirtual.entidades.Usuario;
import com.migimvirtual.entidades.Profesor;
import com.migimvirtual.entidades.Serie;
import com.migimvirtual.entidades.SerieEjercicio;
import com.migimvirtual.excepciones.ResourceNotFoundException;
import com.migimvirtual.repositorios.RutinaRepository;
import com.migimvirtual.repositorios.UsuarioRepository;
import com.migimvirtual.repositorios.ProfesorRepository;
import com.migimvirtual.repositorios.SerieRepository;
import com.migimvirtual.repositorios.SerieEjercicioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.security.SecureRandom;

@Service
@Transactional
public class RutinaService {

    private final RutinaRepository rutinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProfesorRepository profesorRepository;
    private final SerieRepository serieRepository;
    private final SerieEjercicioRepository serieEjercicioRepository;
    private final SerieService serieService;
    private static final SecureRandom TOKEN_RANDOM = new SecureRandom();
    private static final char[] TOKEN_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

    public RutinaService(RutinaRepository rutinaRepository,
            UsuarioRepository usuarioRepository,
            ProfesorRepository profesorRepository,
            SerieRepository serieRepository,
            SerieEjercicioRepository serieEjercicioRepository,
            SerieService serieService) {
        this.rutinaRepository = rutinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.profesorRepository = profesorRepository;
        this.serieRepository = serieRepository;
        this.serieEjercicioRepository = serieEjercicioRepository;
        this.serieService = serieService;
    }

    // Crear rutina básica
    public Rutina crearRutina(Long usuarioId, Long profesorId, String nombre, String descripcion) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("Profesor no encontrado con id: " + profesorId));

        Rutina rutina = new Rutina(nombre, descripcion, usuario, profesor);
        rutina.setTokenPublico(generarTokenUnico());
        return rutinaRepository.save(rutina);
    }

    // Obtener todas las rutinas
    public List<Rutina> obtenerTodasLasRutinas() {
        return rutinaRepository.findAll();
    }

    // Obtener rutina por ID
    public Rutina obtenerRutinaPorId(Long id) {
        Rutina rutina = rutinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada con id: " + id));
        if (rutina.getTokenPublico() == null || rutina.getTokenPublico().isBlank()) {
            rutina.setTokenPublico(generarTokenUnico());
            rutina = rutinaRepository.save(rutina);
        }
        return rutina;
    }

    /** Obtiene una rutina con sus series cargadas y ordenadas por orden (para el formulario de edición). */
    public Rutina obtenerRutinaPorIdConSeries(Long id) {
        Rutina rutina = rutinaRepository.findByIdWithSeries(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada con id: " + id));
        if (rutina.getTokenPublico() == null || rutina.getTokenPublico().isBlank()) {
            rutina.setTokenPublico(generarTokenUnico());
            rutina = rutinaRepository.save(rutina);
        }
        if (rutina.getSeries() != null) {
            rutina.getSeries().sort(Comparator.comparingInt(Serie::getOrden));
        }
        return rutina;
    }

    /** Carga la rutina por token con series, serieEjercicios y exercise (igual que en creación/ver serie) para la hoja pública. */
    public Rutina obtenerRutinaPorToken(String tokenPublico) {
        Rutina rutina = rutinaRepository.findByTokenPublicoWithSeries(tokenPublico)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada con token: " + tokenPublico));
        cargarSeriesConEjercicios(rutina);
        return rutina;
    }

    /** Carga la rutina por ID con series y serieEjercicios para vista privada (panel profesor, requiere sesión). */
    public Rutina obtenerRutinaPorIdParaVista(Long id) {
        Rutina rutina = rutinaRepository.findByIdWithSeries(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rutina no encontrada con id: " + id));
        cargarSeriesConEjercicios(rutina);
        return rutina;
    }

    private void cargarSeriesConEjercicios(Rutina rutina) {
        if (rutina.getSeries() != null && !rutina.getSeries().isEmpty()) {
            List<Long> seriesIds = rutina.getSeries().stream().map(Serie::getId).toList();
            List<Serie> seriesConEjercicios = serieRepository.findByIdInWithSerieEjercicios(seriesIds);
            seriesConEjercicios.forEach(s -> s.setRutina(rutina));
            rutina.getSeries().clear();
            rutina.getSeries().addAll(seriesConEjercicios);
            rutina.getSeries().sort(Comparator.comparingInt(Serie::getOrden));
            for (Serie s : rutina.getSeries()) {
                if (s.getSerieEjercicios() != null) {
                    s.getSerieEjercicios().sort(Comparator.comparingInt(se -> se.getOrden() != null ? se.getOrden() : 0));
                }
            }
        }
    }

    // Obtener rutinas por usuario
    public List<Rutina> obtenerRutinasPorUsuario(Long usuarioId) {
        return rutinaRepository.findByUsuarioId(usuarioId);
    }

    // Obtener rutinas asignadas por usuario (no plantillas)
    public List<Rutina> obtenerRutinasAsignadasPorUsuario(Long usuarioId) {
        return rutinaRepository.findByUsuarioIdAndEsPlantillaFalse(usuarioId);
    }

    // Obtener rutinas por profesor
    public List<Rutina> obtenerRutinasPorProfesor(Long profesorId) {
        return rutinaRepository.findByProfesorId(profesorId);
    }

    // Actualizar rutina
    public Rutina actualizarRutina(Long id, Rutina rutinaActualizada) {
        Rutina rutina = obtenerRutinaPorId(id);

        rutina.setNombre(rutinaActualizada.getNombre());
        rutina.setDescripcion(rutinaActualizada.getDescripcion());
        rutina.setEstado(rutinaActualizada.getEstado());

        return rutinaRepository.save(rutina);
    }

    // Eliminar rutina
    public void eliminarRutina(Long id) {
        Rutina rutina = obtenerRutinaPorId(id);
        rutinaRepository.delete(rutina);
    }

    // Cambiar estado de rutina
    public Rutina cambiarEstadoRutina(Long id, String nuevoEstado) {
        Rutina rutina = obtenerRutinaPorId(id);
        rutina.setEstado(nuevoEstado);
        return rutinaRepository.save(rutina);
    }

    // Asignar rutina a usuario
    public Rutina asignarRutinaAUsuario(Long rutinaId, Long usuarioId) {
        Rutina rutina = obtenerRutinaPorId(rutinaId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

        rutina.setUsuario(usuario);
        return rutinaRepository.save(rutina);
    }

    // Obtener rutinas activas por usuario
    public List<Rutina> obtenerRutinasActivasPorUsuario(Long usuarioId) {
        return rutinaRepository.findByUsuarioIdAndEstado(usuarioId, "ACTIVA");
    }

    // Obtener rutinas completadas por usuario
    public List<Rutina> obtenerRutinasCompletadasPorUsuario(Long usuarioId) {
        return rutinaRepository.findByUsuarioIdAndEstado(usuarioId, "COMPLETADA");
    }

    /** Inactiva todas las rutinas asignadas a un alumno (usuarioId). Devuelve la cantidad inactivadas. */
    public int inactivarTodasRutinasDelAlumno(Long usuarioId) {
        List<Rutina> rutinas = rutinaRepository.findByUsuarioIdAndEsPlantillaFalse(usuarioId);
        int count = 0;
        for (Rutina r : rutinas) {
            if (r.getEstado() == null || !"INACTIVA".equalsIgnoreCase(r.getEstado().trim())) {
                r.setEstado("INACTIVA");
                rutinaRepository.save(r);
                count++;
            }
        }
        return count;
    }

    // Marcar rutina como completada
    public Rutina marcarRutinaComoCompletada(Long id) {
        return cambiarEstadoRutina(id, "COMPLETADA");
    }

    // Obtener estadísticas de rutinas por usuario
    public EstadisticasRutinaDTO obtenerEstadisticasRutinas(Long usuarioId) {
        List<Rutina> todasLasRutinas = obtenerRutinasPorUsuario(usuarioId);
        List<Rutina> rutinasActivas = obtenerRutinasActivasPorUsuario(usuarioId);
        List<Rutina> rutinasCompletadas = obtenerRutinasCompletadasPorUsuario(usuarioId);

        return new EstadisticasRutinaDTO(todasLasRutinas.size(), rutinasActivas.size(), rutinasCompletadas.size());
    }

    // Crear rutina plantilla
    public Rutina crearRutinaPlantilla(Long profesorId, String nombre, String descripcion, String categoria) {
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("Profesor no encontrado con id: " + profesorId));

        Rutina rutina = new Rutina(nombre, descripcion, profesor);
        rutina.setCategoria(categoria);
        rutina.setCreador("ADMIN");
        rutina.setTokenPublico(generarTokenUnico());
        return rutinaRepository.save(rutina);
    }

    // Obtener rutinas plantilla por profesor
    public List<Rutina> obtenerRutinasPlantillaPorProfesor(Long profesorId) {
        return rutinaRepository.findByProfesorIdAndEsPlantillaTrue(profesorId);
    }

    /** Rutinas plantilla del profesor con series cargadas (para dashboard y modal detalle móvil). */
    public List<Rutina> obtenerRutinasPlantillaPorProfesorWithSeries(Long profesorId) {
        return rutinaRepository.findByProfesorIdAndEsPlantillaTrueWithSeries(profesorId);
    }

    // Obtener todas las rutinas plantilla
    public List<Rutina> obtenerTodasLasRutinasPlantilla() {
        return rutinaRepository.findByEsPlantillaTrue();
    }

    // Buscar rutinas plantilla por categoría
    public List<Rutina> buscarRutinasPlantillaPorCategoria(Long profesorId, String categoria) {
        return rutinaRepository.findByProfesorIdAndEsPlantillaTrueAndCategoriaContaining(profesorId, categoria);
    }

    // Buscar rutinas plantilla por nombre
    public List<Rutina> buscarRutinasPlantillaPorNombre(Long profesorId, String nombre) {
        return rutinaRepository.findByProfesorIdAndEsPlantillaTrueAndNombreContainingIgnoreCase(profesorId, nombre);
    }

    // Asignar rutina plantilla a un usuario (crear copia)
    public Rutina asignarRutinaPlantillaAUsuario(Long rutinaPlantillaId, Long usuarioId, Long profesorId, String notaParaAlumno) {
        Rutina rutinaPlantilla = obtenerRutinaPorId(rutinaPlantillaId);
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new ResourceNotFoundException("Profesor no encontrado con id: " + profesorId));

        // Crear una copia de la rutina plantilla
        Rutina nuevaRutina = new Rutina();
        nuevaRutina.setNombre(rutinaPlantilla.getNombre());
        nuevaRutina.setDescripcion(rutinaPlantilla.getDescripcion());
        nuevaRutina.setCategoria(rutinaPlantilla.getCategoria());
        nuevaRutina.setUsuario(usuario);
        nuevaRutina.setProfesor(profesor);
        nuevaRutina.setEsPlantilla(false); // No es plantilla, es asignada
        nuevaRutina.setEstado("ACTIVA");
        nuevaRutina.setTokenPublico(generarTokenUnico());
        nuevaRutina.setNotaParaAlumno(notaParaAlumno != null && !notaParaAlumno.isBlank() ? notaParaAlumno.trim() : null);

        Rutina rutinaGuardada = rutinaRepository.save(nuevaRutina);

        // Copiar las series de la plantilla a la nueva rutina en orden
        if (rutinaPlantilla.getSeries() != null) {
            List<Serie> seriesOrdenadas = serieRepository.findByRutinaIdOrderByOrdenAsc(rutinaPlantilla.getId());
            for (int i = 0; i < seriesOrdenadas.size(); i++) {
                serieService.copiarSerieParaNuevaRutina(seriesOrdenadas.get(i), rutinaGuardada, i);
            }
        }

        return rutinaGuardada;
    }

    // Duplicar rutina plantilla (crear nueva plantilla basada en otra)
    public Rutina duplicarRutinaPlantilla(Long rutinaPlantillaId, String nuevoNombre) {
        Rutina rutinaOriginal = obtenerRutinaPorId(rutinaPlantillaId);

        Rutina nuevaRutina = new Rutina();
        nuevaRutina.setNombre(nuevoNombre);
        nuevaRutina.setDescripcion(rutinaOriginal.getDescripcion());
        nuevaRutina.setCategoria(rutinaOriginal.getCategoria());
        nuevaRutina.setProfesor(rutinaOriginal.getProfesor());
        nuevaRutina.setEsPlantilla(true);
        nuevaRutina.setCreador("ADMIN");
        nuevaRutina.setTokenPublico(generarTokenUnico());

        return rutinaRepository.save(nuevaRutina);
    }

    private String generarTokenUnico() {
        String token;
        do {
            token = generarToken(12);
        } while (rutinaRepository.existsByTokenPublico(token));
        return token;
    }

    private String generarToken(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(TOKEN_CHARS[TOKEN_RANDOM.nextInt(TOKEN_CHARS.length)]);
        }
        return builder.toString();
    }

    // Obtener rutinas asignadas a usuarios por profesor
    public List<Rutina> obtenerRutinasAsignadasPorProfesor(Long profesorId) {
        return rutinaRepository.findByProfesorIdAndEsPlantillaFalse(profesorId);
    }

    // Método para actualizar solo la info básica de una rutina
    public Rutina actualizarInformacionBasicaRutina(Long id, String nombre, String descripcion, String categoria) {
        Rutina rutina = obtenerRutinaPorId(id);
        rutina.setNombre(nombre);
        rutina.setDescripcion(descripcion);
        rutina.setCategoria(categoria);
        return rutinaRepository.save(rutina);
    }

    // Método para actualizar el conjunto de series de una rutina
    @Transactional
    public void actualizarSeriesDeRutina(Long rutinaId, List<Long> seriesIds, List<Integer> repeticionesExistentes,
            List<Long> nuevasSeriesIds, List<Integer> repeticionesNuevas) {
        Rutina rutina = obtenerRutinaPorId(rutinaId);

        // 1. ANTES de borrar: resolver plantillaId y repeticiones de cada serie existente
        //    (una vez borradas, ya no podemos leerlas por ID)
        List<Long> plantillaIdsAReañadir = new ArrayList<>();
        List<Integer> repsAReañadir = new ArrayList<>();
        if (seriesIds != null && repeticionesExistentes != null) {
            for (int i = 0; i < seriesIds.size(); i++) {
                Long serieId = seriesIds.get(i);
                Serie s = serieRepository.findById(serieId).orElse(null);
                if (s != null) {
                    Long plantillaId = s.getPlantillaId() != null ? s.getPlantillaId() : s.getId();
                    plantillaIdsAReañadir.add(plantillaId);
                    repsAReañadir.add(i < repeticionesExistentes.size() && repeticionesExistentes.get(i) != null
                            ? repeticionesExistentes.get(i) : 1);
                }
            }
        }

        // 2. Borrar las series antiguas de la rutina
        for (Serie serie : rutina.getSeries()) {
            serieEjercicioRepository.deleteBySerieId(serie.getId());
        }
        serieRepository.deleteByRutinaId(rutinaId);
        rutina.getSeries().clear();

        // 3. Re-añadir las series existentes (mismo orden) con sus repeticiones actualizadas
        for (int i = 0; i < plantillaIdsAReañadir.size(); i++) {
            agregarSerieARutina(rutinaId, plantillaIdsAReañadir.get(i), repsAReañadir.get(i));
        }

        // 4. Añadir las nuevas series seleccionadas
        if (nuevasSeriesIds != null) {
            for (int i = 0; i < nuevasSeriesIds.size(); i++) {
                Long plantillaId = nuevasSeriesIds.get(i);
                int repeticiones = (repeticionesNuevas != null && i < repeticionesNuevas.size()
                        && repeticionesNuevas.get(i) != null)
                        ? repeticionesNuevas.get(i)
                        : 1;
                agregarSerieARutina(rutinaId, plantillaId, repeticiones);
            }
        }
    }

    /** Actualiza la nota/reseña para el alumno de una rutina asignada. Solo si la rutina pertenece al profesor y no es plantilla. */
    public void actualizarNotaParaAlumno(Long rutinaId, Long profesorId, String nota) {
        Rutina rutina = obtenerRutinaPorId(rutinaId);
        if (rutina.getProfesor() == null || !rutina.getProfesor().getId().equals(profesorId)) {
            throw new ResourceNotFoundException("No tiene permiso para editar esta rutina.");
        }
        if (rutina.isEsPlantilla()) {
            throw new IllegalArgumentException("Solo se puede editar la nota de rutinas asignadas a un alumno.");
        }
        rutina.setNotaParaAlumno(nota != null && !nota.isBlank() ? nota.trim() : null);
        rutinaRepository.save(rutina);
    }

    // Agregar serie a una rutina con repeticiones específicas
    public void agregarSerieARutina(Long rutinaId, Long seriePlantillaId, int repeticiones) {
        Rutina rutina = obtenerRutinaPorId(rutinaId);
        Serie seriePlantilla = serieService.obtenerSeriePorId(seriePlantillaId);

        // Crear una copia de la serie plantilla para la rutina
        int orden = (int) serieRepository.countByRutinaId(rutinaId);
        Serie nuevaSerie = new Serie();
        nuevaSerie.setNombre(seriePlantilla.getNombre());
        nuevaSerie.setDescripcion(seriePlantilla.getDescripcion());
        nuevaSerie.setProfesor(seriePlantilla.getProfesor());
        nuevaSerie.setRutina(rutina);
        nuevaSerie.setEsPlantilla(false); // No es plantilla, es asignada
        nuevaSerie.setRepeticionesSerie(repeticiones);
        nuevaSerie.setCreador(seriePlantilla.getCreador());
        nuevaSerie.setPlantillaId(seriePlantilla.getId()); // Guardar referencia a la plantilla original
        nuevaSerie.setOrden(orden);

        // Guardar la nueva serie
        Serie serieGuardada = serieRepository.save(nuevaSerie);

        // Copiar los ejercicios de la serie plantilla (incluyendo peso y orden)
        if (seriePlantilla.getSerieEjercicios() != null) {
            List<SerieEjercicio> originalOrdenados = new ArrayList<>(seriePlantilla.getSerieEjercicios());
            originalOrdenados.sort(Comparator.comparingInt(se -> se.getOrden() != null ? se.getOrden() : 0));
            for (int i = 0; i < originalOrdenados.size(); i++) {
                SerieEjercicio seOriginal = originalOrdenados.get(i);
                SerieEjercicio nuevoSe = new SerieEjercicio();
                nuevoSe.setSerie(serieGuardada);
                nuevoSe.setExercise(seOriginal.getExercise());
                nuevoSe.setValor(seOriginal.getValor());
                nuevoSe.setUnidad(seOriginal.getUnidad());
                nuevoSe.setPeso(seOriginal.getPeso());
                nuevoSe.setOrden(i);
                serieEjercicioRepository.save(nuevoSe);
            }
        }
    }
}
