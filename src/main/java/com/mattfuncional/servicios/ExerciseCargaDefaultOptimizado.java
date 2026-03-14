package com.mattfuncional.servicios;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StreamUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import com.mattfuncional.entidades.Exercise;
import com.mattfuncional.entidades.GrupoMuscular;
import com.mattfuncional.entidades.Imagen;
import com.mattfuncional.repositorios.ExerciseRepository;

@Service
public class ExerciseCargaDefaultOptimizado {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseCargaDefaultOptimizado.class);
    
    private final ExerciseService exerciseService;
    private final ExerciseRepository exerciseRepository;
    private final ProfesorService profesorService;
    private final GrupoMuscularService grupoMuscularService;
    
    @Autowired
    private ImageOptimizationService imageOptimizationService;
    
    @Autowired
    private WebPConversionService webPConversionService;
    
    @Autowired
    private ImagenServicio imagenServicio;
    
    @Autowired
    private org.springframework.transaction.PlatformTransactionManager transactionManager;
    
    @Autowired
    private com.mattfuncional.repositorios.ImagenRepository imagenRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Value("${mattfuncional.uploads.dir:uploads}")
    private String uploadsDir;
    
    @Value("${mattfuncional.uploads.ejercicios:ejercicios}")
    private String ejerciciosDir;
    
    // Pool de hilos para procesamiento paralelo
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private final com.mattfuncional.repositorios.SerieEjercicioRepository serieEjercicioRepository;

    public ExerciseCargaDefaultOptimizado(ExerciseRepository exerciseRepository, 
                                        ExerciseService exerciseService,
                                        ProfesorService profesorService,
                                        GrupoMuscularService grupoMuscularService,
                                        com.mattfuncional.repositorios.SerieEjercicioRepository serieEjercicioRepository) {
        this.exerciseService = exerciseService;
        this.exerciseRepository = exerciseRepository;
        this.profesorService = profesorService;
        this.grupoMuscularService = grupoMuscularService;
        this.serieEjercicioRepository = serieEjercicioRepository;
    }

    /**
     * Carga ejercicios predeterminados de manera optimizada usando procesamiento en lotes
     * @return Número de ejercicios cargados
     */
    @Transactional(rollbackFor = Exception.class)
    public int saveDefaultExercisesOptimizado() {
        long startTime = System.currentTimeMillis();
        logger.info("=== Iniciando carga OPTIMIZADA de ejercicios predeterminados (BORRAR TODO) ===");
        logger.info("🖼️ Sistema de imágenes híbrido: WebP + GIF + Fallbacks");
        
        try {
            // PRIMERO: Diagnosticar el sistema de imágenes (sin afectar transacción)
            logger.info("🔧 Diagnóstico del sistema de imágenes...");
            try {
                diagnosticarSistema();
            } catch (Exception e) {
                logger.warn("⚠️ Error en diagnóstico (continuando): {}", e.getMessage());
            }
            
            // SEGUNDO: Verificar estado inicial de la base de datos
            logger.info("Verificando estado inicial de la base de datos...");
            verificarEstadoBaseDatos();
            
            // TERCERO: Limpiar ejercicios existentes directamente en esta transacción
            logger.info("Limpiando ejercicios existentes...");
            limpiarEjerciciosExistentes();
            logger.info("Ejercicios existentes eliminados correctamente");
            
            // CUARTO: Verificar que la limpieza fue exitosa
            logger.info("Verificando que la limpieza fue exitosa...");
            verificarLimpiezaExitosa();
            
            // QUINTO: Los ejercicios predeterminados NO tienen profesor (profesor = null)
            // Ya no necesitamos el profesor admin para crear predeterminados
            logger.info("Creando ejercicios predeterminados (sin profesor asignado)");

            // SEXTO: Crear lista de ejercicios predeterminados (sin profesor). Sin optimizar imágenes para carga rápida.
            logger.info("Creando lista de ejercicios predeterminados (sin optimizar imágenes)...");
            List<Exercise> defaultExercises = crearListaEjerciciosPredeterminados(null, true, false);
            logger.info("Lista creada: {} ejercicios", defaultExercises.size());
            
            if (!defaultExercises.isEmpty()) {
                long ejerciciosConProfesor = defaultExercises.stream().filter(e -> e.getProfesor() != null).count();
                long ejerciciosSinFlag = defaultExercises.stream().filter(e -> e.getEsPredeterminado() == null || !e.getEsPredeterminado()).count();
                if (ejerciciosConProfesor > 0 || ejerciciosSinFlag > 0) {
                    logger.warn("Config incorrecta: {} con profesor, {} sin flag predeterminado", ejerciciosConProfesor, ejerciciosSinFlag);
                }
            }

            // SÉPTIMO: Procesar en lotes para mejor rendimiento
            int ejerciciosCargados = procesarEjerciciosEnLotes(defaultExercises);
            
            // OCTAVO: Verificar que los ejercicios se guardaron correctamente
            logger.info("🔍 Verificando que los ejercicios se guardaron en la BD...");
            
            // Forzar commit de la transacción actual
            logger.info("💾 Forzando commit de la transacción...");
            
            long ejerciciosEnBD = exerciseRepository.countEjerciciosPredeterminados();
            long totalEjercicios = exerciseRepository.count();
            logger.info("📊 Ejercicios predeterminados en BD: {} (esperados: {})", ejerciciosEnBD, ejerciciosCargados);
            logger.info("📊 Total de ejercicios en BD: {}", totalEjercicios);
            
            // Verificar directamente con una consulta SQL si es necesario
            if (ejerciciosEnBD == 0 && ejerciciosCargados > 0) {
                logger.error("❌ PROBLEMA CRÍTICO: Se cargaron {} ejercicios pero hay 0 en BD", ejerciciosCargados);
                logger.error("   Esto indica que la transacción se hizo rollback o no se completó");
                logger.error("   Verificando ejercicios con profesor = null directamente...");
                
                List<Exercise> ejerciciosSinProfesor = exerciseRepository.findByProfesorIsNull();
                logger.error("   Ejercicios con profesor = null: {}", ejerciciosSinProfesor.size());
                
                if (ejerciciosSinProfesor.isEmpty()) {
                    logger.error("   ❌ CONFIRMADO: No hay ejercicios en BD. La transacción se hizo rollback.");
                    throw new RuntimeException("Los ejercicios no se guardaron en la BD. La transacción probablemente se hizo rollback.");
                }
            }
            
            if (ejerciciosEnBD != ejerciciosCargados) {
                logger.error("❌ DISCREPANCIA: Se cargaron {} ejercicios pero hay {} en BD", ejerciciosCargados, ejerciciosEnBD);
                // Listar algunos ejercicios para debugging
                List<Exercise> ejerciciosVerificados = exerciseRepository.findEjerciciosPredeterminados();
                logger.info("Ejercicios encontrados en BD (mostrando primeros 10):");
                ejerciciosVerificados.stream().limit(10).forEach(e -> 
                    logger.info("  - {} (ID: {}, profesor: {}, esPredeterminado: {})", 
                              e.getName(), e.getId(), 
                              e.getProfesor() == null ? "null" : e.getProfesor().getNombre(),
                              e.getEsPredeterminado()));
                
                // Verificar si hay ejercicios sin profesor pero sin flag
                List<Exercise> ejerciciosSinProfesor = exerciseRepository.findByProfesorIsNull();
                logger.info("Ejercicios con profesor = null: {}", ejerciciosSinProfesor.size());
                ejerciciosSinProfesor.stream().limit(5).forEach(e -> 
                    logger.info("  - {} (ID: {}, esPredeterminado: {})", 
                              e.getName(), e.getId(), e.getEsPredeterminado()));
            } else {
                logger.info("✅ Verificación exitosa: {} ejercicios predeterminados guardados correctamente", ejerciciosEnBD);
            }
            
            long endTime = System.currentTimeMillis();
            long tiempoTotal = endTime - startTime;
            
            logger.info("=== Carga OPTIMIZADA completada exitosamente (base limpiada) ===");
            logger.info("Ejercicios cargados: {}", ejerciciosCargados);
            logger.info("Ejercicios en BD: {}", ejerciciosEnBD);
            logger.info("Tiempo total: {} ms", tiempoTotal);
            
            // MOSTRAR ESTADÍSTICAS DE CONVERSIÓN DE IMÁGENES (sin afectar transacción)
            try {
                mostrarEstadisticasConversionImagenes();
            } catch (Exception e) {
                logger.warn("⚠️ Error mostrando estadísticas (continuando): {}", e.getMessage());
            }
            
            return ejerciciosCargados;
            
        } catch (Exception e) {
            logger.error("Error en carga optimizada: {}", e.getMessage(), e);
            throw new RuntimeException("Error al cargar ejercicios predeterminados optimizados: " + e.getMessage(), e);
        }
    }

    /**
     * Carga ejercicios predeterminados solo los nuevos (mantiene ejercicios existentes)
     * @return Número de ejercicios nuevos agregados
     */
    @Transactional
    public int saveDefaultExercisesAgregarNuevos() {
        long startTime = System.currentTimeMillis();
        logger.info("=== Iniciando carga de ejercicios predeterminados (AGREGAR SOLO NUEVOS) ===");
        
        try {
            // PRIMERO: Los ejercicios predeterminados NO tienen profesor
            logger.info("Verificando ejercicios predeterminados existentes...");

            // SEGUNDO: Obtener ejercicios predeterminados existentes para verificar duplicados
            List<Exercise> ejerciciosExistentes = exerciseRepository.findEjerciciosPredeterminados();
            Set<String> nombresExistentes = ejerciciosExistentes.stream()
                .map(Exercise::getName)
                .collect(java.util.stream.Collectors.toSet());
            
            logger.info("Ejercicios existentes encontrados: {} (nombres: {})", 
                       ejerciciosExistentes.size(), nombresExistentes);

            // TERCERO: Crear lista de ejercicios predeterminados (sin profesor, sin optimizar imágenes)
            List<Exercise> defaultExercises = crearListaEjerciciosPredeterminados(null, true, false);
            logger.info("Lista de ejercicios predeterminados creada: {} ejercicios", defaultExercises.size());

            // CUARTO: Filtrar solo ejercicios que NO existen
            List<Exercise> ejerciciosNuevos = defaultExercises.stream()
                .filter(ej -> !nombresExistentes.contains(ej.getName()))
                .collect(java.util.stream.Collectors.toList());
            
            logger.info("Ejercicios nuevos a agregar: {} de {} predeterminados", 
                       ejerciciosNuevos.size(), defaultExercises.size());

            if (ejerciciosNuevos.isEmpty()) {
                logger.info("No hay ejercicios nuevos para agregar");
                return 0;
            }

            // QUINTO: Procesar solo ejercicios nuevos en lotes
            int ejerciciosCargados = procesarEjerciciosEnLotes(ejerciciosNuevos);
            
            long endTime = System.currentTimeMillis();
            long tiempoTotal = endTime - startTime;
            
            logger.info("=== Carga de nuevos ejercicios completada exitosamente ===");
            logger.info("Ejercicios nuevos agregados: {}", ejerciciosCargados);
            logger.info("Ejercicios existentes mantenidos: {}", ejerciciosExistentes.size());
            logger.info("Tiempo total: {} ms", tiempoTotal);
            
            return ejerciciosCargados;
            
        } catch (Exception e) {
            logger.error("Error en carga de nuevos ejercicios: {}", e.getMessage(), e);
            throw new RuntimeException("Error al cargar ejercicios predeterminados nuevos: " + e.getMessage(), e);
        }
    }

    /**
     * Limpia ejercicios existentes de manera ULTRA AGRESIVA usando múltiples estrategias
     */
    private void limpiarEjerciciosExistentes() {
        try {
            logger.info("=== INICIANDO LIMPIEZA RESPETANDO CONSTRAINTS ===");
            
            // PRIMERO: Contar ejercicios antes de eliminar
            long countBefore = exerciseRepository.count();
            logger.info("ESTADO INICIAL: {} ejercicios en la base de datos", countBefore);
            
            if (countBefore > 0) {
                // SEGUNDO: Obtener solo los ejercicios PREDETERMINADOS (profesor = null o esPredeterminado = true)
                List<Exercise> ejerciciosPredeterminados = exerciseRepository.findEjerciciosPredeterminados();
                logger.info("Ejercicios PREDETERMINADOS encontrados: {} (de {} totales)", ejerciciosPredeterminados.size(), countBefore);
                
                if (ejerciciosPredeterminados.isEmpty()) {
                    logger.info("ℹ️ No hay ejercicios predeterminados para eliminar");
                    return;
                }
                
                // TERCERO: Eliminar solo las referencias de ejercicios predeterminados
                logger.info("🔴 PASO 1: ELIMINANDO REFERENCIAS DE EJERCICIOS PREDETERMINADOS...");
                try {
                    // Obtener todas las referencias y filtrar solo las de ejercicios predeterminados
                    List<com.mattfuncional.entidades.SerieEjercicio> todasLasReferencias = serieEjercicioRepository.findAll();
                    List<com.mattfuncional.entidades.SerieEjercicio> referenciasAEliminar = new ArrayList<>();
                    
                    for (com.mattfuncional.entidades.SerieEjercicio referencia : todasLasReferencias) {
                        if (referencia.getExercise() != null) {
                            // Verificar si es predeterminado (profesor = null o esPredeterminado = true)
                            Exercise ejercicio = referencia.getExercise();
                            if (ejercicio.getProfesor() == null || 
                                (ejercicio.getEsPredeterminado() != null && ejercicio.getEsPredeterminado())) {
                                referenciasAEliminar.add(referencia);
                            }
                        }
                    }
                    
                    if (!referenciasAEliminar.isEmpty()) {
                        serieEjercicioRepository.deleteAll(referenciasAEliminar);
                        logger.info("✅ Referencias eliminadas: {} referencias de ejercicios predeterminados", 
                                   referenciasAEliminar.size());
                    } else {
                        logger.info("ℹ️ No hay referencias de ejercicios predeterminados para eliminar");
                    }
                } catch (Exception e) {
                    logger.warn("⚠️ Error eliminando referencias: {}", e.getMessage());
                }
                
                // CUARTO: Ahora eliminar solo los ejercicios predeterminados
                logger.info("🔴 PASO 2: ELIMINANDO SOLO EJERCICIOS PREDETERMINADOS...");
                int eliminados = 0;
                for (Exercise ejercicio : ejerciciosPredeterminados) {
                    try {
                        logger.debug("Eliminando ejercicio predeterminado: {} (ID: {})", ejercicio.getName(), ejercicio.getId());
                        exerciseRepository.delete(ejercicio);
                        eliminados++;
                        
                        // Log de progreso cada 5 ejercicios
                        if (eliminados % 5 == 0) {
                            logger.info("Progreso: {}/{} ejercicios predeterminados eliminados", eliminados, ejerciciosPredeterminados.size());
                        }
                        
                    } catch (Exception e) {
                        logger.warn("⚠️ Error eliminando ejercicio predeterminado {} (ID: {}): {}", 
                                   ejercicio.getName(), ejercicio.getId(), e.getMessage());
                        // Continuar con el siguiente ejercicio
                    }
                }
                        
                // Verificación final
                long countFinal = exerciseRepository.count();
                long ejerciciosPredeterminadosEliminados = countBefore - countFinal;
                logger.info("ESTADO FINAL: {} ejercicios restantes de {} iniciales", countFinal, countBefore);
                logger.info("✅ LIMPIEZA COMPLETADA: {} ejercicios predeterminados eliminados", eliminados);
                logger.info("📊 Resumen: {} ejercicios predeterminados eliminados, {} ejercicios de profesores preservados", 
                           ejerciciosPredeterminadosEliminados, countFinal);
                        
                if (countFinal > 0) {
                    logger.info("ℹ️ {} ejercicios preservados (pertenecen a profesores)", countFinal);
                }
            } else {
                logger.info("ℹ️ No hay ejercicios existentes para eliminar");
            }
            
            // SIEMPRE limpiar la carpeta de uploads, incluso si no había ejercicios
            logger.info("🔴 PASO 3: ELIMINANDO CARPETA DE UPLOADS/EJERCICIOS...");
            try {
                limpiarCarpetaUploads();
            } catch (Exception e) {
                logger.warn("⚠️ Error eliminando carpeta de uploads (continuando): {}", e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("❌ ERROR durante la limpieza de ejercicios: {}", e.getMessage(), e);
            throw new RuntimeException("Error al limpiar ejercicios existentes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Limpia completamente la carpeta de uploads/ejercicios eliminando todos los archivos
     */
    private void limpiarCarpetaUploads() {
        try {
            Path carpetaEjercicios = Paths.get(uploadsDir, ejerciciosDir);
            Path carpetaAbsoluta = carpetaEjercicios.toAbsolutePath();
            
            logger.info("🗑️ Limpiando carpeta de uploads: {}", carpetaAbsoluta);
            
            if (!Files.exists(carpetaEjercicios)) {
                logger.info("ℹ️ La carpeta {} no existe, no hay nada que limpiar", carpetaAbsoluta);
                return;
            }
            
            // Contar archivos antes de eliminar
            long archivosAntes = 0;
            try {
                archivosAntes = Files.list(carpetaEjercicios)
                    .filter(Files::isRegularFile)
                    .count();
                logger.info("📊 Archivos encontrados en carpeta: {}", archivosAntes);
            } catch (IOException e) {
                logger.warn("⚠️ No se pudo contar archivos: {}", e.getMessage());
            }
            
            // Eliminar todos los archivos en la carpeta
            try {
                Files.list(carpetaEjercicios)
                    .filter(Files::isRegularFile)
                    .forEach(archivo -> {
                        try {
                            Files.delete(archivo);
                            logger.debug("✅ Archivo eliminado: {}", archivo.getFileName());
                        } catch (IOException e) {
                            logger.warn("⚠️ No se pudo eliminar archivo {}: {}", archivo.getFileName(), e.getMessage());
                        }
                    });
                
                logger.info("✅ Carpeta de uploads limpiada: {} archivos eliminados", archivosAntes);
            } catch (IOException e) {
                logger.error("❌ Error al listar archivos en carpeta: {}", e.getMessage(), e);
                throw new RuntimeException("Error al limpiar carpeta de uploads: " + e.getMessage(), e);
            }
            
        } catch (Exception e) {
            logger.error("❌ Error limpiando carpeta de uploads: {}", e.getMessage(), e);
            throw new RuntimeException("Error al limpiar carpeta de uploads: " + e.getMessage(), e);
        }
    }

    /**
     * Procesa ejercicios en lotes para mejor rendimiento
     */
    private int procesarEjerciciosEnLotes(List<Exercise> ejercicios) {
        final int TAMANO_LOTE = 10; // Procesar 10 ejercicios por lote
        int totalCargados = 0;
        AtomicInteger contador = new AtomicInteger(0);
        
        logger.info("Procesando {} ejercicios en lotes de {}", ejercicios.size(), TAMANO_LOTE);
        
        for (int i = 0; i < ejercicios.size(); i += TAMANO_LOTE) {
            int finLote = Math.min(i + TAMANO_LOTE, ejercicios.size());
            List<Exercise> lote = ejercicios.subList(i, finLote);
            
            try {
                for (Exercise ejercicio : lote) {
                    if (ejercicio.getName() == null || ejercicio.getName().trim().isEmpty()) {
                        throw new RuntimeException("Ejercicio sin nombre en el lote");
                    }
                }
                logger.info("Cargando ejercicios {}-{} de {}...", i + 1, finLote, ejercicios.size());
                
                // Guardar cada ejercicio individualmente para mejor control de errores y debugging
                List<Exercise> ejerciciosGuardados = new ArrayList<>();
                for (Exercise ejercicio : lote) {
                    try {
                        // IMPORTANTE: Si la imagen ya está guardada en una transacción separada,
                        // necesitamos hacer merge() para que esté en estado "managed" en el contexto actual
                        Imagen imagenOriginal = ejercicio.getImagen();
                        if (imagenOriginal != null && imagenOriginal.getId() != null) {
                            try {
                                Imagen imagenManaged = entityManager.merge(imagenOriginal);
                                ejercicio.setImagen(imagenManaged);
                            } catch (Exception e) {
                                logger.debug("Merge imagen {}: {}", imagenOriginal.getId(), e.getMessage());
                                ejercicio.setImagen(null);
                            }
                        }
                        Exercise guardado = exerciseRepository.save(ejercicio);
                        exerciseRepository.flush();
                        if (guardado.getId() == null) {
                            throw new RuntimeException("Ejercicio guardado sin ID: " + ejercicio.getName());
                        }
                        ejerciciosGuardados.add(guardado);
                    } catch (Exception e) {
                        throw new RuntimeException("Error guardando ejercicio " + ejercicio.getName() + ": " + e.getMessage(), e);
                    }
                }
                
                try {
                    exerciseRepository.flush();
                } catch (Exception e) {
                    throw new RuntimeException("Error en flush del lote: " + e.getMessage(), e);
                }
                totalCargados += ejerciciosGuardados.size();
                contador.addAndGet(ejerciciosGuardados.size());
                logger.info("Listo {}/{} ejercicios.", contador.get(), ejercicios.size());
                
            } catch (Exception e) {
                logger.error("❌ Error procesando lote {}: {}", (i / TAMANO_LOTE) + 1, e.getMessage(), e);
                throw new RuntimeException("Error al procesar lote de ejercicios: " + e.getMessage(), e);
            }
        }
        
        return totalCargados;
    }

    /**
     * Crea la lista completa de ejercicios predeterminados.
     * @param profesorAdmin null = predeterminados (profesor = null)
     * @param skipImageOptimization true = no optimizar imágenes (carga más rápida, evita bloqueos)
     * @param soloMetadata true = no asignar imágenes (para asegurarEjerciciosPredeterminados que usa uploads/)
     */
    private List<Exercise> crearListaEjerciciosPredeterminados(com.mattfuncional.entidades.Profesor profesorAdmin, boolean skipImageOptimization, boolean soloMetadata) {
        List<Exercise> defaultExercises = new ArrayList<>();
        if (!soloMetadata) cargarImagenesEnLote();
        defaultExercises.addAll(crearEjerciciosBrazos(profesorAdmin, skipImageOptimization, soloMetadata));
        defaultExercises.addAll(crearEjerciciosPiernas(profesorAdmin, skipImageOptimization, soloMetadata));
        defaultExercises.addAll(crearEjerciciosPecho(profesorAdmin, skipImageOptimization, soloMetadata));
        defaultExercises.addAll(crearEjerciciosEspalda(profesorAdmin, skipImageOptimization, soloMetadata));
        defaultExercises.addAll(crearEjerciciosHombros(profesorAdmin, skipImageOptimization, soloMetadata));
        defaultExercises.addAll(crearEjerciciosAbdomen(profesorAdmin, skipImageOptimization, soloMetadata));
        defaultExercises.addAll(crearEjerciciosCardio(profesorAdmin, skipImageOptimization, soloMetadata));
        defaultExercises.addAll(crearEjerciciosElongacion(profesorAdmin, skipImageOptimization, soloMetadata));
        return defaultExercises;
    }

    /**
     * Asegura que existan los 60 ejercicios predeterminados. No borra nada.
     * Imágenes: busca en uploads/ejercicios/ los archivos 1.webp, 1.gif, 2.webp, ... 60.webp.
     * Si no existe el archivo, el ejercicio se crea con imagen por defecto (null).
     * @return número de ejercicios creados en esta ejecución
     */
    @Transactional(rollbackFor = Exception.class)
    public int asegurarEjerciciosPredeterminados() {
        if (exerciseRepository.countEjerciciosPredeterminados() >= 60) return 0;
        // Asegurar grupos musculares primero (por si DataInitializer no corrió o falló, ej. en servidor Donweb)
        grupoMuscularService.asegurarGruposSistema();
        List<Exercise> lista = crearListaEjerciciosPredeterminados(null, true, true);
        int creados = 0;
        for (int i = 0; i < lista.size(); i++) {
            Exercise e = lista.get(i);
            if (exerciseRepository.findByNameAndProfesorIsNull(e.getName()).isPresent()) continue;
            int n = i + 1;
            Imagen img = imagenServicio.registrarArchivoExistente(n + ".webp");
            if (img == null) img = imagenServicio.registrarArchivoExistente(n + ".gif");
            e.setImagen(img);
            exerciseRepository.save(e);
            creados++;
        }
        if (creados > 0) logger.info("Ejercicios predeterminados creados: {} (imágenes desde uploads/ejercicios/)", creados);
        return creados;
    }

    /**
     * Actualiza las imágenes de los ejercicios predeterminados leyendo desde uploads/ejercicios/.
     * La relación es por número de orden (1.webp, 1.gif, 2.webp, ... 60.webp), igual que en la carga inicial.
     * Útil cuando el profesor reemplaza archivos en la carpeta (ej. cambia 11.gif por 11.webp) sin editar cada ejercicio.
     * @return número de ejercicios a los que se les asignó o actualizó la imagen
     */
    @Transactional(rollbackFor = Exception.class)
    public int actualizarImagenesDesdeCarpeta() {
        List<Exercise> listaOrdenada = crearListaEjerciciosPredeterminados(null, true, true);
        AtomicInteger actualizados = new AtomicInteger(0);
        for (int i = 0; i < listaOrdenada.size(); i++) {
            String nombre = listaOrdenada.get(i).getName();
            int n = i + 1;
            exerciseRepository.findByNameAndProfesorIsNull(nombre).ifPresent(e -> {
                Imagen img = imagenServicio.registrarArchivoExistente(n + ".webp");
                if (img == null) img = imagenServicio.registrarArchivoExistente(n + ".gif");
                if (img != null) {
                    e.setImagen(img);
                    exerciseRepository.save(e);
                    actualizados.incrementAndGet();
                }
            });
        }
        if (actualizados.get() > 0) logger.info("Imágenes de ejercicios actualizadas desde carpeta: {} ejercicios", actualizados.get());
        return actualizados.get();
    }

    /**
     * Pre-carga todas las imágenes para evitar múltiples lecturas de archivo
     */
    private void cargarImagenesEnLote() {
        // Esta función se puede expandir para pre-cargar todas las imágenes
        // y almacenarlas en memoria temporal para reutilización
        logger.info("Imágenes pre-cargadas para procesamiento en lotes");
    }
    
    /** Si soloMetadata es true devuelve null; si no, crea imagen desde classpath (para carga antigua). */
    private Imagen imagenOpcionalParaEjercicio(String numGif, String nombreEjercicio, boolean soloMetadata, boolean skipImageOptimization) {
        return soloMetadata ? null : imagenOpcionalParaEjercicioDesdeClasspath(numGif, nombreEjercicio, skipImageOptimization);
    }

    /**
     * Configura un ejercicio como predeterminado o lo asigna a un profesor
     * Si profesorAdmin es null, el ejercicio se marca como predeterminado (sin profesor)
     * Si profesorAdmin no es null, se asigna al profesor (compatibilidad hacia atrás)
     */
    private void configurarEjercicioComoPredeterminado(Exercise ejercicio, com.mattfuncional.entidades.Profesor profesorAdmin) {
        if (profesorAdmin == null) {
            // Ejercicio predeterminado: sin profesor y con flag activado
            ejercicio.setProfesor(null);
            ejercicio.setEsPredeterminado(true);
            logger.debug("Ejercicio configurado como predeterminado (sin profesor): {}", ejercicio.getName());
        } else {
            // DEPRECADO: Compatibilidad hacia atrás - asignar al profesor admin
            // Esto ya no debería usarse, pero se mantiene por compatibilidad
            ejercicio.setProfesor(profesorAdmin);
            ejercicio.setEsPredeterminado(false);
            logger.warn("⚠️ Ejercicio asignado al admin (DEPRECADO): {}", ejercicio.getName());
        }
    }

    private List<Exercise> crearEjerciciosBrazos(com.mattfuncional.entidades.Profesor profesorAdmin, boolean skipImageOptimization, boolean soloMetadata) {
        GrupoMuscular grupo = grupoMuscularService.findByNombreSistema("BRAZOS").orElseThrow();
        List<Exercise> ejercicios = new ArrayList<>();
        Exercise ej1 = new Exercise("Curl de Bíceps con Barra", "Mejora la fuerza de los bíceps",
                Set.of(grupo), "BRAZOS", "http://example.com/video1", 
                "Realizar 3 series de 12 repeticiones", "", "");
        ej1.setImagen(imagenOpcionalParaEjercicio("1.gif", "Curl de Bíceps con Barra", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej1, profesorAdmin);
        ejercicios.add(ej1);
        
        Exercise ej2 = new Exercise("Extensiones de Tríceps", "Desarrolla los tríceps",
                Set.of(grupo), "BRAZOS", "http://example.com/video2", 
                "Hacer 3 series de 15 repeticiones", "", "");
        ej2.setImagen(imagenOpcionalParaEjercicio("2.gif", "Extensiones de Tríceps", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej2, profesorAdmin);
        ejercicios.add(ej2);
        
        Exercise ej3 = new Exercise("Curl de Martillo", "Trabaja los bíceps y los antebrazos",
                Set.of(grupo), "BRAZOS", "http://example.com/video3", 
                "Hacer 3 series de 12 repeticiones", "", "");
        ej3.setImagen(imagenOpcionalParaEjercicio("3.gif", "Curl de Martillo", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej3, profesorAdmin);
        ejercicios.add(ej3);
        
        Exercise ej4 = new Exercise("Fondos", "Desarrolla los tríceps y el pecho", 
                Set.of(grupo), "BRAZOS", "http://example.com/video4", 
                "Realizar 4 series de 10 repeticiones", "", "");
        ej4.setImagen(imagenOpcionalParaEjercicio("4.gif", "Fondos", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej4, profesorAdmin);
        ejercicios.add(ej4);
        
        Exercise ej5 = new Exercise("Curl de Bíceps con Mancuernas", "Aísla los bíceps",
                Set.of(grupo), "BRAZOS", "http://example.com/video5", 
                "Hacer 3 series de 12 repeticiones", "", "");
        ej5.setImagen(imagenOpcionalParaEjercicio("5.gif", "Curl de Bíceps con Mancuernas", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej5, profesorAdmin);
        ejercicios.add(ej5);
        
        Exercise ej6 = new Exercise("Press Francés", "Fortalece los tríceps", 
                Set.of(grupo), "Ejercicio para tríceps", "http://example.com/video6",
                "Realizar 3 series de 12 repeticiones", "", "");
        ej6.setImagen(imagenOpcionalParaEjercicio("6.gif", "Press Francés", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej6, profesorAdmin);
        ejercicios.add(ej6);
        
        Exercise ej7 = new Exercise("Curl de Bíceps en Predicador", "Aísla los bíceps",
                Set.of(grupo), "Ejercicio para bíceps", "http://example.com/video7",
                "Hacer 3 series de 12 repeticiones", "", "");
        ej7.setImagen(imagenOpcionalParaEjercicio("7.gif", "Curl de Bíceps en Predicador", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej7, profesorAdmin);
        ejercicios.add(ej7);
        
        Exercise ej8 = new Exercise("Patada de Tríceps", "Aísla los tríceps", 
                Set.of(grupo), "Ejercicio para tríceps", "http://example.com/video8",
                "Realizar 3 series de 15 repeticiones", "", "");
        ej8.setImagen(imagenOpcionalParaEjercicio("8.gif", "Patada de Tríceps", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej8, profesorAdmin);
        ejercicios.add(ej8);
        
        Exercise ej9 = new Exercise("Curl de Bíceps con Cable", "Aísla los bíceps", 
                Set.of(grupo), "Ejercicio para bíceps", "http://example.com/video9",
                "Hacer 3 series de 12 repeticiones", "", "");
        ej9.setImagen(imagenOpcionalParaEjercicio("9.gif", "Curl de Bíceps con Cable", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej9, profesorAdmin);
        ejercicios.add(ej9);
        
        Exercise ej10 = new Exercise("Press de Tríceps en Máquina", "Fortalece los tríceps",
                Set.of(grupo), "Ejercicio para tríceps", "http://example.com/video10",
                "Realizar 3 series de 15 repeticiones", "", "");
        ej10.setImagen(imagenOpcionalParaEjercicio("10.gif", "Press de Tríceps en Máquina", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej10, profesorAdmin);
        ejercicios.add(ej10);
        
        return ejercicios;
    }

    private List<Exercise> crearEjerciciosPiernas(com.mattfuncional.entidades.Profesor profesorAdmin, boolean soloMetadata, boolean skipImageOptimization) {
        GrupoMuscular grupo = grupoMuscularService.findByNombreSistema("PIERNAS").orElseThrow();
        List<Exercise> ejercicios = new ArrayList<>();
        
        Exercise ej11 = new Exercise("Sentadillas con Barra", "Fortalece los cuádriceps",
                Set.of(grupo), "PIERNAS", "http://example.com/video11", 
                "Realizar 4 series de 10 repeticiones", "", "");
        ej11.setImagen(imagenOpcionalParaEjercicio("11.gif", "Sentadillas con Barra", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej11, profesorAdmin);
        ejercicios.add(ej11);
        
        Exercise ej12 = new Exercise("Elevación de Caderas", "Desarrolla los glúteos",
                Set.of(grupo), "PIERNAS", "http://example.com/video12", 
                "Hacer 4 series de 12 repeticiones", "", "");
        ej12.setImagen(imagenOpcionalParaEjercicio("12.gif", "Elevación de Caderas", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej12, profesorAdmin);
        ejercicios.add(ej12);
        
        Exercise ej13 = new Exercise("Peso Muerto", "Trabaja los isquiotibiales y glúteos",
                Set.of(grupo), "PIERNAS", "http://example.com/video13", 
                "Realizar 4 series de 10 repeticiones", "", "");
        ej13.setImagen(imagenOpcionalParaEjercicio("13.gif", "Peso Muerto", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej13, profesorAdmin);
        ejercicios.add(ej13);
        
        Exercise ej14 = new Exercise("Prensa de Piernas", "Fortalece los cuádriceps",
                Set.of(grupo), "PIERNAS", "http://example.com/video14", 
                "Hacer 4 series de 12 repeticiones", "", "");
        ej14.setImagen(imagenOpcionalParaEjercicio("14.gif", "Prensa de Piernas", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej14, profesorAdmin);
        ejercicios.add(ej14);
        
        Exercise ej15 = new Exercise("Zancadas", "Trabaja cuádriceps y glúteos",
                Set.of(grupo), "PIERNAS", "http://example.com/video15", 
                "Realizar 3 series de 12 repeticiones por pierna", "", "");
        ej15.setImagen(imagenOpcionalParaEjercicio("15.gif", "Zancadas", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej15, profesorAdmin);
        ejercicios.add(ej15);
        
        Exercise ej16 = new Exercise("Extensiones de Cuádriceps", "Aísla los cuádriceps",
                Set.of(grupo), "PIERNAS", "http://example.com/video16", 
                "Hacer 3 series de 15 repeticiones", "", "");
        ej16.setImagen(imagenOpcionalParaEjercicio("16.gif", "Extensiones de Cuádriceps", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej16, profesorAdmin);
        ejercicios.add(ej16);
        
        Exercise ej17 = new Exercise("Curl de Isquiotibiales", "Aísla los isquiotibiales",
                Set.of(grupo), "PIERNAS", "http://example.com/video17", 
                "Realizar 3 series de 12 repeticiones", "", "");
        ej17.setImagen(imagenOpcionalParaEjercicio("17.gif", "Curl de Isquiotibiales", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej17, profesorAdmin);
        ejercicios.add(ej17);
        
        Exercise ej18 = new Exercise("Elevación de Gemelos", "Fortalece las pantorrillas",
                Set.of(grupo), "PIERNAS", "http://example.com/video18", 
                "Hacer 4 series de 20 repeticiones", "", "");
        ej18.setImagen(imagenOpcionalParaEjercicio("18.gif", "Elevación de Gemelos", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej18, profesorAdmin);
        ejercicios.add(ej18);
        
        Exercise ej19 = new Exercise("Sentadillas Búlgaras", "Trabaja cuádriceps y glúteos",
                Set.of(grupo), "PIERNAS", "http://example.com/video19", 
                "Realizar 3 series de 10 repeticiones por pierna", "", "");
        ej19.setImagen(imagenOpcionalParaEjercicio("19.gif", "Sentadillas Búlgaras", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej19, profesorAdmin);
        ejercicios.add(ej19);
        
        Exercise ej20 = new Exercise("Hip Thrust", "Desarrolla los glúteos",
                Set.of(grupo), "PIERNAS", "http://example.com/video20", 
                "Hacer 4 series de 12 repeticiones", "", "");
        ej20.setImagen(imagenOpcionalParaEjercicio("20.gif", "Hip Thrust", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej20, profesorAdmin);
        ejercicios.add(ej20);
        
        return ejercicios;
    }

    private List<Exercise> crearEjerciciosPecho(com.mattfuncional.entidades.Profesor profesorAdmin, boolean soloMetadata, boolean skipImageOptimization) {
        GrupoMuscular grupo = grupoMuscularService.findByNombreSistema("PECHO").orElseThrow();
        List<Exercise> ejercicios = new ArrayList<>();
        
        Exercise ej21 = new Exercise("Press de Banca", "Desarrolla el pecho",
                Set.of(grupo), "PECHO", "http://example.com/video21", 
                "Realizar 4 series de 8-10 repeticiones", "", "");
        ej21.setImagen(imagenOpcionalParaEjercicio("21.gif", "Press de Banca", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej21, profesorAdmin);
        ejercicios.add(ej21);
        
        Exercise ej22 = new Exercise("Press de Banca Inclinado", "Trabaja la parte superior del pecho",
                Set.of(grupo), "PECHO", "http://example.com/video22", 
                "Hacer 3 series de 10 repeticiones", "", "");
        ej22.setImagen(imagenOpcionalParaEjercicio("22.gif", "Press de Banca Inclinado", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej22, profesorAdmin);
        ejercicios.add(ej22);
        
        Exercise ej23 = new Exercise("Aperturas con Mancuernas", "Aísla el pecho",
                Set.of(grupo), "PECHO", "http://example.com/video23", 
                "Realizar 3 series de 12 repeticiones", "", "");
        ej23.setImagen(imagenOpcionalParaEjercicio("23.gif", "Aperturas con Mancuernas", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej23, profesorAdmin);
        ejercicios.add(ej23);
        
        Exercise ej24 = new Exercise("Flexiones", "Fortalece el pecho",
                Set.of(grupo), "PECHO", "http://example.com/video24", 
                "Hacer 3 series de 15 repeticiones", "", "");
        ej24.setImagen(imagenOpcionalParaEjercicio("24.gif", "Flexiones", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej24, profesorAdmin);
        ejercicios.add(ej24);
        
        Exercise ej25 = new Exercise("Press de Banca Declinado", "Trabaja la parte inferior del pecho",
                Set.of(grupo), "PECHO", "http://example.com/video25", 
                "Realizar 3 series de 10 repeticiones", "", "");
        ej25.setImagen(imagenOpcionalParaEjercicio("25.gif", "Press de Banca Declinado", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej25, profesorAdmin);
        ejercicios.add(ej25);
        
        return ejercicios;
    }

    private List<Exercise> crearEjerciciosEspalda(com.mattfuncional.entidades.Profesor profesorAdmin, boolean soloMetadata, boolean skipImageOptimization) {
        GrupoMuscular grupo = grupoMuscularService.findByNombreSistema("ESPALDA").orElseThrow();
        List<Exercise> ejercicios = new ArrayList<>();
        
        Exercise ej26 = new Exercise("Dominadas", "Desarrolla la espalda",
                Set.of(grupo), "ESPALDA", "http://example.com/video26", 
                "Realizar 3 series de 8-10 repeticiones", "", "");
        ej26.setImagen(imagenOpcionalParaEjercicio("26.gif", "Dominadas", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej26, profesorAdmin);
        ejercicios.add(ej26);
        
        Exercise ej27 = new Exercise("Remo con Barra", "Fortalece la espalda",
                Set.of(grupo), "ESPALDA", "http://example.com/video27", 
                "Hacer 4 series de 10 repeticiones", "", "");
        ej27.setImagen(imagenOpcionalParaEjercicio("27.gif", "Remo con Barra", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej27, profesorAdmin);
        ejercicios.add(ej27);
        
        Exercise ej28 = new Exercise("Remo con Cable", "Aísla la espalda",
                Set.of(grupo), "ESPALDA", "http://example.com/video28", 
                "Realizar 3 series de 12 repeticiones", "", "");
        ej28.setImagen(imagenOpcionalParaEjercicio("28.gif", "Remo con Cable", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej28, profesorAdmin);
        ejercicios.add(ej28);
        
        Exercise ej29 = new Exercise("Pull Down", "Desarrolla la espalda",
                Set.of(grupo), "ESPALDA", "http://example.com/video29", 
                "Hacer 3 series de 12 repeticiones", "", "");
        ej29.setImagen(imagenOpcionalParaEjercicio("29.gif", "Pull Down", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej29, profesorAdmin);
        ejercicios.add(ej29);
        
        Exercise ej30 = new Exercise("Remo Invertido", "Fortalece la espalda",
                Set.of(grupo), "ESPALDA", "http://example.com/video30", 
                "Realizar 3 series de 15 repeticiones", "", "");
        ej30.setImagen(imagenOpcionalParaEjercicio("30.gif", "Remo Invertido", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej30, profesorAdmin);
        ejercicios.add(ej30);
        
        return ejercicios;
    }

    private List<Exercise> crearEjerciciosHombros(com.mattfuncional.entidades.Profesor profesorAdmin, boolean soloMetadata, boolean skipImageOptimization) {
        GrupoMuscular grupo = grupoMuscularService.findByNombreSistema("BRAZOS").orElseThrow();
        List<Exercise> ejercicios = new ArrayList<>();
        
        Exercise ej31 = new Exercise("Press Militar", "Desarrolla los hombros",
                Set.of(grupo), "HOMBROS", "http://example.com/video31", 
                "Realizar 3 series de 10 repeticiones", "", "");
        ej31.setImagen(imagenOpcionalParaEjercicio("31.gif", "Press Militar", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej31, profesorAdmin);
        ejercicios.add(ej31);
        
        Exercise ej32 = new Exercise("Elevaciones Laterales", "Aísla los hombros",
                Set.of(grupo), "HOMBROS", "http://example.com/video32", 
                "Hacer 3 series de 12 repeticiones", "", "");
        ej32.setImagen(imagenOpcionalParaEjercicio("32.gif", "Elevaciones Laterales", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej32, profesorAdmin);
        ejercicios.add(ej32);
        
        Exercise ej33 = new Exercise("Elevaciones Frontales", "Trabaja los hombros frontales",
                Set.of(grupo), "HOMBROS", "http://example.com/video33", 
                "Realizar 3 series de 12 repeticiones", "", "");
        ej33.setImagen(imagenOpcionalParaEjercicio("33.gif", "Elevaciones Frontales", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej33, profesorAdmin);
        ejercicios.add(ej33);
        
        Exercise ej34 = new Exercise("Elevaciones Posteriores", "Desarrolla los hombros posteriores",
                Set.of(grupo), "HOMBROS", "http://example.com/video34", 
                "Hacer 3 series de 12 repeticiones", "", "");
        ej34.setImagen(imagenOpcionalParaEjercicio("34.gif", "Elevaciones Posteriores", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej34, profesorAdmin);
        ejercicios.add(ej34);
        
        Exercise ej35 = new Exercise("Press Arnold", "Trabaja los hombros",
                Set.of(grupo), "HOMBROS", "http://example.com/video35", 
                "Realizar 3 series de 10 repeticiones", "", "");
        ej35.setImagen(imagenOpcionalParaEjercicio("35.gif", "Press Arnold", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej35, profesorAdmin);
        ejercicios.add(ej35);
        
        return ejercicios;
    }

    private List<Exercise> crearEjerciciosAbdomen(com.mattfuncional.entidades.Profesor profesorAdmin, boolean soloMetadata, boolean skipImageOptimization) {
        GrupoMuscular grupo = grupoMuscularService.findByNombreSistema("PIERNAS").orElseThrow();
        List<Exercise> ejercicios = new ArrayList<>();
        
        Exercise ej36 = new Exercise("Crunches", "Fortalece el abdomen",
                Set.of(grupo), "ABDOMEN", "http://example.com/video36", 
                "Realizar 3 series de 20 repeticiones", "", "");
        ej36.setImagen(imagenOpcionalParaEjercicio("36.gif", "Crunches", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej36, profesorAdmin);
        ejercicios.add(ej36);
        
        Exercise ej37 = new Exercise("Plancha", "Fortalece el core",
                Set.of(grupo), "ABDOMEN", "http://example.com/video37", 
                "Mantener 3 series de 30 segundos", "", "");
        ej37.setImagen(imagenOpcionalParaEjercicio("37.gif", "Plancha", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej37, profesorAdmin);
        ejercicios.add(ej37);
        
        Exercise ej38 = new Exercise("Russian Twists", "Trabaja los oblicuos",
                Set.of(grupo), "ABDOMEN", "http://example.com/video38", 
                "Hacer 3 series de 20 repeticiones", "", "");
        ej38.setImagen(imagenOpcionalParaEjercicio("38.gif", "Russian Twists", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej38, profesorAdmin);
        ejercicios.add(ej38);
        
        Exercise ej39 = new Exercise("Leg Raises", "Fortalece el abdomen inferior",
                Set.of(grupo), "ABDOMEN", "http://example.com/video39", 
                "Realizar 3 series de 15 repeticiones", "", "");
        ej39.setImagen(imagenOpcionalParaEjercicio("39.gif", "Leg Raises", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej39, profesorAdmin);
        ejercicios.add(ej39);
        
        Exercise ej40 = new Exercise("Mountain Climbers Abdomen", "Trabaja todo el core",
                Set.of(grupo), "ABDOMEN", "http://example.com/video40", 
                "Hacer 3 series de 30 segundos", "", "");
        ej40.setImagen(imagenOpcionalParaEjercicio("40.gif", "Mountain Climbers Abdomen", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej40, profesorAdmin);
        ejercicios.add(ej40);
        
        return ejercicios;
    }

    private List<Exercise> crearEjerciciosCardio(com.mattfuncional.entidades.Profesor profesorAdmin, boolean soloMetadata, boolean skipImageOptimization) {
        GrupoMuscular grupo = grupoMuscularService.findByNombreSistema("CARDIO").orElseThrow();
        List<Exercise> ejercicios = new ArrayList<>();
        
        Exercise ej41 = new Exercise("Correr", "Mejora la resistencia cardiovascular",
                Set.of(grupo), "CARDIO", "http://example.com/video41", 
                "Correr durante 30 minutos a ritmo moderado", "", "");
        ej41.setImagen(imagenOpcionalParaEjercicio("41.gif", "Correr", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej41, profesorAdmin);
        ejercicios.add(ej41);
        
        Exercise ej42 = new Exercise("Bicicleta", "Desarrolla la resistencia",
                Set.of(grupo), "CARDIO", "http://example.com/video42", 
                "Pedalear durante 45 minutos", "", "");
        ej42.setImagen(imagenOpcionalParaEjercicio("42.gif", "Bicicleta", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej42, profesorAdmin);
        ejercicios.add(ej42);
        
        Exercise ej43 = new Exercise("Saltar Cuerda", "Mejora la coordinación",
                Set.of(grupo), "CARDIO", "http://example.com/video43", 
                "Saltar durante 20 minutos", "", "");
        ej43.setImagen(imagenOpcionalParaEjercicio("43.gif", "Saltar Cuerda", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej43, profesorAdmin);
        ejercicios.add(ej43);
        
        Exercise ej44 = new Exercise("Burpees", "Ejercicio completo de cardio",
                Set.of(grupo), "CARDIO", "http://example.com/video44", 
                "Realizar 3 series de 10 repeticiones", "", "");
        ej44.setImagen(imagenOpcionalParaEjercicio("44.gif", "Burpees", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej44, profesorAdmin);
        ejercicios.add(ej44);
        
        Exercise ej45 = new Exercise("Jumping Jacks", "Mejora la resistencia",
                Set.of(grupo), "CARDIO", "http://example.com/video45", 
                "Hacer 3 series de 30 repeticiones", "", "");
        ej45.setImagen(imagenOpcionalParaEjercicio("45.gif", "Jumping Jacks", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej45, profesorAdmin);
        ejercicios.add(ej45);
        
        Exercise ej46 = new Exercise("High Knees", "Trabaja la resistencia",
                Set.of(grupo), "CARDIO", "http://example.com/video46", 
                "Realizar 3 series de 30 segundos", "", "");
        ej46.setImagen(imagenOpcionalParaEjercicio("46.gif", "High Knees", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej46, profesorAdmin);
        ejercicios.add(ej46);
        
        Exercise ej47 = new Exercise("Mountain Climbers Cardio", "Ejercicio de cardio",
                Set.of(grupo), "CARDIO", "http://example.com/video47", 
                "Hacer 3 series de 30 segundos", "", "");
        ej47.setImagen(imagenOpcionalParaEjercicio("47.gif", "Mountain Climbers Cardio", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej47, profesorAdmin);
        ejercicios.add(ej47);
        
        Exercise ej48 = new Exercise("Squat Jumps", "Combina fuerza y cardio",
                Set.of(grupo), "CARDIO", "http://example.com/video48", 
                "Realizar 3 series de 15 repeticiones", "", "");
        ej48.setImagen(imagenOpcionalParaEjercicio("48.gif", "Squat Jumps", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej48, profesorAdmin);
        ejercicios.add(ej48);
        
        Exercise ej49 = new Exercise("Ciclismo", "Mejora la resistencia",
                Set.of(grupo), "CARDIO", "http://example.com/video49", 
                "Pedalear durante 60 minutos", "", "");
        ej49.setImagen(imagenOpcionalParaEjercicio("49.gif", "Ciclismo", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej49, profesorAdmin);
        ejercicios.add(ej49);
        
        Exercise ej50 = new Exercise("Subir Escaleras", "Fortalece el sistema cardiovascular y muscular",
                Set.of(grupo), "Ejercicio para cardio", "http://example.com/video50",
                "Subir y bajar escaleras durante 20 minutos", "", "");
        ej50.setImagen(imagenOpcionalParaEjercicio("50.gif", "Subir Escaleras", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej50, profesorAdmin);
        ejercicios.add(ej50);
        
        return ejercicios;
    }

    private List<Exercise> crearEjerciciosElongacion(com.mattfuncional.entidades.Profesor profesorAdmin, boolean soloMetadata, boolean skipImageOptimization) {
        GrupoMuscular grupo = grupoMuscularService.findByNombreSistema("ELONGACION").orElseThrow();
        List<Exercise> ejercicios = new ArrayList<>();
        
        Exercise ej51 = new Exercise("Estiramiento de Cuádriceps", "Aumenta la flexibilidad en los cuádriceps", 
                Set.of(grupo), "ELONGACION", "http://example.com/video51",
                "Realizar 3 series de 30 segundos por pierna", "", "");
        ej51.setImagen(imagenOpcionalParaEjercicio("51.gif", "Estiramiento de Cuádriceps", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej51, profesorAdmin);
        ejercicios.add(ej51);
        
        Exercise ej52 = new Exercise("Estiramiento de Isquiotibiales", "Desarrolla la flexibilidad en los isquiotibiales", 
                Set.of(grupo), "ELONGACION", "http://example.com/video52", 
                "Hacer 3 series de 30 segundos por pierna", "", "");
        ej52.setImagen(imagenOpcionalParaEjercicio("52.gif", "Estiramiento de Isquiotibiales", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej52, profesorAdmin);
        ejercicios.add(ej52);
        
        Exercise ej53 = new Exercise("Estiramiento de Cadera", "Aumenta la flexibilidad en la cadera",
                Set.of(grupo), "ELONGACION", "http://example.com/video53",
                "Realizar 3 series de 30 segundos por pierna", "", "");
        ej53.setImagen(imagenOpcionalParaEjercicio("53.gif", "Estiramiento de Cadera", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej53, profesorAdmin);
        ejercicios.add(ej53);
        
        Exercise ej54 = new Exercise("Estiramiento de Hombros", "Desarrolla la flexibilidad en los hombros",
                Set.of(grupo), "ELONGACION", "http://example.com/video54",
                "Hacer 3 series de 30 segundos por brazo", "", "");
        ej54.setImagen(imagenOpcionalParaEjercicio("54.gif", "Estiramiento de Hombros", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej54, profesorAdmin);
        ejercicios.add(ej54);
        
        Exercise ej55 = new Exercise("Estiramiento de Cuello", "Aumenta la flexibilidad en el cuello",
                Set.of(grupo), "ELONGACION", "http://example.com/video55", 
                "Realizar 3 series de 30 segundos", "", "");
        ej55.setImagen(imagenOpcionalParaEjercicio("55.gif", "Estiramiento de Cuello", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej55, profesorAdmin);
        ejercicios.add(ej55);
        
        Exercise ej56 = new Exercise("Estiramiento de Espalda Baja", "Desarrolla la flexibilidad en la espalda baja", 
                Set.of(grupo), "Ejercicio de elongación", "http://example.com/video56",
                "Hacer 3 series de 30 segundos", "", "");
        ej56.setImagen(imagenOpcionalParaEjercicio("56.gif", "Estiramiento de Espalda Baja", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej56, profesorAdmin);
        ejercicios.add(ej56);
        
        Exercise ej57 = new Exercise("Estiramiento de Pecho", "Aumenta la flexibilidad en el pecho",
                Set.of(grupo), "Ejercicio de elongación", "http://example.com/video57",
                "Realizar 3 series de 30 segundos", "", "");
        ej57.setImagen(imagenOpcionalParaEjercicio("57.gif", "Estiramiento de Pecho", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej57, profesorAdmin);
        ejercicios.add(ej57);
        
        Exercise ej58 = new Exercise("Estiramiento de Pantorrillas", "Desarrolla la flexibilidad en las pantorrillas",
                Set.of(grupo), "Ejercicio de elongación", "http://example.com/video58",
                "Hacer 3 series de 30 segundos por pierna", "", "");
        ej58.setImagen(imagenOpcionalParaEjercicio("58.gif", "Estiramiento de Pantorrillas", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej58, profesorAdmin);
        ejercicios.add(ej58);
        
        Exercise ej59 = new Exercise("Estiramiento de Cuádriceps en Pareja", "Aumenta la flexibilidad en los cuádriceps", 
                Set.of(grupo), "Ejercicio de elongación", "http://example.com/video59", 
                "Realizar 3 series de 30 segundos por pierna", "", "");
        ej59.setImagen(imagenOpcionalParaEjercicio("59.gif", "Estiramiento de Cuádriceps en Pareja", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej59, profesorAdmin);
        ejercicios.add(ej59);
        
        Exercise ej60 = new Exercise("Estiramiento de Isquiotibiales en Pareja", "Desarrolla la flexibilidad en los isquiotibiales", 
                Set.of(grupo), "Ejercicio de elongación", "http://example.com/video60",
                "Hacer 3 series de 30 segundos por pierna", "", "");
        ej60.setImagen(imagenOpcionalParaEjercicio("60.gif", "Estiramiento de Isquiotibiales en Pareja", soloMetadata, skipImageOptimization));
        configurarEjercicioComoPredeterminado(ej60, profesorAdmin);
        ejercicios.add(ej60);
        
        return ejercicios;
    }

    /**
     * Crea imagen para ejercicio desde classpath. skipOptimization=true en carga masiva evita bloqueos y acelera.
     */
    private Imagen imagenOpcionalParaEjercicioDesdeClasspath(String nombreArchivo, String nombreEjercicio) {
        return imagenOpcionalParaEjercicioDesdeClasspath(nombreArchivo, nombreEjercicio, false);
    }

    private Imagen imagenOpcionalParaEjercicioDesdeClasspath(String nombreArchivo, String nombreEjercicio, boolean skipOptimization) {
        byte[] contenido = null;
        String mime = "image/gif";
        String nombre = nombreEjercicio + ".gif";
        String formatoDetectado = detectarFormatoDisponible(nombreArchivo);
        String rutaCompleta = "/static/gif-ejercicios/" + formatoDetectado;

        try (InputStream is = getClass().getResourceAsStream(rutaCompleta)) {
            if (is != null) {
                byte[] contenidoOriginal = StreamUtils.copyToByteArray(is);
                if (skipOptimization || imageOptimizationService == null) {
                    contenido = contenidoOriginal;
                    mime = "image/" + (formatoDetectado.contains(".") ? formatoDetectado.substring(formatoDetectado.lastIndexOf(".") + 1) : "gif");
                    nombre = nombreEjercicio + "." + (formatoDetectado.contains(".") ? formatoDetectado.substring(formatoDetectado.lastIndexOf(".") + 1) : "gif");
                } else {
                    try {
                        byte[] optimizado = imageOptimizationService.optimizeImage(contenidoOriginal, formatoDetectado);
                        if (optimizado != null && optimizado.length > 0) {
                            contenido = optimizado;
                        } else {
                            contenido = contenidoOriginal;
                        }
                    } catch (Exception e) {
                        contenido = contenidoOriginal;
                    }
                    mime = "image/" + (formatoDetectado.contains(".") ? formatoDetectado.substring(formatoDetectado.lastIndexOf(".") + 1) : "gif");
                    nombre = nombreEjercicio + "." + (formatoDetectado.contains(".") ? formatoDetectado.substring(formatoDetectado.lastIndexOf(".") + 1) : "gif");
                }
            }
        } catch (Exception e) {
            logger.debug("Imagen {}: {}", nombreArchivo, e.getMessage());
        }

        if (contenido == null || contenido.length == 0) {
            return null;
        }
        try {
            Imagen imagen = guardarImagenEnTransaccionSeparada(contenido, nombre, formatoDetectado, mime);
            return imagen;
        } catch (Exception e) {
            logger.debug("Error guardando imagen {}: {}", nombreEjercicio, e.getMessage());
            return null;
        }
    }
    
    /**
     * Guarda una imagen en una transacción separada para evitar que errores de imágenes
     * marquen la transacción principal como rollback-only
     */
    private Imagen guardarImagenEnTransaccionSeparada(byte[] contenido, String nombre, String formatoDetectado, String mime) {
        if (transactionManager == null) {
            // Si no hay TransactionTemplate, intentar guardar directamente (menos seguro pero funcional)
            logger.warn("⚠️ TransactionManager no disponible, guardando imagen directamente");
            try {
                return imagenServicio.guardar(contenido, nombre);
            } catch (Exception e) {
                logger.error("Error guardando imagen directamente: {}", e.getMessage());
                return null;
            }
        }
        
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setPropagationBehavior(org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.setIsolationLevel(org.springframework.transaction.TransactionDefinition.ISOLATION_READ_COMMITTED);
        
        try {
            Imagen imagenGuardada = transactionTemplate.execute(status -> {
                try {
                    Imagen imagen = imagenServicio.guardar(contenido, nombre);
                    logger.debug("Imagen guardada en transacción separada: {}", imagen.getId());
                    return imagen;
                } catch (Exception e) {
                    logger.error("Error en transacción separada al guardar imagen: {}", e.getMessage());
                    // Marcar para rollback solo de esta transacción, no de la principal
                    status.setRollbackOnly();
                    return null;
                }
            });
            
            // Si la imagen se guardó correctamente, refrescarla en el contexto de la transacción principal
            // para que esté en estado "managed" y Hibernate pueda manejar correctamente la relación
            if (imagenGuardada != null && imagenGuardada.getId() != null && imagenRepository != null) {
                try {
                    // Refrescar la imagen desde la BD para que esté en estado "managed"
                    Imagen imagenRefrescada = imagenRepository.findById(imagenGuardada.getId()).orElse(imagenGuardada);
                    logger.debug("Imagen refrescada en contexto principal: {}", imagenRefrescada.getId());
                    return imagenRefrescada;
                } catch (Exception e) {
                    logger.warn("No se pudo refrescar la imagen, usando la original: {}", e.getMessage());
                    return imagenGuardada;
                }
            }
            
            return imagenGuardada;
        } catch (Exception e) {
            logger.error("Error ejecutando transacción separada para imagen: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Detecta automáticamente el formato del archivo basado en la extensión
     */
    private String detectarFormatoArchivo(String nombreArchivo) {
        if (nombreArchivo == null) return "gif";
        
        String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();
        
        switch (extension) {
            case "webp": return "webp";
            case "png": return "png";
            case "jpg":
            case "jpeg": return "jpg";
            case "gif": return "gif";
            case "bmp": return "bmp";
            default: return "gif";
        }
    }
    
    /**
     * Detecta automáticamente el formato disponible de la imagen
     * Verifica primero WebP, luego GIF
     */
    private String detectarFormatoDisponible(String nombreArchivo) {
        // Extraer el número del ejercicio (ej: "1.gif" -> "1")
        String numeroEjercicio = nombreArchivo.replaceAll("[^0-9]", "");
        
        // Verificar si existe WebP primero (preferido)
        String rutaWebP = "/static/gif-ejercicios/" + numeroEjercicio + ".webp";
        try (InputStream is = getClass().getResourceAsStream(rutaWebP)) {
            if (is != null) {
                logger.debug("✅ Detectado formato WebP para ejercicio {}: {}.webp", numeroEjercicio, numeroEjercicio);
                return numeroEjercicio + ".webp";
            }
        } catch (Exception e) {
            logger.debug("No se pudo verificar WebP para ejercicio {}: {}", numeroEjercicio, e.getMessage());
        }
        
        // Si no existe WebP, verificar GIF
        String rutaGif = "/static/gif-ejercicios/" + numeroEjercicio + ".gif";
        try (InputStream is = getClass().getResourceAsStream(rutaGif)) {
            if (is != null) {
                logger.debug("✅ Detectado formato GIF para ejercicio {}: {}.gif", numeroEjercicio, numeroEjercicio);
                return numeroEjercicio + ".gif";
            }
        } catch (Exception e) {
            logger.debug("No se pudo verificar GIF para ejercicio {}: {}", numeroEjercicio, e.getMessage());
        }
        
        // Si no existe ninguno, retornar GIF por defecto (se usará imagen por defecto)
        logger.warn("⚠️ No se encontró imagen para ejercicio {} (ni .webp ni .gif)", numeroEjercicio);
        return numeroEjercicio + ".gif";
    }
    
    /**
     * Verifica si un archivo es WebP válido
     */
    private boolean esArchivoWebP(byte[] datos) {
        if (datos.length < 12) return false;
        
        // Verificar firma WebP: RIFF....WEBP
        return datos[0] == 'R' && datos[1] == 'I' && 
               datos[2] == 'F' && datos[3] == 'F' &&
               datos[8] == 'W' && datos[9] == 'E' && 
               datos[10] == 'B' && datos[11] == 'P';
    }

    /**
     * Verifica el estado inicial de la base de datos
     */
    private void verificarEstadoBaseDatos() {
        try {
            long totalEjercicios = exerciseRepository.count();
            logger.info("📊 ESTADO INICIAL DE LA BASE DE DATOS:");
            logger.info("   - Total de ejercicios: {}", totalEjercicios);
            
            if (totalEjercicios > 0) {
                // Obtener algunos ejemplos para debug
                List<Exercise> ejerciciosEjemplo = exerciseRepository.findAll();
                ejerciciosEjemplo.stream()
                    .limit(5)
                    .forEach(ej -> logger.info("   - Ejercicio existente: {} (ID: {})", ej.getName(), ej.getId()));
                
                if (ejerciciosEjemplo.size() > 5) {
                    logger.info("   - ... y {} ejercicios más", ejerciciosEjemplo.size() - 5);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error verificando estado de la base de datos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al verificar estado de la base de datos: " + e.getMessage(), e);
        }
    }
    
    /**
     * Verifica que la limpieza fue exitosa (solo ejercicios predeterminados)
     */
    private void verificarLimpiezaExitosa() {
        try {
            // Contar solo los ejercicios PREDETERMINADOS (profesor = null o esPredeterminado = true)
            long ejerciciosPredeterminadosRestantes = exerciseRepository.countEjerciciosPredeterminados();
            long totalEjercicios = exerciseRepository.count();
            
            logger.info("🔍 VERIFICACIÓN DE LIMPIEZA DE EJERCICIOS PREDETERMINADOS:");
            logger.info("   - Ejercicios predeterminados restantes: {}", ejerciciosPredeterminadosRestantes);
            logger.info("   - Total de ejercicios en BD: {}", totalEjercicios);
            
            if (ejerciciosPredeterminadosRestantes > 0) {
                logger.error("❌ VERIFICACIÓN FALLIDA: Quedaron {} ejercicios predeterminados después de la limpieza", ejerciciosPredeterminadosRestantes);
                throw new RuntimeException("La limpieza no fue exitosa. Quedaron " + ejerciciosPredeterminadosRestantes + " ejercicios predeterminados");
            } else {
                logger.info("✅ VERIFICACIÓN EXITOSA: Todos los ejercicios predeterminados fueron eliminados");
                if (totalEjercicios > 0) {
                    logger.info("ℹ️ {} ejercicios de profesores preservados correctamente", totalEjercicios);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error verificando limpieza: {}", e.getMessage(), e);
            throw new RuntimeException("Error al verificar limpieza: " + e.getMessage(), e);
        }
    }

    /**
     * Cierra el pool de hilos al destruir el servicio
     */
    public void destroy() {
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    /**
     * Muestra estadísticas de la conversión de imágenes
     */
    private void mostrarEstadisticasConversionImagenes() {
        if (imageOptimizationService != null) {
            logger.info("📊 ESTADÍSTICAS DE CONVERSIÓN DE IMÁGENES:");
            logger.info("   - WebP soportado: {}", imageOptimizationService.isWebPSupported() ? "Sí" : "No");
            logger.info("   - Formato preferido: {}", imageOptimizationService.getPreferredFormat());
            logger.info("   - Sistema híbrido activo: ✅");
            logger.info("   - Conversión automática: ✅");
            logger.info("   - Fallback a formato original: ✅");
            
            // DIAGNÓSTICO ADICIONAL
            logger.info("🔍 DIAGNÓSTICO DEL SISTEMA:");
            logger.info("   - ImageOptimizationService inyectado: ✅");
            logger.info("   - Método isWebPSupported disponible: ✅");
            logger.info("   - Método optimizeImage disponible: ✅");
            
        } else {
            logger.error("❌ CRÍTICO: ImageOptimizationService NO está configurado!");
            logger.error("   - Verificar inyección de dependencias");
            logger.error("   - Verificar que el servicio esté marcado como @Service");
            logger.error("   - Verificar que esté en el classpath");
        }
    }
    
    /**
     * Método de diagnóstico para verificar el sistema
     */
    public void diagnosticarSistema() {
        logger.info("🔧 === DIAGNÓSTICO DEL SISTEMA DE IMÁGENES ===");
        
        // DIAGNÓSTICO 1: ImageOptimizationService
        if (imageOptimizationService == null) {
            logger.error("❌ ImageOptimizationService es NULL");
        } else {
            try {
                boolean webpSoportado = imageOptimizationService.isWebPSupported();
                String formatoPreferido = imageOptimizationService.getPreferredFormat();
                
                logger.info("✅ ImageOptimizationService funcionando:");
                logger.info("   - WebP soportado: {}", webpSoportado);
                logger.info("   - Formato preferido: {}", formatoPreferido);
                
                // No probar optimizeImage con datos inválidos (evita bloqueos/NPE en carga)
                
            } catch (Exception e) {
                logger.error("❌ Error diagnosticando ImageOptimizationService: {}", e.getMessage(), e);
            }
        }
        
        // DIAGNÓSTICO 2: WebPConversionService
        if (webPConversionService == null) {
            logger.error("❌ WebPConversionService es NULL");
        } else {
            try {
                boolean webpSoportado = webPConversionService.isWebPSupported();
                
                logger.info("✅ WebPConversionService funcionando:");
                logger.info("   - WebP soportado: {}", webpSoportado);
                
                // Probar con una imagen de prueba
                byte[] imagenPrueba = "test".getBytes();
                try {
                    byte[] resultado = webPConversionService.convertToWebP(imagenPrueba, "gif");
                    logger.info("✅ Método convertToWebP funcionando: {}", resultado != null ? "Sí" : "No");
                } catch (Exception e) {
                    logger.error("❌ Error en convertToWebP: {}", e.getMessage());
                }
                
            } catch (Exception e) {
                logger.error("❌ Error diagnosticando WebPConversionService: {}", e.getMessage(), e);
            }
        }
        
        logger.info("🔧 === FIN DEL DIAGNÓSTICO ===");
    }
}
