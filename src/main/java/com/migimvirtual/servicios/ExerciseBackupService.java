package com.migimvirtual.servicios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.migimvirtual.entidades.Exercise;
import com.migimvirtual.entidades.Profesor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class ExerciseBackupService {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseBackupService.class);
    
    private final ExerciseService exerciseService;
    private final ProfesorService profesorService;
    private final ObjectMapper objectMapper;
    
    // Directorio para backups (se creará automáticamente)
    private static final String BACKUP_DIR = "backups/ejercicios";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public ExerciseBackupService(ExerciseService exerciseService, ProfesorService profesorService) {
        this.exerciseService = exerciseService;
        this.profesorService = profesorService;
        
        // Configurar ObjectMapper
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Exporta ejercicios predeterminados del sistema a un archivo JSON local
     * @return Información del backup creado
     */
    @Transactional(readOnly = true)
    public Map<String, Object> exportarEjerciciosLocal() {
        return exportarEjerciciosLocal(null); // Por defecto exporta ejercicios predeterminados del sistema
    }

    /**
     * Exporta ejercicios a un archivo JSON local
     * @param profesorId ID del profesor (null para ejercicios predeterminados del sistema)
     * @return Información del backup creado
     */
    @Transactional(readOnly = true)
    public Map<String, Object> exportarEjerciciosLocal(Long profesorId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Profesor profesor = null;
            String nombreArchivo;
            List<Exercise> ejercicios;
            
            if (profesorId == null) {
                // Exportar ejercicios PREDETERMINADOS del sistema (profesor = null)
                nombreArchivo = "ejercicios_predeterminados";
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
                nombreArchivo = "ejercicios_prof_" + profesor.getNombre().toLowerCase().replace(" ", "_");
                ejercicios = exerciseService.findEjerciciosPropiosDelProfesor(profesorId);
                if (ejercicios.isEmpty()) {
                    throw new RuntimeException("No hay ejercicios propios para exportar del profesor: " + profesor.getNombre());
                }
            }
            
            // Crear nombre de archivo con timestamp
            String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
            String fileName = String.format("%s_%s.json", nombreArchivo, timestamp);
            Path filePath = Paths.get(BACKUP_DIR, fileName);
            
            // Convertir a JSON
            String jsonContent = objectMapper.writeValueAsString(ejercicios);
            
            // Escribir archivo
            Files.write(filePath, jsonContent.getBytes());
            
            // Obtener información del archivo
            long fileSize = Files.size(filePath);
            
            result.put("success", true);
            result.put("message", "Ejercicios exportados exitosamente");
            result.put("fileName", fileName);
            result.put("filePath", filePath.toAbsolutePath().toString());
            result.put("fileSize", fileSize);
            result.put("ejerciciosExportados", ejercicios.size());
            if (profesor != null) {
                result.put("profesorOrigen", profesor.getNombre() + (profesor.getApellido() != null ? " " + profesor.getApellido() : ""));
                result.put("profesorOrigenId", profesor.getId());
            } else {
                result.put("profesorOrigen", "Sistema (Predeterminados)");
                result.put("profesorOrigenId", null);
            }
            result.put("timestamp", timestamp);
            
            String origenInfo = profesor != null ? profesor.getNombre() : "Sistema (Predeterminados)";
            logger.info("Backup creado exitosamente: {} ({} ejercicios, {} bytes) desde: {}", 
                       fileName, ejercicios.size(), fileSize, origenInfo);
            
        } catch (Exception e) {
            logger.error("Error en exportación local: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error al exportar ejercicios: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Restaura ejercicios predeterminados desde un archivo JSON local
     * @param fileName Nombre del archivo a restaurar
     * @return Información de la restauración
     */
    @Transactional
    public Map<String, Object> restaurarEjerciciosLocal(String fileName) {
        return restaurarEjerciciosLocal(fileName, null); // Por defecto restaura como ejercicios predeterminados del sistema
    }

    /**
     * Restaura ejercicios desde un archivo JSON local
     * @param fileName Nombre del archivo a restaurar
     * @param profesorDestinoId ID del profesor destino (null para restaurar como predeterminados del sistema)
     * @return Información de la restauración
     */
    @Transactional
    public Map<String, Object> restaurarEjerciciosLocal(String fileName, Long profesorDestinoId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Verificar que el archivo existe
            Path filePath = Paths.get(BACKUP_DIR, fileName);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("Archivo de backup no encontrado: " + fileName);
            }
            
            // Leer archivo JSON
            String jsonContent = Files.readString(filePath);
            
            // Parsear JSON a lista de ejercicios
            List<Exercise> ejercicios = objectMapper.readValue(jsonContent, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Exercise.class));
            
            if (ejercicios.isEmpty()) {
                throw new RuntimeException("El archivo de backup no contiene ejercicios válidos");
            }
            
            Profesor profesorDestino = null;
            boolean esPredeterminado = (profesorDestinoId == null);
            
            if (!esPredeterminado) {
                // Restaurar a profesor específico (ejercicios propios del profesor)
                profesorDestino = profesorService.getProfesorById(profesorDestinoId);
                if (profesorDestino == null) {
                    throw new RuntimeException("No se encontró el profesor destino con ID: " + profesorDestinoId);
                }
                
                // Limpiar ejercicios PROPIOS existentes del profesor (no predeterminados)
                List<Exercise> ejerciciosExistentes = exerciseService.findEjerciciosPropiosDelProfesor(profesorDestinoId);
                for (Exercise ejercicio : ejerciciosExistentes) {
                    exerciseService.deleteExercise(ejercicio.getId());
                }
                logger.info("Ejercicios propios del profesor {} eliminados antes de la restauración", 
                           profesorDestino.getNombre());
            } else {
                // Restaurar como ejercicios PREDETERMINADOS del sistema
                // Limpiar ejercicios predeterminados existentes
                List<Exercise> ejerciciosPredeterminados = exerciseService.findEjerciciosPredeterminados();
                for (Exercise ejercicio : ejerciciosPredeterminados) {
                    exerciseService.deleteExercise(ejercicio.getId());
                }
                logger.info("Ejercicios predeterminados existentes eliminados antes de la restauración");
            }
            
            // Restaurar ejercicios
            int ejerciciosRestaurados = 0;
            for (Exercise ejercicio : ejercicios) {
                try {
                    // Resetear ID
                    ejercicio.setId(null);
                    
                    if (esPredeterminado) {
                        // Restaurar como predeterminado (profesor = null, esPredeterminado = true)
                        ejercicio.setProfesor(null);
                        ejercicio.setEsPredeterminado(true);
                    } else {
                        // Restaurar como ejercicio propio del profesor
                        ejercicio.setProfesor(profesorDestino);
                        ejercicio.setEsPredeterminado(false);
                    }
                    
                    // Guardar ejercicio (sin imagen por ahora, se restaurará solo la estructura)
                    ejercicio.setImagen(null); // Resetear imagen para evitar problemas
                    exerciseService.saveExercise(ejercicio, null);
                    ejerciciosRestaurados++;
                    
                } catch (Exception e) {
                    logger.warn("Error restaurando ejercicio {}: {}", ejercicio.getName(), e.getMessage());
                }
            }
            
            result.put("success", true);
            result.put("message", "Ejercicios restaurados exitosamente");
            result.put("fileName", fileName);
            result.put("ejerciciosRestaurados", ejerciciosRestaurados);
            result.put("ejerciciosEnArchivo", ejercicios.size());
            if (profesorDestino != null) {
                result.put("profesorDestino", profesorDestino.getNombre() + (profesorDestino.getApellido() != null ? " " + profesorDestino.getApellido() : ""));
                result.put("profesorDestinoId", profesorDestino.getId());
            } else {
                result.put("profesorDestino", "Sistema (Predeterminados)");
                result.put("profesorDestinoId", null);
            }
            result.put("esPredeterminado", esPredeterminado);
            result.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
            
            String destinoInfo = profesorDestino != null ? profesorDestino.getNombre() : "Sistema (Predeterminados)";
            logger.info("Restauración completada: {} ejercicios restaurados desde {} a: {}", 
                       ejerciciosRestaurados, fileName, destinoInfo);
            
        } catch (Exception e) {
            logger.error("Error en restauración local: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error al restaurar ejercicios: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Lista todos los archivos de backup disponibles
     * @return Lista de archivos de backup
     */
    public Map<String, Object> listarBackupsDisponibles() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Path backupPath = Paths.get(BACKUP_DIR);
            if (!Files.exists(backupPath)) {
                result.put("success", true);
                result.put("backups", new File[0]);
                result.put("total", 0);
                return result;
            }
            
            // Obtener archivos .json en el directorio de backups
            File[] backupFiles = backupPath.toFile().listFiles((dir, name) -> name.endsWith(".json"));
            
            if (backupFiles == null) {
                backupFiles = new File[0];
            }
            
            // Ordenar por fecha de modificación (más reciente primero)
            java.util.Arrays.sort(backupFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            
            result.put("success", true);
            result.put("backups", backupFiles);
            result.put("total", backupFiles.length);
            result.put("backupDirectory", backupPath.toAbsolutePath().toString());
            
        } catch (Exception e) {
            logger.error("Error listando backups: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error al listar backups: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Elimina un archivo de backup específico
     * @param fileName Nombre del archivo a eliminar
     * @return Resultado de la eliminación
     */
    public Map<String, Object> eliminarBackup(String fileName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Path filePath = Paths.get(BACKUP_DIR, fileName);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("Archivo de backup no encontrado: " + fileName);
            }
            
            // Eliminar archivo
            Files.delete(filePath);
            
            result.put("success", true);
            result.put("message", "Backup eliminado exitosamente");
            result.put("fileName", fileName);
            
            logger.info("Backup eliminado: {}", fileName);
            
        } catch (Exception e) {
            logger.error("Error eliminando backup: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error al eliminar backup: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Obtiene información detallada de un archivo de backup
     * @param fileName Nombre del archivo
     * @return Información del backup
     */
    public Map<String, Object> obtenerInfoBackup(String fileName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Path filePath = Paths.get(BACKUP_DIR, fileName);
            if (!Files.exists(filePath)) {
                throw new RuntimeException("Archivo de backup no encontrado: " + fileName);
            }
            
            // Leer archivo para obtener información
            String jsonContent = Files.readString(filePath);
            List<Exercise> ejercicios = objectMapper.readValue(jsonContent, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, Exercise.class));
            
            // Obtener estadísticas del archivo
            long fileSize = Files.size(filePath);
            long lastModified = Files.getLastModifiedTime(filePath).toMillis();
            
            result.put("success", true);
            result.put("fileName", fileName);
            result.put("fileSize", fileSize);
            result.put("lastModified", lastModified);
            result.put("ejerciciosCount", ejercicios.size());
            result.put("filePath", filePath.toAbsolutePath().toString());
            
            // Información adicional de ejercicios
            if (!ejercicios.isEmpty()) {
                result.put("primerEjercicio", ejercicios.get(0).getName());
                result.put("ultimoEjercicio", ejercicios.get(ejercicios.size() - 1).getName());
            }
            
        } catch (Exception e) {
            logger.error("Error obteniendo info del backup: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error al obtener información del backup: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * Obtiene lista de profesores disponibles para exportación/importación
     * @return Lista de profesores con información básica
     */
    public Map<String, Object> obtenerProfesoresParaBackup() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            List<Profesor> profesores = profesorService.getAllProfesores();
            
            List<Map<String, Object>> profesoresInfo = profesores.stream()
                .map(p -> {
                    Map<String, Object> profInfo = new HashMap<>();
                    profInfo.put("id", p.getId());
                    profInfo.put("nombre", p.getNombre() + " " + p.getApellido());
                    profInfo.put("correo", p.getCorreo());
                    profInfo.put("esProfesorPrincipal", "profesor@migimvirtual.com".equals(p.getCorreo()));
                    
                    // Contar ejercicios PROPIOS del profesor (excluyendo predeterminados)
                    try {
                        List<Exercise> ejercicios = exerciseService.findEjerciciosPropiosDelProfesor(p.getId());
                        profInfo.put("ejerciciosCount", ejercicios.size());
                    } catch (Exception e) {
                        profInfo.put("ejerciciosCount", 0);
                    }
                    
                    return profInfo;
                })
                .collect(Collectors.toList());
            
            result.put("success", true);
            result.put("profesores", profesoresInfo);
            result.put("total", profesoresInfo.size());
            
            logger.info("Profesores obtenidos para backup: {}", profesoresInfo.size());
            
        } catch (Exception e) {
            logger.error("Error obteniendo profesores para backup: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "Error al obtener profesores: " + e.getMessage());
        }
        
        return result;
    }
}
