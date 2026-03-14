package com.mattfuncional.servicios;

import com.mattfuncional.entidades.Exercise;
import com.mattfuncional.entidades.Imagen;
import com.mattfuncional.entidades.Profesor;
import com.mattfuncional.repositorios.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mattfuncional.servicios.ImagenServicio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ExerciseAsignacionService {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseAsignacionService.class);
    
    private final ExerciseRepository exerciseRepository;
    private final ProfesorService profesorService;
    private final ImagenServicio imagenServicio;
    
    // Tamaño del lote para procesamiento optimizado
    private static final int TAMANO_LOTE = 15;

    public ExerciseAsignacionService(ExerciseRepository exerciseRepository, 
                                   ProfesorService profesorService,
                                   ImagenServicio imagenServicio) {
        this.exerciseRepository = exerciseRepository;
        this.profesorService = profesorService;
        this.imagenServicio = imagenServicio;
    }

    /**
     * DEPRECADO: Los ejercicios predeterminados ahora son compartidos (profesor = null)
     * Los profesores ya tienen acceso automático a todos los ejercicios predeterminados
     * Este método retorna 0 porque no es necesario copiar ejercicios
     * 
     * @param profesorId ID del profesor
     * @return Siempre retorna 0 (los ejercicios predeterminados ya están disponibles)
     */
    @Deprecated
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int asignarEjerciciosPredefinidosAProfesorOptimizado(Long profesorId) {
        logger.warn("=== MÉTODO DEPRECADO: asignarEjerciciosPredefinidosAProfesorOptimizado ===");
        logger.warn("Los ejercicios predeterminados ahora son compartidos (profesor = null)");
        logger.warn("Los profesores ya tienen acceso automático a todos los ejercicios predeterminados");
        logger.warn("No es necesario copiar ejercicios. Use findEjerciciosDisponiblesParaProfesor() para obtener ejercicios.");
        
        // Verificar que el profesor existe
        Profesor profesor = profesorService.getProfesorById(profesorId);
        if (profesor == null) {
            throw new RuntimeException("Profesor no encontrado con ID: " + profesorId);
        }
        
        // Verificar que hay ejercicios predeterminados disponibles
        long countPredeterminados = exerciseRepository.countEjerciciosPredeterminados();
        logger.info("Ejercicios predeterminados disponibles en el sistema: {}", countPredeterminados);
        
        if (countPredeterminados == 0) {
            throw new RuntimeException("No hay ejercicios predeterminados en el sistema. Cargue ejercicios predeterminados primero.");
        }
        
        logger.info("El profesor {} ya tiene acceso a {} ejercicios predeterminados compartidos", 
                   profesor.getNombre(), countPredeterminados);
        
        // Retornar 0 porque no se copian ejercicios (son compartidos)
        return 0;
    }

    /**
     * DEPRECADO: Los ejercicios predeterminados ahora son compartidos (profesor = null)
     * Los profesores ya tienen acceso automático a todos los ejercicios predeterminados
     * Solo limpia ejercicios propios del profesor si se solicita
     * 
     * @param profesorId ID del profesor
     * @param limpiarEjerciciosAnteriores Si true, elimina ejercicios PROPIOS del profesor (no predeterminados)
     * @return Siempre retorna 0 (los ejercicios predeterminados ya están disponibles)
     */
    @Deprecated
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int asignarEjerciciosPredefinidosAProfesorAvanzado(Long profesorId, boolean limpiarEjerciciosAnteriores) {
        logger.warn("=== MÉTODO DEPRECADO: asignarEjerciciosPredefinidosAProfesorAvanzado ===");
        logger.warn("Los ejercicios predeterminados ahora son compartidos (profesor = null)");
        logger.warn("Los profesores ya tienen acceso automático a todos los ejercicios predeterminados");
        
        // Obtener el profesor destino
        Profesor profesor = profesorService.getProfesorById(profesorId);
        if (profesor == null) {
            throw new RuntimeException("Profesor no encontrado con ID: " + profesorId);
        }
        
        logger.info("Profesor destino: {} ({})", profesor.getNombre(), profesor.getCorreo());
        
        // Si se solicita limpieza, eliminar SOLO ejercicios PROPIOS del profesor (no predeterminados)
        if (limpiarEjerciciosAnteriores) {
            logger.info("Limpiando ejercicios PROPIOS del profesor {}...", profesor.getNombre());
            limpiarEjerciciosPropiosDelProfesor(profesorId);
            logger.info("Ejercicios propios eliminados del profesor {}", profesor.getNombre());
        }
        
        // Verificar que hay ejercicios predeterminados disponibles
        long countPredeterminados = exerciseRepository.countEjerciciosPredeterminados();
        logger.info("Ejercicios predeterminados disponibles en el sistema: {}", countPredeterminados);
        
        if (countPredeterminados == 0) {
            throw new RuntimeException("No hay ejercicios predeterminados en el sistema. Cargue ejercicios predeterminados primero.");
        }
        
        logger.info("El profesor {} ya tiene acceso a {} ejercicios predeterminados compartidos", 
                   profesor.getNombre(), countPredeterminados);
        logger.info("Limpieza de ejercicios propios realizada: {}", limpiarEjerciciosAnteriores);
        
        // Retornar 0 porque no se copian ejercicios (son compartidos)
        return 0;
    }

    /**
     * Limpia SOLO los ejercicios PROPIOS de un profesor (excluyendo predeterminados)
     * Los ejercicios predeterminados son compartidos y no se eliminan
     */
    private void limpiarEjerciciosPropiosDelProfesor(Long profesorId) {
        try {
            logger.info("Iniciando limpieza de ejercicios PROPIOS del profesor ID: {}", profesorId);
            
            // Contar ejercicios PROPIOS antes de eliminar (excluyendo predeterminados)
            long countBefore = exerciseRepository.countEjerciciosPropiosDelProfesor(profesorId);
            logger.info("Ejercicios PROPIOS existentes del profesor: {}", countBefore);
            
            if (countBefore > 0) {
                // Obtener ejercicios propios del profesor
                List<Exercise> ejerciciosPropios = exerciseRepository.findEjerciciosPropiosDelProfesor(profesorId);
                
                // Eliminar ejercicios propios uno por uno
                for (Exercise ejercicio : ejerciciosPropios) {
                    exerciseRepository.delete(ejercicio);
                }
                
                // Verificar eliminación
                long countAfter = exerciseRepository.countEjerciciosPropiosDelProfesor(profesorId);
                logger.info("Ejercicios PROPIOS restantes después de limpieza: {}", countAfter);
                
                if (countAfter > 0) {
                    throw new RuntimeException("Error: No se pudieron eliminar todos los ejercicios propios del profesor");
                }
                
                logger.info("Limpieza exitosa: {} ejercicios PROPIOS eliminados del profesor", countBefore);
            } else {
                logger.info("El profesor no tenía ejercicios propios para limpiar");
            }
            
        } catch (Exception e) {
            logger.error("Error limpiando ejercicios propios del profesor {}: {}", profesorId, e.getMessage(), e);
            throw new RuntimeException("Error al limpiar ejercicios propios del profesor: " + e.getMessage(), e);
        }
    }

    /**
     * DEPRECADO: Filtra ejercicios que no están duplicados para el profesor
     * Ya no se usa porque los ejercicios predeterminados son compartidos
     */
    @Deprecated
    private List<Exercise> filtrarEjerciciosSinDuplicar(List<Exercise> ejerciciosAdmin, Long profesorId) {
        // Obtener nombres de ejercicios PROPIOS existentes del profesor (excluyendo predeterminados)
        List<Exercise> ejerciciosPropios = exerciseRepository.findEjerciciosPropiosDelProfesor(profesorId);
        List<String> nombresExistentes = ejerciciosPropios.stream()
                .map(Exercise::getName)
                .collect(Collectors.toList());
        
        // Filtrar ejercicios que no existen como propios del profesor
        return ejerciciosAdmin.stream()
                .filter(ejercicio -> !nombresExistentes.contains(ejercicio.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Procesa la asignación de ejercicios en lotes para mejor rendimiento
     */
    private int procesarAsignacionEnLotes(List<Exercise> ejercicios, Profesor profesor) {
        int totalAsignados = 0;
        AtomicInteger contador = new AtomicInteger(0);
        
        logger.info("Procesando {} ejercicios en lotes de {}", ejercicios.size(), TAMANO_LOTE);
        
        for (int i = 0; i < ejercicios.size(); i += TAMANO_LOTE) {
            int finLote = Math.min(i + TAMANO_LOTE, ejercicios.size());
            List<Exercise> lote = ejercicios.subList(i, finLote);
            
            logger.info("Procesando lote {}/{}: ejercicios {} a {}", 
                       (i / TAMANO_LOTE) + 1, 
                       (ejercicios.size() + TAMANO_LOTE - 1) / TAMANO_LOTE,
                       i + 1, finLote);
            
            try {
                // Clonar ejercicios del lote
                List<Exercise> ejerciciosClonados = clonarEjerciciosEnLote(lote, profesor);
                
                // Guardar lote completo de una vez
                List<Exercise> ejerciciosGuardados = exerciseRepository.saveAll(ejerciciosClonados);
                totalAsignados += ejerciciosGuardados.size();
                
                // Actualizar contador para feedback
                contador.addAndGet(ejerciciosGuardados.size());
                logger.info("Lote procesado exitosamente. Total asignado: {}/{}", contador.get(), ejercicios.size());
                
            } catch (Exception e) {
                logger.error("Error procesando lote {}: {}", (i / TAMANO_LOTE) + 1, e.getMessage());
                throw new RuntimeException("Error al procesar lote de ejercicios: " + e.getMessage(), e);
            }
        }
        
        return totalAsignados;
    }

    /**
     * Clona ejercicios en lotes de manera optimizada
     */
    private List<Exercise> clonarEjerciciosEnLote(List<Exercise> ejercicios, Profesor profesor) {
        List<Exercise> ejerciciosClonados = new ArrayList<>();
        
        for (Exercise original : ejercicios) {
            Exercise copia = new Exercise();
            
            // Copiar propiedades básicas
            copia.setName(original.getName());
            copia.setDescription(original.getDescription());
            copia.setType(original.getType());
            // Crear nueva colección para evitar referencias compartidas
            if (original.getGrupos() != null) {
                copia.setGrupos(new HashSet<>(original.getGrupos()));
            } else {
                copia.setGrupos(new HashSet<>());
            }
            copia.setVideoUrl(original.getVideoUrl());
            copia.setInstructions(original.getInstructions());
            copia.setBenefits(original.getBenefits());
            copia.setContraindications(original.getContraindications());
            
            // Clonar imagen de manera optimizada
            if (original.getImagen() != null) {
                copia.setImagen(clonarImagenOptimizada(original.getImagen()));
            } else {
                copia.setImagen(null);
            }
            
            // Asignar al profesor destino
            copia.setProfesor(profesor);
            
            ejerciciosClonados.add(copia);
        }
        
        return ejerciciosClonados;
    }

    /**
     * Clona imagen copiando el archivo físico y creando una nueva entrada
     * Nota: Para ejercicios predeterminados, se puede reutilizar la misma imagen
     */
    private Imagen clonarImagenOptimizada(Imagen original) {
        try {
            // Si la imagen original tiene un archivo físico, copiarlo
            if (original.getRutaArchivo() != null) {
                // Obtener contenido del archivo original
                byte[] contenido = imagenServicio.obtenerContenido(original.getId());
                // Guardar como nueva imagen (crea nuevo archivo físico)
                return imagenServicio.guardar(contenido, original.getNombre());
            } else {
                // Si no hay archivo, crear una imagen vacía
                Imagen nuevaImg = new Imagen();
                nuevaImg.setMime(original.getMime());
                nuevaImg.setNombre(original.getNombre());
                nuevaImg.setRutaArchivo(""); // Ruta vacía
                nuevaImg.setTamanoBytes(0L);
                return nuevaImg;
            }
        } catch (Exception e) {
            logger.warn("Error al clonar imagen: {}", e.getMessage());
            // Fallback: crear imagen básica
            Imagen nuevaImg = new Imagen();
            nuevaImg.setMime(original.getMime());
            nuevaImg.setNombre(original.getNombre());
            nuevaImg.setRutaArchivo("");
            nuevaImg.setTamanoBytes(0L);
            return nuevaImg;
        }
    }

    /**
     * Asigna ejercicios específicos a un profesor
     * @param profesorId ID del profesor
     * @param ejercicioIds Lista de IDs de ejercicios a asignar
     * @return Número de ejercicios asignados
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int asignarEjerciciosEspecificos(Long profesorId, List<Long> ejercicioIds) {
        logger.info("Asignando {} ejercicios específicos al profesor ID: {}", ejercicioIds.size(), profesorId);
        
        try {
            // Obtener ejercicios específicos
            List<Exercise> ejercicios = exerciseRepository.findAllById(ejercicioIds);
            if (ejercicios.isEmpty()) {
                logger.warn("No se encontraron ejercicios con los IDs proporcionados");
                return 0;
            }
            
            // Obtener profesor destino
            Profesor profesor = profesorService.getProfesorById(profesorId);
            if (profesor == null) {
                throw new RuntimeException("Profesor no encontrado con ID: " + profesorId);
            }
            
            // Verificar duplicados
            List<Exercise> ejerciciosSinDuplicar = filtrarEjerciciosSinDuplicar(ejercicios, profesorId);
            
            if (ejerciciosSinDuplicar.isEmpty()) {
                logger.info("El profesor ya tiene todos los ejercicios seleccionados asignados");
                return 0;
            }
            
            // Procesar asignación
            int ejerciciosAsignados = procesarAsignacionEnLotes(ejerciciosSinDuplicar, profesor);
            
            logger.info("Ejercicios específicos asignados exitosamente: {}", ejerciciosAsignados);
            return ejerciciosAsignados;
            
        } catch (Exception e) {
            logger.error("Error asignando ejercicios específicos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al asignar ejercicios específicos: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene estadísticas de asignación para un profesor
     * @param profesorId ID del profesor
     * @return Estadísticas de asignación
     */
    public AsignacionStats obtenerEstadisticasAsignacion(Long profesorId) {
        try {
            Profesor profesor = profesorService.getProfesorById(profesorId);
            if (profesor == null) {
                throw new RuntimeException("Profesor no encontrado con ID: " + profesorId);
            }
            
            // Contar ejercicios PROPIOS del profesor (excluyendo predeterminados)
            long ejerciciosPropios = exerciseRepository.countEjerciciosPropiosDelProfesor(profesorId);
            
            // Contar ejercicios predeterminados disponibles (compartidos)
            long totalPredeterminados = exerciseRepository.countEjerciciosPredeterminados();
            
            // Total disponible = predeterminados + propios
            long totalDisponible = totalPredeterminados + ejerciciosPropios;
            
            return new AsignacionStats(profesorId, ejerciciosPropios, totalDisponible);
            
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener estadísticas: " + e.getMessage(), e);
        }
    }

    /**
     * Clase interna para estadísticas de asignación
     */
    public static class AsignacionStats {
        private final Long profesorId;
        private final long ejerciciosAsignados;
        private final long totalDisponible;
        
        public AsignacionStats(Long profesorId, long ejerciciosAsignados, long totalDisponible) {
            this.profesorId = profesorId;
            this.ejerciciosAsignados = ejerciciosAsignados;
            this.totalDisponible = totalDisponible;
        }
        
        // Getters
        public Long getProfesorId() { return profesorId; }
        public long getEjerciciosAsignados() { return ejerciciosAsignados; }
        public long getTotalDisponible() { return totalDisponible; }
        public long getEjerciciosPendientes() { return totalDisponible - ejerciciosAsignados; }
        public double getPorcentajeCompletado() { 
            return totalDisponible > 0 ? (double) ejerciciosAsignados / totalDisponible * 100 : 0; 
        }
    }
}
