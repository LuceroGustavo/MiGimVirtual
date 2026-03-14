package com.mattfuncional.entidades;

import jakarta.persistence.*;

@Entity
@Table(name = "pizarra_item")
public class PizarraItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "columna_id", nullable = false)
    private PizarraColumna columna;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", nullable = false)
    private Exercise exercise;

    private Integer peso; // kg
    private Integer repeticiones; // o minutos si unidad = "min"
    private String unidad = "reps"; // "reps" | "min"

    @Column(nullable = false)
    private int orden = 0;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PizarraColumna getColumna() { return columna; }
    public void setColumna(PizarraColumna columna) { this.columna = columna; }

    public Exercise getExercise() { return exercise; }
    public void setExercise(Exercise exercise) { this.exercise = exercise; }

    public Integer getPeso() { return peso; }
    public void setPeso(Integer peso) { this.peso = peso; }

    public Integer getRepeticiones() { return repeticiones; }
    public void setRepeticiones(Integer repeticiones) { this.repeticiones = repeticiones; }

    public String getUnidad() { return unidad != null ? unidad : "reps"; }
    public void setUnidad(String unidad) { this.unidad = unidad != null ? unidad : "reps"; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }
}
