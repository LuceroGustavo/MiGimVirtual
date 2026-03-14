package com.mattfuncional.servicios;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class WebPConversionService {
    
    private static final Logger logger = LoggerFactory.getLogger(WebPConversionService.class);
    
    /**
     * Convierte una imagen a WebP usando múltiples estrategias
     */
    public byte[] convertToWebP(byte[] originalImage, String originalFormat) {
        try {
            logger.info("🔄 Iniciando conversión a WebP desde {}...", originalFormat);
            
            // Cargar la imagen original
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(originalImage));
            if (image == null) {
                logger.error("❌ No se pudo cargar la imagen original");
                return null;
            }
            
            logger.info("📊 Imagen original: {}x{} pixels, {} bytes", 
                image.getWidth(), image.getHeight(), originalImage.length);
            
            // ESTRATEGIA 1: Intentar con ImageIO directo
            byte[] webpResult = tryImageIOWebP(image);
            if (webpResult != null) {
                logger.info("✅ Conversión exitosa con ImageIO: {} bytes", webpResult.length);
                return webpResult;
            }
            
            // ESTRATEGIA 2: Intentar con diferentes tipos de BufferedImage
            webpResult = tryDifferentImageTypes(image);
            if (webpResult != null) {
                logger.info("✅ Conversión exitosa con tipos alternativos: {} bytes", webpResult.length);
                return webpResult;
            }
            
            // ESTRATEGIA 3: Intentar con PNG como alternativa
            logger.info("🔄 WebP falló, intentando PNG como alternativa...");
            byte[] pngResult = convertToPNG(image);
            if (pngResult != null) {
                logger.info("✅ Conversión a PNG exitosa: {} bytes", pngResult.length);
                return pngResult;
            }
            
            logger.warn("⚠️ Todas las estrategias de conversión fallaron");
            return null;
            
        } catch (Exception e) {
            logger.error("❌ Error en conversión WebP: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Intenta conversión directa con ImageIO
     */
    private byte[] tryImageIOWebP(BufferedImage image) {
        try {
            // Intentar con diferentes variaciones del nombre del formato
            String[] webpFormats = {"webp", "WEBP", "WebP", "Webp"};
            
            for (String format : webpFormats) {
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    if (ImageIO.write(image, format, outputStream)) {
                        byte[] result = outputStream.toByteArray();
                        if (result.length > 0 && isValidWebP(result)) {
                            logger.info("✅ ImageIO WebP exitoso con formato: {}", format);
                            return result;
                        }
                    }
                } catch (Exception e) {
                    logger.debug("Formato {} falló: {}", format, e.getMessage());
                }
            }
            
            return null;
            
        } catch (Exception e) {
            logger.debug("ImageIO WebP falló: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Intenta con diferentes tipos de BufferedImage
     */
    private byte[] tryDifferentImageTypes(BufferedImage originalImage) {
        try {
            // Crear diferentes tipos de BufferedImage
            BufferedImage[] imageTypes = {
                new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB),
                new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_3BYTE_BGR),
                new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_4BYTE_ABGR),
                new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB)
            };
            
            for (int i = 0; i < imageTypes.length; i++) {
                try {
                    BufferedImage imgType = imageTypes[i];
                    Graphics2D g2d = imgType.createGraphics();
                    
                    // Configurar calidad de renderizado
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Dibujar la imagen original en el nuevo tipo
                    g2d.drawImage(originalImage, 0, 0, null);
                    g2d.dispose();
                    
                    // Intentar convertir a WebP
                    byte[] result = tryImageIOWebP(imgType);
                    if (result != null) {
                        logger.info("✅ Conversión exitosa con tipo de imagen {}: {} bytes", i, result.length);
                        return result;
                    }
                    
                } catch (Exception e) {
                    logger.debug("Tipo de imagen {} falló: {}", i, e.getMessage());
                }
            }
            
            return null;
            
        } catch (Exception e) {
            logger.debug("Diferentes tipos de imagen fallaron: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Convierte a PNG como alternativa
     */
    private byte[] convertToPNG(BufferedImage image) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if (ImageIO.write(image, "png", outputStream)) {
                return outputStream.toByteArray();
            }
            return null;
        } catch (Exception e) {
            logger.debug("Conversión PNG falló: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Verifica si un archivo es WebP válido
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
     * Verifica si WebP está soportado en el sistema
     */
    public boolean isWebPSupported() {
        try {
            // Verificar formatos disponibles
            String[] formats = ImageIO.getWriterFormatNames();
            for (String format : formats) {
                if (format.equalsIgnoreCase("webp")) {
                    logger.info("✅ WebP soportado nativamente: {}", format);
                    return true;
                }
            }
            
            logger.warn("⚠️ WebP NO soportado nativamente");
            return false;
            
        } catch (Exception e) {
            logger.error("❌ Error verificando soporte WebP: {}", e.getMessage());
            return false;
        }
    }
}
