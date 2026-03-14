package com.mattfuncional.dto;

import java.time.LocalDateTime;

public class ChatMessage {
    private Long id;
    private String contenido;
    private Long emisorId;
    private Long receptorId;
    private String tipoEmisor;
    private LocalDateTime fecha;
    private boolean leido;

    // Constructores
    public ChatMessage() {
    }

    public ChatMessage(Long id, String contenido, Long emisorId, Long receptorId,
            String tipoEmisor, LocalDateTime fecha, boolean leido) {
        this.id = id;
        this.contenido = contenido;
        this.emisorId = emisorId;
        this.receptorId = receptorId;
        this.tipoEmisor = tipoEmisor;
        this.fecha = fecha;
        this.leido = leido;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public Long getEmisorId() {
        return emisorId;
    }

    public void setEmisorId(Long emisorId) {
        this.emisorId = emisorId;
    }

    public Long getReceptorId() {
        return receptorId;
    }

    public void setReceptorId(Long receptorId) {
        this.receptorId = receptorId;
    }

    public String getTipoEmisor() {
        return tipoEmisor;
    }

    public void setTipoEmisor(String tipoEmisor) {
        this.tipoEmisor = tipoEmisor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }
}