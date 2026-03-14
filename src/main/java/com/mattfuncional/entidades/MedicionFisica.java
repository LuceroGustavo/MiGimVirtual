package com.mattfuncional.entidades;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class MedicionFisica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private LocalDate fecha;
    private Double peso;      // kg
    private Double altura;    // cm
    private Double cintura;   // cm (opcional)
    private Double pecho;     // cm (opcional)
    private Double cadera;    // cm (opcional)
    private Double biceps;    // cm (opcional)
    private Double muslo;     // cm (opcional)

    public MedicionFisica() {}

    // Getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Double getPeso() { return peso; }
    public void setPeso(Double peso) { this.peso = peso; }

    public Double getAltura() { return altura; }
    public void setAltura(Double altura) { this.altura = altura; }

    public Double getCintura() { return cintura; }
    public void setCintura(Double cintura) { this.cintura = cintura; }

    public Double getPecho() { return pecho; }
    public void setPecho(Double pecho) { this.pecho = pecho; }

    public Double getCadera() { return cadera; }
    public void setCadera(Double cadera) { this.cadera = cadera; }

    public Double getBiceps() { return biceps; }
    public void setBiceps(Double biceps) { this.biceps = biceps; }

    public Double getMuslo() { return muslo; }
    public void setMuslo(Double muslo) { this.muslo = muslo; }
} 