package com.mattfuncional.controladores;

import com.mattfuncional.entidades.Imagen;
import com.mattfuncional.excepciones.ResourceNotFoundException;
import com.mattfuncional.repositorios.ImagenRepository;
import com.mattfuncional.servicios.ImagenServicio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Controlador para servir imágenes desde el filesystem
 */
@Controller
public class ImagenController {

    private static final Logger logger = LoggerFactory.getLogger(ImagenController.class);

    @Autowired
    private ImagenServicio imagenServicio;

    @Autowired
    private ImagenRepository imagenRepository;

    /**
     * Endpoint para servir imágenes desde el filesystem
     * URL: /img/{id}
     * Solo intercepta UUIDs (IDs de imágenes en BD), no archivos estáticos
     * Los archivos estáticos (avatar1.png, etc.) se sirven directamente desde /static/img/
     */
    @GetMapping("/img/{id:[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}}")
    public ResponseEntity<byte[]> obtenerImagen(@PathVariable String id) {
        try {
            logger.info("📥 Solicitud de imagen recibida: ID={}", id);
            
            // Obtener imagen de BD
            Imagen imagen = imagenRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Imagen no encontrada con ID: " + id));
            
            logger.info("✅ Imagen encontrada en BD: ID={}, Ruta={}, MIME={}", 
                       imagen.getId(), imagen.getRutaArchivo(), imagen.getMime());
            
            // Obtener contenido del filesystem
            byte[] contenido = imagenServicio.obtenerContenido(id);
            
            logger.info("✅ Archivo físico leído: {} bytes", contenido.length);
            
            // Configurar headers HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(imagen.getMime()));
            headers.setContentLength(contenido.length);
            
            // Headers para caché del navegador (1 año)
            headers.setCacheControl("public, max-age=31536000");
            headers.setExpires(System.currentTimeMillis() + 31536000000L);
            
            logger.info("✅ Imagen servida exitosamente: {} ({} bytes, {})", id, contenido.length, imagen.getMime());
            
            return new ResponseEntity<>(contenido, headers, HttpStatus.OK);
            
        } catch (ResourceNotFoundException e) {
            logger.debug("Imagen no encontrada (se sirve placeholder): {} - {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/img/not_imagen.png")).build();
        } catch (Exception e) {
            logger.warn("Error al servir imagen {} (se sirve placeholder): {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("/img/not_imagen.png")).build();
        }
    }
}

