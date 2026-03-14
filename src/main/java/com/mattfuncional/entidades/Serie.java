package com.mattfuncional.entidades;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Serie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private int orden; // Para mantener el orden de las series en una rutina
    private String descripcion;
    private boolean esPlantilla; // true = serie plantilla, false = serie asignada
    private String creador; // "ADMIN" (único gestor del panel)
    private int repeticionesSerie = 1; // Cantidad de veces que se repite la serie completa
    private Long plantillaId; // ID de la Serie plantilla original

    @OneToMany(mappedBy = "serie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SerieEjercicio> serieEjercicios = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "rutina_id")
    private Rutina rutina;

    @ManyToOne
    @JoinColumn(name = "profesor_id")
    private Profesor profesor;

    // Constructor sin argumentos
    public Serie() {
        this.esPlantilla = true; // Por defecto es plantilla
        this.repeticionesSerie = 1; // Por defecto 1 repetición
    }

    // Constructor con argumentos
    public Serie(String nombre, int orden, String descripcion,
            List<SerieEjercicio> serieEjercicios, Rutina rutina, Profesor profesor) {
        this();
        this.nombre = nombre;
        this.orden = orden;
        this.descripcion = descripcion;
        this.serieEjercicios = serieEjercicios;
        this.rutina = rutina;
        this.profesor = profesor;
    }

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

    public int getOrden() {
        return orden;
    }

    public void setOrden(int orden) {
        this.orden = orden;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isEsPlantilla() {
        return esPlantilla;
    }

    public void setEsPlantilla(boolean esPlantilla) {
        this.esPlantilla = esPlantilla;
    }

    public String getCreador() {
        return creador;
    }

    public void setCreador(String creador) {
        this.creador = creador;
    }

    public Long getPlantillaId() {
        return plantillaId;
    }

    public void setPlantillaId(Long plantillaId) {
        this.plantillaId = plantillaId;
    }

    public int getRepeticionesSerie() {
        return repeticionesSerie;
    }

    public void setRepeticionesSerie(int repeticionesSerie) {
        this.repeticionesSerie = repeticionesSerie;
    }

    public List<SerieEjercicio> getSerieEjercicios() {
        return serieEjercicios;
    }

    public void setSerieEjercicios(List<SerieEjercicio> serieEjercicios) {
        this.serieEjercicios = serieEjercicios;
    }

    public Rutina getRutina() {
        return rutina;
    }

    public void setRutina(Rutina rutina) {
        this.rutina = rutina;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }
}