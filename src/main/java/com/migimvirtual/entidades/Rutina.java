package com.migimvirtual.entidades;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class Rutina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;
    private String estado; // ACTIVA, INACTIVA, COMPLETADA
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;
    private boolean esPlantilla; // true = rutina plantilla, false = rutina asignada
    private String creador; // "ADMIN" (único gestor del panel)

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "rutina_categoria",
               joinColumns = @JoinColumn(name = "rutina_id"),
               inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias = new HashSet<>();
    @Column(unique = true, length = 32)
    private String tokenPublico;

    /** Nota o reseña del profesor para el alumno (solo en rutinas asignadas, no plantillas). Visible en la hoja pública. */
    @Column(length = 2000)
    private String notaParaAlumno;

    @OneToMany(mappedBy = "rutina", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Serie> series;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "profesor_id")
    private Profesor profesor;

    public Rutina() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
        this.estado = "ACTIVA";
        this.esPlantilla = true; // Por defecto es plantilla
    }

    public Rutina(String nombre, String descripcion, Usuario usuario, Profesor profesor) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.usuario = usuario;
        this.profesor = profesor;
        this.esPlantilla = false; // Si tiene usuario asignado, no es plantilla
    }

    public Rutina(String nombre, String descripcion, Profesor profesor) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.profesor = profesor;
        this.esPlantilla = true; // Es plantilla
    }

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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
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

    public Set<Categoria> getCategorias() {
        return categorias != null ? categorias : new HashSet<>();
    }

    public void setCategorias(Set<Categoria> categorias) {
        this.categorias = categorias != null ? categorias : new HashSet<>();
    }

    /** Lista de nombres de categorías (para mostrar en vistas). */
    public List<String> getCategoriasList() {
        if (categorias == null || categorias.isEmpty()) return Collections.emptyList();
        return categorias.stream()
                .map(Categoria::getNombre)
                .filter(n -> n != null && !n.isBlank())
                .collect(Collectors.toList());
    }

    public String getTokenPublico() {
        return tokenPublico;
    }

    public void setTokenPublico(String tokenPublico) {
        this.tokenPublico = tokenPublico;
    }

    public String getNotaParaAlumno() {
        return notaParaAlumno;
    }

    public void setNotaParaAlumno(String notaParaAlumno) {
        this.notaParaAlumno = notaParaAlumno;
    }

    public List<Serie> getSeries() {
        return series;
    }

    public void setSeries(List<Serie> series) {
        this.series = series;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaModificacion = LocalDateTime.now();
    }
}
