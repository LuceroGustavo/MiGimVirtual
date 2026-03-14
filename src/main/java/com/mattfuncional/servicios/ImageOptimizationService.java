package com.mattfuncional.servicios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class ImageOptimizationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageOptimizationService.class);
    
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 600;
    private static final float COMPRESSION_QUALITY = 0.8f;
    
    // Formato preferido para almacenamiento (mantener formato original)
    private static final String PREFERRED_FORMAT = "original";
    
    /**
     * Optimiza una imagen reduciendo su tamaño y convirtiendo a formato preferido.
     * GIF y WebP no se procesan para preservar la animación (ImageIO solo lee el primer fotograma).
     */
    public byte[] optimizeImage(byte[] originalImage, String format) {
        if (format != null && ("gif".equalsIgnoreCase(format) || "webp".equalsIgnoreCase(format))) {
            logger.debug("Manteniendo GIF/WebP sin optimizar para preservar animación");
            return originalImage;
        }
        try {
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(originalImage));
            
            // Calcular nuevas dimensiones manteniendo aspect ratio
            int originalWidth = original.getWidth();
            int originalHeight = original.getHeight();
            
            int newWidth = originalWidth;
            int newHeight = originalHeight;
            
            if (originalWidth > MAX_WIDTH || originalHeight > MAX_HEIGHT) {
                double scale = Math.min(
                    (double) MAX_WIDTH / originalWidth,
                    (double) MAX_HEIGHT / originalHeight
                );
                
                newWidth = (int) (originalWidth * scale);
                newHeight = (int) (originalHeight * scale);
            }
            
            // Crear imagen redimensionada
            BufferedImage resized = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resized.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
            
            // Intentar optimizar manteniendo formato original
            byte[] optimizedImage = convertToPreferredFormat(resized, originalImage);
            
            if (optimizedImage != null) {
                logger.info("Imagen optimizada y convertida: {}x{} -> {}x{} ({} bytes -> {} bytes) [{}]", 
                    originalWidth, originalHeight, newWidth, newHeight, 
                    originalImage.length, optimizedImage.length, PREFERRED_FORMAT.toUpperCase());
                return optimizedImage;
            }
            
            // Si la conversión falla, usar formato original optimizado
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(resized, format, outputStream);
            
            byte[] fallbackImage = outputStream.toByteArray();
            logger.info("✅ Imagen optimizada (formato original {}): {}x{} -> {}x{} ({} bytes -> {} bytes)", 
                format.toUpperCase(), originalWidth, originalHeight, newWidth, newHeight, 
                originalImage.length, fallbackImage.length);
            
            return fallbackImage;
            
        } catch (IOException e) {
            logger.error("Error optimizando imagen: {}", e.getMessage(), e);
            logger.warn("⚠️ Retornando imagen original sin optimizar debido al error");
            return originalImage; // Retornar original si hay error
        } catch (Exception e) {
            logger.error("Error inesperado optimizando imagen: {}", e.getMessage(), e);
            logger.warn("⚠️ Retornando imagen original sin optimizar debido al error inesperado");
            return originalImage; // Retornar original si hay error inesperado
        }
    }
    
    /**
     * Intenta mantener el formato original y solo optimizar tamaño
     */
    private byte[] convertToPreferredFormat(BufferedImage image, byte[] originalImage) {
        try {
            logger.info("🔄 Manteniendo formato original y optimizando solo tamaño...");
            
            // NO convertir formato, solo optimizar tamaño
            // La imagen ya está redimensionada en el método principal
            return null; // Retornar null para usar el fallback del formato original
            
        } catch (Exception e) {
            logger.error("❌ Error en optimización: {}, usando formato original", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Verifica si el formato original está soportado en el sistema
     */
    public boolean isWebPSupported() {
        try {
            logger.info("🔍 Verificando soporte para mantener formato original...");
            
            // Verificar si ImageIO puede escribir formatos comunes
            String[] formats = ImageIO.getWriterFormatNames();
            logger.info("📋 Formatos de escritura disponibles: {}", String.join(", ", formats));
            
            // Verificar formatos principales
            String[] mainFormats = {"gif", "png", "jpg", "jpeg", "bmp"};
            for (String format : mainFormats) {
                for (String availableFormat : formats) {
                    if (availableFormat.equalsIgnoreCase(format)) {
                        logger.info("✅ Formato {} soportado: {}", format, availableFormat);
                    }
                }
            }
            
            logger.info("✅ Sistema configurado para mantener formato original");
            return true;
            
        } catch (Exception e) {
            logger.error("❌ Error verificando formatos: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Intenta registrar manualmente el soporte WebP
     */
    private boolean tryRegisterWebPSupport() {
        try {
            logger.info("🔄 Intentando registrar soporte WebP manualmente...");
            
            // Intentar cargar clases de WebP si están disponibles
            try {
                // Verificar si las dependencias de WebP están en el classpath
                Class.forName("com.luciad.imageio.webp.WebPImageReader");
                Class.forName("com.luciad.imageio.webp.WebPImageWriter");
                
                logger.info("✅ Clases WebP encontradas en classpath");
                return true;
                
            } catch (ClassNotFoundException e) {
                logger.debug("Clases WebP no encontradas: {}", e.getMessage());
            }
            
            // Intentar con otras implementaciones
            try {
                Class.forName("org.sejda.imageio.webp.WebPImageReader");
                Class.forName("org.sejda.imageio.webp.WebPImageWriter");
                
                logger.info("✅ Clases WebP alternativas encontradas en classpath");
                return true;
                
            } catch (ClassNotFoundException e) {
                logger.debug("Clases WebP alternativas no encontradas: {}", e.getMessage());
            }
            
            logger.warn("⚠️ No se pudo registrar soporte WebP manualmente");
            return false;
            
        } catch (Exception e) {
            logger.error("❌ Error registrando soporte WebP: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida que el archivo WebP generado sea válido
     */
    private boolean isValidWebP(byte[] webpData) {
        if (webpData.length < 12) return false;
        
        // Verificar firma WebP: RIFF....WEBP
        return webpData[0] == 'R' && webpData[1] == 'I' && 
               webpData[2] == 'F' && webpData[3] == 'F' &&
               webpData[8] == 'W' && webpData[9] == 'E' && 
               webpData[10] == 'B' && webpData[11] == 'P';
    }
    
    /**
     * Optimiza un archivo MultipartFile. GIF/WebP se devuelven sin tocar para preservar animación.
     */
    public byte[] optimizeMultipartFile(MultipartFile file) {
        byte[] originalBytes;
        try {
            originalBytes = file.getBytes();
        } catch (IOException e) {
            logger.error("Error leyendo archivo: {}", e.getMessage());
            throw new RuntimeException("No se pudo leer el archivo: " + e.getMessage(), e);
        }
        String format = getImageFormat(file.getOriginalFilename());
        logger.debug("Procesando archivo: {} ({} bytes, formato: {})",
                file.getOriginalFilename(), originalBytes.length, format);
        if ("gif".equalsIgnoreCase(format) || "webp".equalsIgnoreCase(format)) {
            return originalBytes;
        }
        byte[] optimized = optimizeImage(originalBytes, format);
        if (optimized == null || optimized.length == 0) {
            logger.warn("⚠️ La optimización falló, usando imagen original sin procesar");
            return originalBytes;
        }
        return optimized;
    }

    /**
     * Determina el formato de imagen basado en la extensión del archivo
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
     * Verifica si un archivo es una imagen válida
     */
    public boolean isValidImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return false;
        }
        
        // Verificar extensión del archivo
        String filename = file.getOriginalFilename();
        if (filename != null) {
            String extension = getImageFormat(filename);
            return !extension.equals("jpg"); // Si no es jpg, debe ser un formato válido
        }
        
        return true;
    }
    
    /**
     * Obtiene las dimensiones de una imagen
     */
    public Dimension getImageDimensions(byte[] imageBytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image != null) {
                return new Dimension(image.getWidth(), image.getHeight());
            }
        } catch (IOException e) {
            logger.error("Error obteniendo dimensiones de imagen: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Obtiene el formato preferido para almacenamiento
     */
    public String getPreferredFormat() {
        return PREFERRED_FORMAT;
    }
    
    /**
     * Verifica si el formato de entrada es compatible con conversión a WebP
     */
    public boolean isCompatibleWithWebP(String inputFormat) {
        return inputFormat.equalsIgnoreCase("png") || 
               inputFormat.equalsIgnoreCase("gif") || 
               inputFormat.equalsIgnoreCase("jpg") ||
               inputFormat.equalsIgnoreCase("jpeg") ||
               inputFormat.equalsIgnoreCase("bmp");
    }
} 