package com.mattfuncional.entidades;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pizarra_columna")
public class PizarraColumna {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo = "";

    /** Cantidad de vueltas/pasadas (1-9). Null = no mostrar en TV. */
    private Integer vueltas;

    @Column(nullable = false)
    private int orden = 0;

    @ManyToOne
    @JoinColumn(name = "pizarra_id", nullable = false)
    private Pizarra pizarra;

    @OneToMany(mappedBy = "columna", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orden ASC")
    private List<PizarraItem> items = new ArrayList<>();

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo != null ? titulo : ""; }

    public Integer getVueltas() { return vueltas; }
    public void setVueltas(Integer vueltas) { this.vueltas = vueltas; }

    public int getOrden() { return orden; }
    public void setOrden(int orden) { this.orden = orden; }

    public Pizarra getPizarra() { return pizarra; }
    public void setPizarra(Pizarra pizarra) { this.pizarra = pizarra; }

    public List<PizarraItem> getItems() { return items; }
    public void setItems(List<PizarraItem> items) { this.items = items; }
}
