package com.mattfuncional.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para el estado de la pizarra (API sala, vista TV).
 */
public class PizarraEstadoDTO {

    private String nombre;
    private int cantidadColumnas;
    private List<ColumnaDTO> columnas = new ArrayList<>();

    public static class ColumnaDTO {
        private Long id;
        private String titulo;
        private Integer vueltas; // 1-9, null = no mostrar
        private int orden;
        private List<ItemDTO> items = new ArrayList<>();

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }
        public Integer getVueltas() { return vueltas; }
        public void setVueltas(Integer vueltas) { this.vueltas = vueltas; }
        public int getOrden() { return orden; }
        public void setOrden(int orden) { this.orden = orden; }
        public List<ItemDTO> getItems() { return items; }
        public void setItems(List<ItemDTO> items) { this.items = items; }
    }

    public static class ItemDTO {
        private Long id;
        private Long exerciseId;
        private String ejercicioNombre;
        private String imagenUrl;
        private String grupoMuscular; // primer grupo o concatenado
        private Integer peso;
        private Integer repeticiones;
        private String unidad;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getExerciseId() { return exerciseId; }
        public void setExerciseId(Long exerciseId) { this.exerciseId = exerciseId; }
        public String getEjercicioNombre() { return ejercicioNombre; }
        public void setEjercicioNombre(String ejercicioNombre) { this.ejercicioNombre = ejercicioNombre; }
        public String getImagenUrl() { return imagenUrl; }
        public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }
        public String getGrupoMuscular() { return grupoMuscular; }
        public void setGrupoMuscular(String grupoMuscular) { this.grupoMuscular = grupoMuscular; }
        public Integer getPeso() { return peso; }
        public void setPeso(Integer peso) { this.peso = peso; }
        public Integer getRepeticiones() { return repeticiones; }
        public void setRepeticiones(Integer repeticiones) { this.repeticiones = repeticiones; }
        public String getUnidad() { return unidad; }
        public void setUnidad(String unidad) { this.unidad = unidad; }
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getCantidadColumnas() { return cantidadColumnas; }
    public void setCantidadColumnas(int cantidadColumnas) { this.cantidadColumnas = cantidadColumnas; }
    public List<ColumnaDTO> getColumnas() { return columnas; }
    public void setColumnas(List<ColumnaDTO> columnas) { this.columnas = columnas; }
}
