package com.mattfuncional.entidades;

import jakarta.persistence.*;

@Entity
public class SerieEjercicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "serie_id")
    private Serie serie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id")
    private Exercise exercise;

    private Integer valor; // Cantidad de repeticiones o minutos
    private String unidad; // "reps" o "min"
    private Integer peso; // Peso en kilos, puede ser null si no requiere peso
    private Integer orden = 0; // Posición del ejercicio dentro de la serie (para reordenar)

    // Constructores, Getters y Setters

    public SerieEjercicio() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Serie getSerie() {
        return serie;
    }

    public void setSerie(Serie serie) {
        this.serie = serie;
    }

    public Exercise getExercise() {
        return exercise;
    }

    public void setExercise(Exercise exercise) {
        this.exercise = exercise;
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

    public Integer getOrden() {
        return orden != null ? orden : 0;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }
}