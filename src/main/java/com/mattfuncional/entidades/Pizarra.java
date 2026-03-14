package com.mattfuncional.entidades;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pizarra")
public class Pizarra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(unique = true, length = 32)
    private String token;

    /** Hash del PIN de 4 dígitos para ver la sala en TV (null = sin código). */
    @Column(name = "pin_sala_hash", length = 80)
    private String pinSalaHash;

    @Column(nullable = false)
    private int cantidadColumnas = 1;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    @ManyToOne
    @JoinColumn(name = "profesor_id", nullable = false)
    private Profesor profesor;

    @OneToMany(mappedBy = "pizarra", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    private List<PizarraColumna> columnas = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        fechaModificacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getPinSalaHash() { return pinSalaHash; }
    public void setPinSalaHash(String pinSalaHash) { this.pinSalaHash = pinSalaHash; }

    public int getCantidadColumnas() { return cantidadColumnas; }
    public void setCantidadColumnas(int cantidadColumnas) { this.cantidadColumnas = cantidadColumnas; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }

    public Profesor getProfesor() { return profesor; }
    public void setProfesor(Profesor profesor) { this.profesor = profesor; }

    public List<PizarraColumna> getColumnas() { return columnas; }
    public void setColumnas(List<PizarraColumna> columnas) { this.columnas = columnas; }
}
