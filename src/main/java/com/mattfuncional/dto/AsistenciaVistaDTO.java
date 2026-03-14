package com.mattfuncional.dto;

import java.time.LocalDate;
import java.util.List;

public class AsistenciaVistaDTO {
    private Long id;
    private LocalDate fecha;
    private String fechaFormateada;
    private Boolean presente;
    private boolean pendiente;
    private List<String> gruposTrabajados;
    private String observaciones;
    private Long registradoPorId;
    private String registradoPorNombre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public String getFechaFormateada() { return fechaFormateada; }
    public void setFechaFormateada(String fechaFormateada) { this.fechaFormateada = fechaFormateada; }
    public Boolean getPresente() { return presente; }
    public void setPresente(Boolean presente) { this.presente = presente; }
    public boolean isPendiente() { return pendiente; }
    public void setPendiente(boolean pendiente) { this.pendiente = pendiente; }
    public List<String> getGruposTrabajados() { return gruposTrabajados; }
    public void setGruposTrabajados(List<String> gruposTrabajados) { this.gruposTrabajados = gruposTrabajados; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Long getRegistradoPorId() { return registradoPorId; }
    public void setRegistradoPorId(Long registradoPorId) { this.registradoPorId = registradoPorId; }
    public String getRegistradoPorNombre() { return registradoPorNombre; }
    public void setRegistradoPorNombre(String registradoPorNombre) { this.registradoPorNombre = registradoPorNombre; }
}
