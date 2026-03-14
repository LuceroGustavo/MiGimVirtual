package com.mattfuncional.servicios;

import com.mattfuncional.dto.SerieDTO;
import com.mattfuncional.entidades.*;
import com.mattfuncional.repositorios.*;
import com.mattfuncional.excepciones.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SerieService {

    private final SerieRepository serieRepository;
    private final ExerciseRepository exerciseRepository;
    private final ProfesorRepository profesorRepository;
    private final RutinaRepository rutinaRepository;
    private final SerieEjercicioRepository serieEjercicioRepository;

    public SerieService(SerieRepository serieRepository,
            ExerciseRepository exerciseRepository,
            ProfesorRepository profesorRepository,
            RutinaRepository rutinaRepository,
            SerieEjercicioRepository serieEjercicioRepository) {
        this.serieRepository = serieRepository;
        this.exerciseRepository = exerciseRepository;
        this.profesorRepository = profesorRepository;
        this.rutinaRepository = rutinaRepository;
        this.serieEjercicioRepository = serieEjercicioRepository;
    }

    // Crear una nueva serie plantilla a partir de un DTO
    public Serie crearSeriePlantilla(SerieDTO serieDTO) {
        // 1. Validar que el profesor exista
        Profesor profesor = profesorRepository.findById(serieDTO.getProfesorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Profesor no encontrado con id: " + serieDTO.getProfesorId()));

        // 2. Crear la entidad Serie
        Serie nuevaSerie = new Serie();
        nuevaSerie.setNombre(serieDTO.getNombre());
        nuevaSerie.setDescripcion(serieDTO.getDescripcion());
        nuevaSerie.setProfesor(profesor);
        nuevaSerie.setEsPlantilla(true);
        nuevaSerie.setCreador("ADMIN");
        nuevaSerie.setRepeticionesSerie(serieDTO.getRepeticionesSerie());

        // 3. Crear y asociar las entidades SerieEjercicio (con orden según posición en la lista)
        if (serieDTO.getEjercicios() != null) {
            for (int i = 0; i < serieDTO.getEjercicios().size(); i++) {
                SerieDTO.EjercicioSerieDTO ejercicioDTO = serieDTO.getEjercicios().get(i);
                Exercise ejercicio = exerciseRepository.findById(ejercicioDTO.getEjercicioId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Ejercicio no encontrado con id: " + ejercicioDTO.getEjercicioId()));

                SerieEjercicio serieEjercicio = new SerieEjercicio();
                serieEjercicio.setSerie(nuevaSerie);
                serieEjercicio.setExercise(ejercicio);
                serieEjercicio.setValor(ejercicioDTO.getValor() != null ? ejercicioDTO.getValor() : 1);
                serieEjercicio.setUnidad(ejercicioDTO.getUnidad() != null ? ejercicioDTO.getUnidad() : "reps");
                serieEjercicio.setPeso(ejercicioDTO.getPeso());
                serieEjercicio.setOrden(i);

                nuevaSerie.getSerieEjercicios().add(serieEjercicio);
            }
        }

        // 4. Guardar la serie (y los SerieEjercicio en cascada)
        return serieRepository.save(nuevaSerie);
    }

    // Obtener todas las series
    public List<Serie> obtenerTodasLasSeries() {
        return serieRepository.findAll();
    }

    // Obtener serie por ID
    public Serie obtenerSeriePorId(Long id) {
        return serieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serie no encontrada con id: " + id));
    }

    /** Obtiene una serie con sus ejercicios cargados y ordenados por orden (para edición en formulario). */
    public Serie obtenerSeriePorIdConEjercicios(Long id) {
        Serie serie = serieRepository.findByIdWithSerieEjercicios(id)
                .orElseThrow(() -> new ResourceNotFoundException("Serie no encontrada con id: " + id));
        if (serie.getSerieEjercicios() != null) {
            serie.getSerieEjercicios().sort(Comparator.comparingInt(se -> se.getOrden() != null ? se.getOrden() : 0));
        }
        return serie;
    }

    // Buscar series por profesor
    public List<Serie> findByProfesorId(Long profesorId) {
        return serieRepository.findByProfesorId(profesorId);
    }

    // Obtener series por rutina ordenadas
    public List<Serie> obtenerSeriesPorRutina(Long rutinaId) {
        return serieRepository.findByRutinaIdOrderByOrdenAsc(rutinaId);
    }

    // Eliminar serie
    public void eliminarSerie(Long id) {
        serieRepository.deleteById(id);
    }

    // Obtener series plantilla por profesor
    public List<Serie> obtenerSeriesPlantillaPorProfesor(Long profesorId) {
        return serieRepository.findByProfesorIdAndEsPlantillaTrue(profesorId);
    }

    @Transactional
    public Serie copiarSerieParaNuevaRutina(Serie serieOriginal, Rutina nuevaRutina, int orden) {
        Serie nuevaSerie = new Serie();
        nuevaSerie.setNombre(serieOriginal.getNombre());
        nuevaSerie.setDescripcion(serieOriginal.getDescripcion());
        nuevaSerie.setProfesor(serieOriginal.getProfesor());
        nuevaSerie.setRutina(nuevaRutina);
        nuevaSerie.setEsPlantilla(false); // La copia ya no es una plantilla
        nuevaSerie.setCreador(serieOriginal.getCreador());
        nuevaSerie.setRepeticionesSerie(serieOriginal.getRepeticionesSerie()); // Copiar repeticiones
        nuevaSerie.setPlantillaId(serieOriginal.getId()); // Guardar referencia a la plantilla original
        nuevaSerie.setOrden(orden);

        // Guardamos la nueva serie para obtener un ID
        Serie serieGuardada = serieRepository.save(nuevaSerie);

        // Copiamos las relaciones SerieEjercicio (conservando orden)
        List<SerieEjercicio> nuevosSerieEjercicios = new ArrayList<>();
        List<SerieEjercicio> originalOrdenados = new ArrayList<>(serieOriginal.getSerieEjercicios());
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
            nuevosSerieEjercicios.add(serieEjercicioRepository.save(nuevoSe));
        }

        serieGuardada.setSerieEjercicios(nuevosSerieEjercicios);

        return serieRepository.save(serieGuardada);
    }

    // Método para convertir una entidad Serie a su DTO correspondiente
    public SerieDTO convertirSerieADTO(Serie serie) {
        SerieDTO dto = new SerieDTO();
        dto.setId(serie.getId());
        dto.setNombre(serie.getNombre());
        dto.setDescripcion(serie.getDescripcion());
        dto.setRepeticionesSerie(serie.getRepeticionesSerie());
        if (serie.getProfesor() != null) {
            dto.setProfesorId(serie.getProfesor().getId());
        }

        List<SerieEjercicio> ordenados = new ArrayList<>(serie.getSerieEjercicios());
        ordenados.sort(Comparator.comparingInt(se -> se.getOrden() != null ? se.getOrden() : 0));
        List<SerieDTO.EjercicioSerieDTO> ejerciciosDTO = ordenados.stream()
                .map(se -> {
                    SerieDTO.EjercicioSerieDTO ejDTO = new SerieDTO.EjercicioSerieDTO();
                    ejDTO.setEjercicioId(se.getExercise().getId());
                    ejDTO.setValor(se.getValor());
                    ejDTO.setUnidad(se.getUnidad());
                    ejDTO.setPeso(se.getPeso());
                    return ejDTO;
                }).collect(Collectors.toList());

        dto.setEjercicios(ejerciciosDTO);
        return dto;
    }

    // Actualizar una serie plantilla existente
    @Transactional
    public Serie actualizarSeriePlantilla(Long serieId, SerieDTO serieDTO) {
        // 1. Obtener la serie existente
        Serie serie = serieRepository.findById(serieId)
                .orElseThrow(() -> new ResourceNotFoundException("Serie no encontrada con id: " + serieId));

        // 2. Actualizar los campos simples
        serie.setNombre(serieDTO.getNombre());
        serie.setDescripcion(serieDTO.getDescripcion());
        serie.setRepeticionesSerie(serieDTO.getRepeticionesSerie());

        // 3. Actualizar los ejercicios (estrategia: borrar y recrear)
        serieEjercicioRepository.deleteBySerieId(serieId);
        serie.getSerieEjercicios().clear(); // Limpiar la colección en la entidad

        // 4. Crear y asociar las nuevas entidades SerieEjercicio (con orden según posición)
        if (serieDTO.getEjercicios() != null) {
            for (int i = 0; i < serieDTO.getEjercicios().size(); i++) {
                SerieDTO.EjercicioSerieDTO ejercicioDTO = serieDTO.getEjercicios().get(i);
                Exercise ejercicio = exerciseRepository.findById(ejercicioDTO.getEjercicioId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Ejercicio no encontrado con id: " + ejercicioDTO.getEjercicioId()));

                SerieEjercicio serieEjercicio = new SerieEjercicio();
                serieEjercicio.setSerie(serie);
                serieEjercicio.setExercise(ejercicio);
                serieEjercicio.setValor(ejercicioDTO.getValor() != null ? ejercicioDTO.getValor() : 1);
                serieEjercicio.setUnidad(ejercicioDTO.getUnidad() != null ? ejercicioDTO.getUnidad() : "reps");
                serieEjercicio.setPeso(ejercicioDTO.getPeso());
                serieEjercicio.setOrden(i);

                serie.getSerieEjercicios().add(serieEjercicio);
            }
        }

        // 5. Guardar la serie actualizada
        return serieRepository.save(serie);
    }

    // --- MÉTODO DE LIMPIEZA DE DATOS EXISTENTES ---
    @Transactional
    public int corregirSerieEjerciciosNulos() {
        int corregidos = 0;
        List<SerieEjercicio> todos = serieEjercicioRepository.findAll();
        for (SerieEjercicio se : todos) {
            boolean modificado = false;
            if (se.getValor() == null) {
                se.setValor(1);
                modificado = true;
            }
            if (se.getUnidad() == null) {
                se.setUnidad("reps");
                modificado = true;
            }
            if (modificado) {
                serieEjercicioRepository.save(se);
                corregidos++;
            }
        }
        return corregidos;
    }
}