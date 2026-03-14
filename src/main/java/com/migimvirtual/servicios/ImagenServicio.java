package com.migimvirtual.servicios;

import com.migimvirtual.entidades.Imagen;
import com.migimvirtual.excepciones.ResourceNotFoundException;
import com.migimvirtual.repositorios.ImagenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ImagenServicio {

    private static final Logger logger = LoggerFactory.getLogger(ImagenServicio.class);
    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB máximo (aumentado desde 1MB)

    private final ImagenRepository imagenRepository;
    private final ImageOptimizationService imageOptimizationService;
    private final String uploadsDir;
    private final String ejerciciosDir;

    public ImagenServicio(
            ImagenRepository imagenRepository,
            ImageOptimizationService imageOptimizationService,
            @Value("${migimvirtual.uploads.dir:uploads}") String uploadsDir,
            @Value("${migimvirtual.uploads.ejercicios:ejercicios}") String ejerciciosDir) {
        this.imagenRepository = imagenRepository;
        this.imageOptimizationService = imageOptimizationService;
        this.uploadsDir = uploadsDir;
        this.ejerciciosDir = ejerciciosDir;
        
        // Crear estructura de directorios al inicializar
        inicializarDirectorios();
    }

    /**
     * Inicializa la estructura de directorios necesaria
     */
    private void inicializarDirectorios() {
        try {
            Path baseDir = Paths.get(uploadsDir, ejerciciosDir);
            if (!Files.exists(baseDir)) {
                Files.createDirectories(baseDir);
                logger.info("Directorio de imágenes creado: {}", baseDir.toAbsolutePath());
            }
        } catch (IOException e) {
            logger.error("Error al crear directorios de imágenes: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo inicializar el sistema de almacenamiento de imágenes", e);
        }
    }

    /**
     * Guarda una imagen subida por el usuario (ejercicio creado/editado por profesor).
     * El archivo se guarda con nombre único en uploads/ejercicios/ para no pisar los predeterminados (1.webp…60)
     * ni colisionar con otras subidas: formato "nombreSanitizado_8charsUUID.ext" (ej. curl_biceps_a1b2c3d4.jpg).
     */
    public Imagen guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío o es nulo");
        }
        
        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo es demasiado grande. Máximo " + (MAX_FILE_SIZE / 1024 / 1024) + "MB permitido.");
        }
        
        return guardarImagen(archivo);
    }

    /**
     * Guarda una imagen desde byte array (para ejercicios predeterminados)
     */
    public Imagen guardar(byte[] archivoBytes, String nombreArchivo) {
        if (archivoBytes == null || archivoBytes.length == 0) {
            throw new IllegalArgumentException("El archivo está vacío o es nulo");
        }
        
        if (archivoBytes.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo es demasiado grande. Máximo " + (MAX_FILE_SIZE / 1024 / 1024) + "MB permitido.");
        }
        
        try {
            // Obtener formato del nombre de archivo
            String formato = getImageFormat(nombreArchivo);
            String mimeType = getMimeType(formato);
            
            // No optimizar GIF ni WebP: ImageIO.read() solo lee el primer fotograma y pierde la animación.
            // Guardar bytes originales para que GIF/WebP animados sigan en movimiento.
            byte[] optimizedBytes;
            if ("gif".equalsIgnoreCase(formato) || "webp".equalsIgnoreCase(formato)) {
                optimizedBytes = archivoBytes;
            } else {
                optimizedBytes = imageOptimizationService.optimizeImage(archivoBytes, formato);
                if (optimizedBytes == null || optimizedBytes.length == 0) {
                    optimizedBytes = archivoBytes;
                }
            }
            
            // Determinar formato final (sin cambiar gif/webp para no tocar animación)
            String formatoFinal = ("gif".equalsIgnoreCase(formato) || "webp".equalsIgnoreCase(formato))
                ? formato : determinarFormatoFinal(optimizedBytes, formato);
            mimeType = getMimeType(formatoFinal);
            
            // Generar ruta y guardar archivo
            String rutaArchivo = generarRutaArchivo(nombreArchivo, formatoFinal);
            Path archivoPath = Paths.get(uploadsDir, ejerciciosDir, rutaArchivo);
            
            // Crear directorios si no existen
            Files.createDirectories(archivoPath.getParent());
            
            // Guardar archivo en filesystem
            Files.write(archivoPath, optimizedBytes);
            
            // Crear entidad Imagen con metadatos
            Imagen imagen = new Imagen();
            imagen.setMime(mimeType);
            imagen.setNombre(generarNombreArchivo(nombreArchivo, formatoFinal));
            imagen.setRutaArchivo(rutaArchivo);
            imagen.setTamanoBytes((long) optimizedBytes.length);
            
            Imagen savedImagen = imagenRepository.save(imagen);
            
            logger.info("Imagen guardada en filesystem: {} ({} bytes) [{}]", 
                savedImagen.getRutaArchivo(), optimizedBytes.length, formatoFinal.toUpperCase());
            
            return savedImagen;
            
        } catch (Exception e) {
            logger.error("Error al guardar imagen desde bytes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage(), e);
        }
    }

    /**
     * Guarda una imagen para restore de backup, usando el nombre original del archivo en el ZIP
     * (ej: "imagenes/1.webp" → guarda como "1.webp"). Preserva el formato y evita ejercicio_0, etc.
     * @param archivoBytes contenido de la imagen
     * @param rutaEnZip ruta en el ZIP (ej: "imagenes/1.webp" o "imagenes/ejercicio_0.jpg")
     */
    public Imagen guardarParaRestore(byte[] archivoBytes, String rutaEnZip) {
        if (archivoBytes == null || archivoBytes.length == 0) {
            throw new IllegalArgumentException("El archivo está vacío o es nulo");
        }
        if (archivoBytes.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo es demasiado grande. Máximo " + (MAX_FILE_SIZE / 1024 / 1024) + "MB permitido.");
        }
        if (rutaEnZip == null || rutaEnZip.isBlank()) {
            throw new IllegalArgumentException("La ruta en ZIP no puede ser nula");
        }
        try {
            String nombreArchivo = rutaEnZip.contains("/") ? rutaEnZip.substring(rutaEnZip.lastIndexOf('/') + 1) : rutaEnZip;
            String formato = getImageFormat(nombreArchivo);
            byte[] optimizedBytes = archivoBytes;
            if (!"gif".equalsIgnoreCase(formato) && !"webp".equalsIgnoreCase(formato) && imageOptimizationService != null) {
                byte[] opt = imageOptimizationService.optimizeImage(archivoBytes, formato);
                if (opt != null && opt.length > 0) optimizedBytes = opt;
            }
            String formatoFinal = ("gif".equalsIgnoreCase(formato) || "webp".equalsIgnoreCase(formato))
                ? formato : determinarFormatoFinal(optimizedBytes, formato);
            String mimeType = getMimeType(formatoFinal);

            String rutaArchivo = nombreArchivo.contains(".") ? nombreArchivo : (nombreArchivo + "." + formatoFinal);
            Path archivoPath = Paths.get(uploadsDir, ejerciciosDir, rutaArchivo);
            Path parent = archivoPath.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.write(archivoPath, optimizedBytes);

            Imagen imagen = new Imagen();
            imagen.setMime(mimeType);
            imagen.setNombre(rutaArchivo);
            imagen.setRutaArchivo(rutaArchivo);
            imagen.setTamanoBytes((long) optimizedBytes.length);

            Imagen savedImagen = imagenRepository.save(imagen);
            logger.info("Imagen guardada para restore: {} ({} bytes) [{}]", rutaArchivo, optimizedBytes.length, formatoFinal.toUpperCase());
            return savedImagen;
        } catch (Exception e) {
            logger.error("Error al guardar imagen para restore: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar la imagen para restore: " + e.getMessage(), e);
        }
    }

    /**
     * Guarda una imagen desde MultipartFile (método principal)
     */
    private Imagen guardarImagen(MultipartFile archivo) {
        try {
            logger.debug("Procesando imagen: {} ({} bytes)", archivo.getOriginalFilename(), archivo.getSize());
            
            String formatoOriginal = getImageFormat(archivo.getOriginalFilename());
            logger.debug("Formato original detectado: {}", formatoOriginal);
            
            // No optimizar GIF ni WebP para preservar animación (ImageIO solo usa el primer fotograma)
            byte[] optimizedBytes;
            if ("gif".equalsIgnoreCase(formatoOriginal) || "webp".equalsIgnoreCase(formatoOriginal)) {
                optimizedBytes = archivo.getBytes();
            } else {
                optimizedBytes = imageOptimizationService.optimizeMultipartFile(archivo);
            }
            if (optimizedBytes == null || optimizedBytes.length == 0) {
                throw new RuntimeException("Error al procesar la imagen");
            }
            
            String formatoFinal = ("gif".equalsIgnoreCase(formatoOriginal) || "webp".equalsIgnoreCase(formatoOriginal))
                ? formatoOriginal : determinarFormatoFinal(optimizedBytes, formatoOriginal);
            String mimeType = getMimeType(formatoFinal);
            
            // Generar ruta y guardar archivo
            String rutaArchivo = generarRutaArchivo(archivo.getOriginalFilename(), formatoFinal);
            Path archivoPath = Paths.get(uploadsDir, ejerciciosDir, rutaArchivo);
            
            // Crear directorios si no existen
            Files.createDirectories(archivoPath.getParent());
            
            // Guardar archivo en filesystem
            Files.write(archivoPath, optimizedBytes);
            
            // Crear entidad Imagen con metadatos
            Imagen imagen = new Imagen();
            imagen.setMime(mimeType);
            imagen.setNombre(generarNombreArchivo(archivo.getOriginalFilename(), formatoFinal));
            imagen.setRutaArchivo(rutaArchivo);
            imagen.setTamanoBytes((long) optimizedBytes.length);
            
            Imagen savedImagen = imagenRepository.save(imagen);
            
            logger.info("Imagen guardada exitosamente: {} ({} bytes -> {} bytes) [{} -> {}]", 
                archivo.getOriginalFilename(), archivo.getSize(), optimizedBytes.length, 
                formatoOriginal.toUpperCase(), formatoFinal.toUpperCase());
            
            return savedImagen;
            
        } catch (Exception e) {
            logger.error("Error al guardar la imagen: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar la imagen: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza una imagen existente
     */
    public Imagen actualizar(MultipartFile archivo, String idImagen) {
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo está vacío o es nulo");
        }

        if (archivo.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("El archivo es demasiado grande. Máximo " + (MAX_FILE_SIZE / 1024 / 1024) + "MB permitido.");
        }

        Imagen imagen = imagenRepository.findById(idImagen)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + idImagen));
        
        try {
            // Eliminar archivo antiguo
            eliminarArchivoFisico(imagen.getRutaArchivo());
            
            logger.debug("Actualizando imagen: {} ({} bytes)", archivo.getOriginalFilename(), archivo.getSize());
            
            // Obtener formato original
            String formatoOriginal = getImageFormat(archivo.getOriginalFilename());
            logger.debug("Formato original detectado: {}", formatoOriginal);
            
            // Optimizar imagen
            byte[] optimizedBytes = imageOptimizationService.optimizeMultipartFile(archivo);
            
            if (optimizedBytes.length == 0) {
                throw new RuntimeException("Error al procesar la imagen");
            }
            
            // Determinar formato final y MIME type
            String formatoFinal = determinarFormatoFinal(optimizedBytes, formatoOriginal);
            String mimeType = getMimeType(formatoFinal);
            
            // Generar nueva ruta y guardar archivo
            String rutaArchivo = generarRutaArchivo(archivo.getOriginalFilename(), formatoFinal);
            Path archivoPath = Paths.get(uploadsDir, ejerciciosDir, rutaArchivo);
            
            // Crear directorios si no existen
            Files.createDirectories(archivoPath.getParent());
            
            // Guardar archivo en filesystem
            Files.write(archivoPath, optimizedBytes);
            
            // Actualizar metadatos
            imagen.setMime(mimeType);
            imagen.setNombre(generarNombreArchivo(archivo.getOriginalFilename(), formatoFinal));
            imagen.setRutaArchivo(rutaArchivo);
            imagen.setTamanoBytes((long) optimizedBytes.length);
            
            Imagen updatedImagen = imagenRepository.save(imagen);
            
            logger.info("Imagen actualizada exitosamente: {} ({} bytes -> {} bytes) [{} -> {}]", 
                archivo.getOriginalFilename(), archivo.getSize(), optimizedBytes.length, 
                formatoOriginal.toUpperCase(), formatoFinal.toUpperCase());
            
            return updatedImagen;
            
        } catch (Exception e) {
            logger.error("Error al actualizar la imagen: {}", e.getMessage(), e);
            throw new RuntimeException("Error al actualizar la imagen: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina una imagen (tanto de BD como del filesystem)
     */
    public void eliminarImagen(String idImagen) {
        Imagen imagen = imagenRepository.findById(idImagen)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + idImagen));
        
        // Eliminar archivo físico
        eliminarArchivoFisico(imagen.getRutaArchivo());
        
        // Eliminar de BD
        imagenRepository.delete(imagen);
        
        logger.info("Imagen eliminada: {} (archivo: {})", idImagen, imagen.getRutaArchivo());
    }

    /**
     * Elimina el archivo físico del filesystem
     */
    private void eliminarArchivoFisico(String rutaArchivo) {
        try {
            Path archivoPath = Paths.get(uploadsDir, ejerciciosDir, rutaArchivo);
            if (Files.exists(archivoPath)) {
                Files.delete(archivoPath);
                logger.debug("Archivo físico eliminado: {}", archivoPath);
            }
        } catch (IOException e) {
            logger.warn("No se pudo eliminar el archivo físico: {}", rutaArchivo, e);
        }
    }

    /**
     * Obtiene el contenido de una imagen desde el filesystem
     */
    public byte[] obtenerContenido(String idImagen) {
        Imagen imagen = imagenRepository.findById(idImagen)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + idImagen));
        
        try {
            // Construir ruta completa: uploads/ejercicios/{rutaArchivo}
            // rutaArchivo debería ser solo el nombre del archivo (ej: "curl_de_b_ceps_47ef5f80.webp")
            Path archivoPath = Paths.get(uploadsDir, ejerciciosDir, imagen.getRutaArchivo());
            Path archivoPathAbsoluto = archivoPath.toAbsolutePath();
            
            logger.info("🔍 Buscando archivo físico:");
            logger.info("   - Ruta relativa en BD: {}", imagen.getRutaArchivo());
            logger.info("   - Ruta completa construida: {}", archivoPath);
            logger.info("   - Ruta absoluta: {}", archivoPathAbsoluto);
            logger.info("   - Directorio base: {}", Paths.get(uploadsDir, ejerciciosDir).toAbsolutePath());
            
            if (!Files.exists(archivoPath)) {
                logger.error("❌ Archivo físico NO encontrado en: {}", archivoPathAbsoluto);
                // Intentar listar archivos en el directorio para debugging
                try {
                    Path dir = archivoPath.getParent();
                    if (Files.exists(dir)) {
                        logger.info("📁 Archivos en el directorio {}:", dir.toAbsolutePath());
                        Files.list(dir).forEach(f -> logger.info("   - {}", f.getFileName()));
                    } else {
                        logger.error("❌ El directorio {} no existe", dir.toAbsolutePath());
                    }
                } catch (Exception e) {
                    logger.warn("No se pudo listar archivos del directorio: {}", e.getMessage());
                }
                throw new ResourceNotFoundException("Archivo físico no encontrado: " + archivoPathAbsoluto);
            }
            
            byte[] contenido = Files.readAllBytes(archivoPath);
            logger.info("✅ Archivo físico encontrado y leído: {} bytes desde {}", contenido.length, archivoPathAbsoluto);
            return contenido;
        } catch (IOException e) {
            logger.error("❌ Error al leer archivo de imagen: {}", e.getMessage(), e);
            throw new RuntimeException("Error al leer la imagen: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene el contenido de la imagen si el archivo físico existe. No lanza si no existe (para backup: no romper export).
     * @return bytes de la imagen o null si la imagen no existe en BD o el archivo no está en disco
     */
    public byte[] obtenerContenidoSiExiste(String idImagen) {
        if (idImagen == null || idImagen.isBlank()) return null;
        try {
            return obtenerContenido(idImagen);
        } catch (Exception e) {
            logger.warn("Imagen no disponible para export (id={}): {}", idImagen, e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene la ruta física completa de una imagen
     */
    public Path obtenerRutaFisica(String idImagen) {
        Imagen imagen = imagenRepository.findById(idImagen)
                .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + idImagen));
        return Paths.get(uploadsDir, ejerciciosDir, imagen.getRutaArchivo());
    }

    @Transactional(readOnly = true)
    public List<Imagen> listarTodos() {
        return imagenRepository.findAll();
    }

    /**
     * Registra en BD un archivo que ya existe en uploads/ejercicios/ (sin copiar bytes).
     * Útil para ejercicios predeterminados: tú copias 1.webp..60.webp y el sistema solo crea el registro.
     * @param nombreArchivo nombre del archivo en uploads/ejercicios/ (ej. "1.webp", "2.gif")
     * @return Imagen guardada o null si el archivo no existe
     */
    @Transactional
    public Imagen registrarArchivoExistente(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) return null;
        Path path = Paths.get(uploadsDir, ejerciciosDir, nombreArchivo);
        try {
            if (!Files.exists(path) || !Files.isRegularFile(path)) return null;
            String formato = getImageFormat(nombreArchivo);
            String mime = getMimeType(formato);
            Imagen img = new Imagen();
            img.setRutaArchivo(nombreArchivo);
            img.setMime(mime);
            img.setNombre(nombreArchivo);
            img.setTamanoBytes(Files.size(path));
            return imagenRepository.save(img);
        } catch (IOException e) {
            logger.debug("No se pudo registrar archivo existente {}: {}", nombreArchivo, e.getMessage());
            return null;
        }
    }

    /**
     * Genera nombre de archivo único para subidas de usuario (evita pisar 1.webp…60 de predeterminados).
     * Formato: nombreSanitizado_8charsUUID.ext (ej. 1_1b637bae.jpg, curl_biceps_a1b2c3d4.webp).
     */
    private String generarRutaArchivo(String nombreOriginal, String formato) {
        String nombreUnico = UUID.randomUUID().toString().substring(0, 8);
        String nombreSinExtension = sanitizarNombre(nombreOriginal);
        if (nombreSinExtension.length() > 50) {
            nombreSinExtension = nombreSinExtension.substring(0, 50);
        }
        
        // Retornar solo el nombre del archivo, sin carpetas de fecha
        return String.format("%s_%s.%s", nombreSinExtension, nombreUnico, formato);
    }

    /**
     * Sanitiza el nombre del archivo para evitar problemas de seguridad
     */
    private String sanitizarNombre(String nombre) {
        if (nombre == null) {
            return "imagen";
        }
        // Remover extensión
        int lastDot = nombre.lastIndexOf('.');
        if (lastDot > 0) {
            nombre = nombre.substring(0, lastDot);
        }
        // Reemplazar caracteres especiales
        return nombre.replaceAll("[^a-zA-Z0-9_\\-]", "_").toLowerCase();
    }

    /**
     * Obtiene el formato de imagen basado en la extensión del archivo
     */
    private String getImageFormat(String filename) {
        if (filename == null) return "jpg";
        
        String extension = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        
        switch (extension) {
            case "png": return "png";
            case "gif": return "gif";
            case "bmp": return "bmp";
            case "webp": return "webp";
            case "jpg":
            case "jpeg": return "jpg";
            default: return "jpg";
        }
    }
    
    /**
     * Determina el formato final de la imagen después de la optimización
     */
    private String determinarFormatoFinal(byte[] optimizedBytes, String formatoOriginal) {
        // Verificar si la imagen fue convertida a WebP
        if (imageOptimizationService.isWebPSupported() && 
            imageOptimizationService.isCompatibleWithWebP(formatoOriginal)) {
            
            if (isValidWebP(optimizedBytes)) {
                return "webp";
            }
        }
        
        return formatoOriginal;
    }
    
    /**
     * Valida que el archivo WebP generado sea válido
     */
    private boolean isValidWebP(byte[] webpData) {
        if (webpData.length < 12) return false;
        
        return webpData[0] == 'R' && webpData[1] == 'I' && 
               webpData[2] == 'F' && webpData[3] == 'F' &&
               webpData[8] == 'W' && webpData[9] == 'E' && 
               webpData[10] == 'B' && webpData[11] == 'P';
    }
    
    /**
     * Obtiene el MIME type correspondiente al formato
     */
    private String getMimeType(String formato) {
        switch (formato.toLowerCase()) {
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "bmp": return "image/bmp";
            case "webp": return "image/webp";
            case "jpg":
            case "jpeg": return "image/jpeg";
            default: return "image/jpeg";
        }
    }
    
    /**
     * Genera un nombre de archivo con la extensión correcta
     */
    private String generarNombreArchivo(String nombreOriginal, String formatoFinal) {
        if (nombreOriginal == null) {
            return "imagen." + formatoFinal;
        }
        
        String extensionOriginal = getImageFormat(nombreOriginal);
        if (!extensionOriginal.equalsIgnoreCase(formatoFinal)) {
            String nombreSinExtension = nombreOriginal;
            int lastDotIndex = nombreOriginal.lastIndexOf(".");
            if (lastDotIndex > 0) {
                nombreSinExtension = nombreOriginal.substring(0, lastDotIndex);
            }
            return nombreSinExtension + "." + formatoFinal;
        }
        
        return nombreOriginal;
    }
}
