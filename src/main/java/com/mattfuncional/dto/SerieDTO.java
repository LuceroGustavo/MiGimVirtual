package com.mattfuncional.dto;

import java.util.List;

public class SerieDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private List<EjercicioSerieDTO> ejercicios;
    private Long profesorId; // ID del profesor que crea la serie plantilla
    private int repeticionesSerie = 1; // Cantidad de veces que se repite la serie completa

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<EjercicioSerieDTO> getEjercicios() {
        return ejercicios;
    }

    public void setEjercicios(List<EjercicioSerieDTO> ejercicios) {
        this.ejercicios = ejercicios;
    }

    public Long getProfesorId() {
        return profesorId;
    }

    public void setProfesorId(Long profesorId) {
        this.profesorId = profesorId;
    }

    public int getRepeticionesSerie() {
        return repeticionesSerie;
    }

    public void setRepeticionesSerie(int repeticionesSerie) {
        this.repeticionesSerie = repeticionesSerie;
    }

    // Clase anidada para los ejercicios dentro de la serie
    public static class EjercicioSerieDTO {
        private Long ejercicioId;
        private Integer valor;
        private String unidad;
        private Integer peso; // Peso en kilos, puede ser null si no requiere peso

        // Getters y Setters
        public Long getEjercicioId() {
            return ejercicioId;
        }

        public void setEjercicioId(Long ejercicioId) {
            this.ejercicioId = ejercicioId;
        }

        public Integer getValor() {
            return valor;
        }

        public void setValor(Integer valor) {
            this.valor = valor;
        }

        public String getUnidad() {
            return unidad;
        }

        public void setUnidad(String unidad) {
            this.unidad = unidad;
        }

        public Integer getPeso() {
            return peso;
        }

        public void setPeso(Integer peso) {
            this.peso = peso;
        }
    }
}