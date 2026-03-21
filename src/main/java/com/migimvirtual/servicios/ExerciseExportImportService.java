package com.migimvirtual.servicios;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.migimvirtual.entidades.Exercise;
import com.migimvirtual.entidades.Imagen;
import com.migimvirtual.entidades.Profesor;
import com.migimvirtual.repositorios.ImagenRepository;

/**
 * Servicio unificado para exportación e importación de ejercicios
 * Elimina duplicaciones entre admin y profesores
 * Usa estructura JSON consistente (array directo)
 * 
 * VERSIÓN SIMPLIFICADA - Sin compresión para mayor compatibilidad
 */
@Service
public class ExerciseExportImportService {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseExportImportService.class);
    
    private final ExerciseService exerciseService;
    private final ProfesorService profesorService;
    private final GrupoMuscularService grupoMuscularService;
    private final ImagenRepository imagenRepository;
    private final ImagenServicio imagenServicio;
    private final ObjectMapper objectMapper;
    
    // Directorio para backups (se creará automáticamente)
    private static final String BACKUP_DIR = "backups/ejercicios";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    // Obtener directorio de trabajo actual para rutas absolutas
    private final Path workingDir = Paths.get("").toAbsolutePath();

    public ExerciseExportImportService(ExerciseService exerciseService, ProfesorService profesorService,
                                        GrupoMuscularService grupoMuscularService,
                                     ImagenRepository imagenRepository, ImagenServicio imagenServicio) {
        this.exerciseService = exerciseService;
        this.profesorService = profesorService;
        this.grupoMuscularService = grupoMuscularService;
        this.imagenRepository = imagenRepository;
        this.imagenServicio = imagenServicio;
        
        // Configurar ObjectMapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Exporta ejercicios predeterminados del sistema a JSON (array directo)
     * @return Información del backup creado
     */
    @Transactional(readOnly = true)
    public Map<String, Object> exportarEjerciciosAdmin() {
        return exportarEjerciciosProfesor(null);
    }

    /**
     * Exporta ejercicios a JSON (array directo)
     * @param profesorId ID del profesor (null para ejercicios predeterminados del sistema)
     * @return Información del backup creado
     */
    @Transactional(readOnly = true)
    public Map<String, Object> exportarEjerciciosProfesor(Long profesorId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Profesor profesor = null;
            String nombreArchivo;
            List<Exercise> ejercicios;
            
            if (profesorId == null) {
                // Exportar ejercicios PREDETERMINADOS del sistema (profesor = null)
                nombreArchivo = "ejercicios_predeterminados_" + LocalDateTime.now().format(DATE_FORMATTER);
                ejercicios = exerciseService.findEjerciciosPredeterminados();
                if (ejercicios.isEmpty()) {
                    throw new RuntimeException("No hay ejercicios predeterminados para exportar");
                }
            } else {
                // Exportar ejercicios PROPIOS del profesor (excluyendo predeterminados)
                profesor = profesorService.getProfesorById(profesorId);
                if (profesor == null) {
                    throw new RuntimeException("No se encontró el profesor con ID: " + profesorId);
                }
                nombreArchivo = profesor.getCorreo().replace("@", "_") + "_MiGymVirtual_ejer_" + LocalDateTime.now().format(DATE_FORMATTER);
                ejercicios = exerciseService.findEjerciciosPropiosDelProfesor(profesorId);
                if (ejercicios.isEmpty()) {
                    throw new RuntimeException("No hay ejercicios propios para exportar del profesor: " + profesor.getNombre());
                }
            }
            
            // Crear archivo JSON simple
            Path filePath = workingDir.resolve(BACKUP_DIR).resolve(nombreArchivo + ".json");
            
            // Preparar ejercicios para exportación
            List<Map<String, Object>> ejerciciosParaExportar = prepararEjerciciosParaExportacion(ejercicios);
            
            // Convertir a JSON
            String jsonContent = objectMapper.writeValueAsString(ejerciciosParaExportar);
            byte[] jsonBytes = jsonContent.getBytes("UTF-8");
            
            // Guardar archivo JSON
            Files.write(filePath, jsonBytes);
            
            // Información del resultado
            result.put("success", true);
            result.put("fileName", nombreArchivo + ".json");
            result.put("filePath", "backups/ejercicios/" + nombreArchivo + ".json"); // Ruta simplificada
            if (profesor != null) {
                result.put("profesor", profesor.getNombre());
                result.put("profesorId", profesor.getId());
                result.put("profesorOrigen", profesor.getNombre());
            } else {
                result.put("profesor", "Sistema (Predeterminados)");
                result.put("profesorId", null);
                result.put("profesorOrigen", "Sistema (Predeterminados)");
            }
            result.put("ejerciciosExportados", ejerciciosParaExportar.size());
            result.put("fileSize", jsonBytes.length);
            result.put("fechaExportacion", LocalDateTime.now().toString());
            result.put("timestamp", LocalDateTime.now().toString());
            
            String origenInfo = profesor != null ? profesor.getNombre() : "Sistema (Predeterminados)";
            logger.info("Backup creado exitosamente: {} ({} ejercicios, {} bytes) desde: {}",
                       nombreArchivo + ".json", ejerciciosParaExportar.size(), jsonBytes.length, origenInfo);
            
        } catch (Exception e) {
            logger.error("Error al exportar ejercicios: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error al exportar ejercicios: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Prepara ejercicios para exportación con metadatos
     * @param ejercicios Lista de ejercicios
     * @return Lista de ejercicios preparados para exportación
     */
    private List<Map<String, Object>> prepararEjerciciosParaExportacion(List<Exercise> ejercicios) {
        List<Map<String, Object>> ejerciciosParaExportar = new ArrayList<>();
        
        for (Exercise ejercicio : ejercicios) {
            Map<String, Object> ejercicioData = new HashMap<>();
            ejercicioData.put("id", ejercicio.getId());
            ejercicioData.put("name", ejercicio.getName());
            ejercicioData.put("description", ejercicio.getDescription());
            ejercicioData.put("type", ejercicio.getType());
            ejercicioData.put("videoUrl", ejercicio.getVideoUrl());
            ejercicioData.put("instructions", ejercicio.getInstructions());
            ejercicioData.put("benefits", ejercicio.getBenefits());
            ejercicioData.put("contraindications", ejercicio.getContraindications());
            
            // Agregar grupos musculares (por nombre para compatibilidad export/import)
            if (ejercicio.getGrupos() != null && !ejercicio.getGrupos().isEmpty()) {
                List<String> gruposMusculares = ejercicio.getGrupos().stream()
                    .map(com.migimvirtual.entidades.GrupoMuscular::getNombre)
                    .collect(Collectors.toList());
                ejercicioData.put("muscleGroups", gruposMusculares);
            }
            
            // Agregar imagen en Base64 si existe
            if (ejercicio.getImagen() != null) {
                try {
                    // Obtener imagen desde filesystem y convertir a base64
                    String imagenBase64 = null;
                    try {
                        byte[] imagenBytes = imagenServicio.obtenerContenido(ejercicio.getImagen().getId());
                        imagenBase64 = Base64.getEncoder().encodeToString(imagenBytes);
                    } catch (Exception e) {
                        logger.warn("Error al obtener imagen para ejercicio {}: {}", ejercicio.getName(), e.getMessage());
                    }
                    ejercicioData.put("imagenBase64", imagenBase64);
                    ejercicioData.put("tieneImagen", true);
                    ejercicioData.put("mimeType", ejercicio.getImagen().getMime());
                } catch (Exception e) {
                    logger.warn("Error obteniendo imagen para ejercicio {}: {}", ejercicio.getId(), e.getMessage());
                    ejercicioData.put("tieneImagen", false);
                    ejercicioData.put("errorImagen", e.getMessage());
                }
            } else {
                ejercicioData.put("tieneImagen", false);
            }
            
            ejerciciosParaExportar.add(ejercicioData);
        }
        
        return ejerciciosParaExportar;
    }

    /**
     * Importa ejercicios desde archivo JSON
     * @param fileName Nombre del archivo
     * @param profesorDestinoId ID del profesor destino
     * @param limpiarEjerciciosAnteriores Si limpiar ejercicios existentes
     * @return Resultado de la importación
     */
    public Map<String, Object> importarEjerciciosDesdeJSON(String fileName, Long profesorDestinoId, boolean limpiarEjerciciosAnteriores) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Verificar que el archivo existe
            Path filePath = workingDir.resolve(BACKUP_DIR).resolve(fileName);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("Archivo de backup no encontrado: " + fileName);
            }
            
            // Leer archivo JSON
            byte[] jsonBytes = Files.readAllBytes(filePath);
            String jsonContent = new String(jsonBytes, "UTF-8");
            
            // Parsear JSON
            List<Map<String, Object>> ejerciciosData = objectMapper.readValue(jsonContent, List.class);
            
            if (ejerciciosData == null || ejerciciosData.isEmpty()) {
                throw new RuntimeException("El archivo de backup no contiene ejercicios válidos");
            }
            
            // Obtener profesor destino
            Profesor profesorDestino = profesorService.getProfesorById(profesorDestinoId);
            if (profesorDestino == null) {
                throw new RuntimeException("Profesor destino no encontrado: " + profesorDestinoId);
            }
            
            // Limpiar ejercicios anteriores si se solicita
            if (limpiarEjerciciosAnteriores) {
                logger.info("Limpiando ejercicios anteriores del profesor: {}", profesorDestino.getNombre());
                // Obtener ejercicios PROPIOS del profesor (excluyendo predeterminados) y eliminarlos
                List<Exercise> ejerciciosExistentes = exerciseService.findEjerciciosPropiosDelProfesor(profesorDestinoId);
                for (Exercise ejercicio : ejerciciosExistentes) {
                    exerciseService.deleteExercise(ejercicio.getId());
                }
                logger.info("Eliminados {} ejercicios propios anteriores del profesor: {}", ejerciciosExistentes.size(), profesorDestino.getNombre());
            }
            
                         // Importar ejercicios
             int ejerciciosImportados = 0;
             int ejerciciosConImagen = 0;
             int ejerciciosSinImagen = 0;
             List<String> errores = new ArrayList<>();
            
            for (Map<String, Object> ejercicioData : ejerciciosData) {
                try {
                    String nombreEjercicio = (String) ejercicioData.get("name");
                    
                                         // NOTA: El método original NO verificaba duplicados cuando limpiarEjerciciosAnteriores = false
                     // Simplemente importaba todos los ejercicios del backup
                     // Si se desea evitar duplicados, usar limpiarEjerciciosAnteriores = true
                    
                    Exercise ejercicio = new Exercise();
                    ejercicio.setName(nombreEjercicio);
                    ejercicio.setDescription((String) ejercicioData.get("description"));
                    ejercicio.setType((String) ejercicioData.get("type"));
                    ejercicio.setVideoUrl((String) ejercicioData.get("videoUrl"));
                    ejercicio.setInstructions((String) ejercicioData.get("instructions"));
                    ejercicio.setBenefits((String) ejercicioData.get("benefits"));
                    ejercicio.setContraindications((String) ejercicioData.get("contraindications"));
                    // Asignar al profesor destino (ejercicios importados son propios del profesor, no predeterminados)
                    ejercicio.setProfesor(profesorDestino);
                    ejercicio.setEsPredeterminado(false);
                    
                    // Configurar grupos musculares (resolver por nombre: sistema o del profesor)
                    if (ejercicioData.get("muscleGroups") != null) {
                        @SuppressWarnings("unchecked")
                        List<String> nombresGrupos = (List<String>) ejercicioData.get("muscleGroups");
                        ejercicio.setGrupos(grupoMuscularService.resolveGruposByNames(nombresGrupos, profesorDestinoId));
                    }
                    
                    // Procesar imagen si existe ANTES de guardar el ejercicio
                    if (Boolean.TRUE.equals(ejercicioData.get("tieneImagen")) && ejercicioData.get("imagenBase64") != null) {
                        try {
                            String imagenBase64 = (String) ejercicioData.get("imagenBase64");
                            String mimeType = (String) ejercicioData.get("mimeType");
                            
                            if (mimeType == null) {
                                mimeType = "image/jpeg"; // Por defecto
                            }
                            
                            // Decodificar base64 y guardar en filesystem
                            byte[] imagenBytes = Base64.getDecoder().decode(imagenBase64);
                            String nombreImagen = "imagen_ejercicio_" + ejercicio.getName().replaceAll("[^a-zA-Z0-9]", "_");
                            
                            // Guardar imagen usando ImagenServicio (guarda en filesystem)
                            Imagen imagen = imagenServicio.guardar(imagenBytes, nombreImagen);
                            
                            // Asignar imagen al ejercicio
                            ejercicio.setImagen(imagen);
                            
                            ejerciciosConImagen++;
                            logger.debug("Imagen procesada para ejercicio: {} - ID imagen: {}", ejercicio.getName(), imagen.getId());
                            
                        } catch (Exception e) {
                            logger.warn("Error procesando imagen para ejercicio {}: {}", ejercicio.getName(), e.getMessage());
                            ejerciciosSinImagen++;
                            errores.add("Error en imagen para " + ejercicio.getName() + ": " + e.getMessage());
                        }
                    } else {
                        ejerciciosSinImagen++;
                    }
                    
                    // Guardar ejercicio (con o sin imagen ya asignada)
                    exerciseService.saveExercise(ejercicio, null);
                    
                    ejerciciosImportados++;
                    
                } catch (Exception e) {
                    String nombreEjercicio = ejercicioData.get("name") != null ? (String) ejercicioData.get("name") : "Desconocido";
                    logger.warn("Error importando ejercicio {}: {}", nombreEjercicio, e.getMessage());
                    errores.add("Error importando " + nombreEjercicio + ": " + e.getMessage());
                }
            }
            
                         // Resultado de la importación
             result.put("success", true);
             result.put("ejerciciosImportados", ejerciciosImportados);
             result.put("ejerciciosConImagen", ejerciciosConImagen);
             result.put("ejerciciosSinImagen", ejerciciosSinImagen);
             result.put("profesorDestino", profesorDestino.getNombre());
             result.put("archivoOrigen", fileName);
             result.put("fechaImportacion", LocalDateTime.now().toString());
            
            if (!errores.isEmpty()) {
                result.put("errores", errores);
                result.put("totalErrores", errores.size());
            }
            
                         logger.info("Importación completada exitosamente: {} ejercicios importados ({} con imagen, {} sin imagen) al profesor: {}",
                        ejerciciosImportados, ejerciciosConImagen, ejerciciosSinImagen, profesorDestino.getNombre());
            
        } catch (Exception e) {
            logger.error("Error al importar ejercicios: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error al importar ejercicios: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Lista todos los archivos de backup disponibles con información detallada
     * @return Lista de archivos de backup con metadatos
     */
    public Map<String, Object> listarBackupsDisponibles() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Path backupPath = workingDir.resolve(BACKUP_DIR);
            if (!Files.exists(backupPath)) {
                result.put("backups", new File[0]);
                result.put("total", 0);
                result.put("backupDirectory", backupPath.toAbsolutePath().toString());
                return result;
            }
            
            // Obtener archivos .json en el directorio de backups
            File[] backupFiles = backupPath.toFile().listFiles((dir, name) -> name.endsWith(".json"));
            
            if (backupFiles == null) {
                backupFiles = new File[0];
            }
            
            // Ordenar por fecha de modificación (más reciente primero)
            Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            
            // Agregar información detallada de cada archivo
            List<Map<String, Object>> backupsInfo = new ArrayList<>();
            for (File file : backupFiles) {
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("nombre", file.getName()); // Solo nombre del archivo
                fileInfo.put("tamaño", file.length());
                fileInfo.put("fechaModificacion", new java.util.Date(file.lastModified()));
                fileInfo.put("ruta", "backups/ejercicios/" + file.getName()); // Ruta simplificada
                fileInfo.put("displayName", file.getName()); // Para mostrar en el dropdown
                
                // Parsear JSON para obtener información de ejercicios
                try {
                    byte[] fileBytes = Files.readAllBytes(file.toPath());
                    String jsonContent = new String(fileBytes, "UTF-8");
                    List<Map<String, Object>> ejercicios = objectMapper.readValue(jsonContent, List.class);
                    
                    if (ejercicios != null) {
                        fileInfo.put("cantidadEjercicios", ejercicios.size());
                        fileInfo.put("ejerciciosConImagen", ejercicios.stream()
                            .filter(e -> Boolean.TRUE.equals(e.get("tieneImagen")))
                            .count());
                        fileInfo.put("ejerciciosSinImagen", ejercicios.stream()
                            .filter(e -> !Boolean.TRUE.equals(e.get("tieneImagen")))
                            .count());
                    }
                    
                } catch (Exception e) {
                    fileInfo.put("errorParseo", e.getMessage());
                }
                
                backupsInfo.add(fileInfo);
            }
            
            result.put("success", true);
            result.put("backups", backupsInfo);
            result.put("total", backupFiles.length);
            result.put("backupDirectory", backupPath.toAbsolutePath().toString());
            
            logger.info("Backups listados exitosamente: {} archivos encontrados", backupFiles.length);
            
        } catch (Exception e) {
            logger.error("Error listando backups: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error al listar backups: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Obtiene información detallada de un archivo de backup específico
     * @param fileName Nombre del archivo
     * @return Información detallada del backup
     */
    public Map<String, Object> obtenerInfoBackup(String fileName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Path filePath = workingDir.resolve(BACKUP_DIR).resolve(fileName);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("Archivo de backup no encontrado: " + fileName);
            }
            
            // Información básica del archivo
            result.put("nombre", fileName);
            result.put("tamaño", Files.size(filePath));
            result.put("fechaModificacion", new java.util.Date(Files.getLastModifiedTime(filePath).toMillis()));
            result.put("ruta", "backups/ejercicios/" + fileName); // Ruta simplificada
            
            // Leer contenido del archivo
            byte[] fileBytes = Files.readAllBytes(filePath);
            
            // Parsear JSON para obtener información de ejercicios
            try {
                String jsonContent = new String(fileBytes, "UTF-8");
                List<Map<String, Object>> ejercicios = objectMapper.readValue(jsonContent, List.class);
                
                if (ejercicios != null) {
                    result.put("cantidadEjercicios", ejercicios.size());
                    result.put("ejerciciosConImagen", ejercicios.stream()
                        .filter(e -> Boolean.TRUE.equals(e.get("tieneImagen")))
                        .count());
                    result.put("ejerciciosSinImagen", ejercicios.stream()
                        .filter(e -> !Boolean.TRUE.equals(e.get("tieneImagen")))
                        .count());
                }
                
            } catch (Exception e) {
                result.put("errorParseo", e.getMessage());
            }
            
            result.put("success", true);
            logger.info("Información del backup obtenida: {} ({} bytes)", fileName, result.get("tamaño"));
            
        } catch (Exception e) {
            logger.error("Error obteniendo info del backup: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error al obtener información del backup: " + e.getMessage());
        }
        
        return result;
    }
}
