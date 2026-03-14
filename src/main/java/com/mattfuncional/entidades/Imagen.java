package com.mattfuncional.entidades;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Imagen {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

    private String mime;

    private String nombre;

    /**
     * Ruta relativa del archivo en el sistema de archivos
     * Ejemplo: "ejercicios/2025/01/ejercicio_abc123.webp"
     */
    @Column(nullable = false, length = 500)
    private String rutaArchivo;

    /**
     * Tamaño del archivo en bytes (para estadísticas)
     */
    private Long tamanoBytes;

    /**
     * Obtiene la URL pública para acceder a la imagen
     * @return URL relativa para usar en templates (ej: "/img/abc123")
     */
    public String getUrl() {
        if (this.id != null) {
            return "/img/" + this.id;
        }
        return null;
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public Long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(Long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }
    
    /**
     * Método de compatibilidad hacia atrás (deprecated)
     * Retorna null ya que las imágenes ahora están en filesystem
     * @deprecated Usar getUrl() en su lugar
     */
    @Deprecated
    public String getBase64Encoded() {
        return null; // Ya no se usa base64
    }
}
