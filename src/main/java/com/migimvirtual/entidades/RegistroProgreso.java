package com.migimvirtual.entidades;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "registro_progreso")
public class RegistroProgreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 500)
    private String gruposMusculares; // Nombres separados por coma: "BRAZOS,PIERNAS,PECHO"

    @Column(length = 2000)
    private String observaciones;

    public RegistroProgreso() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getGruposMusculares() {
        return gruposMusculares;
    }

    public void setGruposMusculares(String gruposMusculares) {
        this.gruposMusculares = gruposMusculares;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
