package com.mattfuncional.entidades;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "asistencia")
public class Asistencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate fecha;
    private boolean presente;
    @Column(length = 2000)
    private String observaciones;

    @ManyToOne
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "registrado_por_id")
    private Usuario registradoPor;

    /** Grupos musculares que trabajó el alumno ese día (seleccionados por el profesor). */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "asistencia_grupos_trabajados",
               joinColumns = @JoinColumn(name = "asistencia_id"),
               inverseJoinColumns = @JoinColumn(name = "grupo_muscular_id"))
    private Set<GrupoMuscular> gruposTrabajados = new HashSet<>();

    public Asistencia() {}

    public Asistencia(LocalDate fecha, boolean presente, String observaciones, Usuario usuario) {
        this.fecha = fecha;
        this.presente = presente;
        this.observaciones = observaciones;
        this.usuario = usuario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public boolean isPresente() { return presente; }
    public void setPresente(boolean presente) { this.presente = presente; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Usuario getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(Usuario registradoPor) { this.registradoPor = registradoPor; }
    public Set<GrupoMuscular> getGruposTrabajados() { return gruposTrabajados; }
    public void setGruposTrabajados(Set<GrupoMuscular> gruposTrabajados) { this.gruposTrabajados = gruposTrabajados != null ? gruposTrabajados : new HashSet<>(); }
} 