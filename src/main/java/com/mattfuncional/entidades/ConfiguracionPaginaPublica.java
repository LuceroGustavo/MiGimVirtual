package com.mattfuncional.entidades;

import jakarta.persistence.*;

@Entity
@Table(name = "configuracion_pagina_publica", uniqueConstraints = @UniqueConstraint(columnNames = "clave"))
public class ConfiguracionPaginaPublica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String clave;

    @Column(length = 500)
    private String valor;

    public ConfiguracionPaginaPublica() {
    }

    public ConfiguracionPaginaPublica(String clave, String valor) {
        this.clave = clave;
        this.valor = valor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    /** Claves estándar para la configuración. */
    public static final String CLAVE_WHATSAPP = "whatsapp";
    public static final String CLAVE_INSTAGRAM = "instagram";
    public static final String CLAVE_DIRECCION = "direccion";
    public static final String CLAVE_DIAS_HORARIOS = "dias_horarios";
    public static final String CLAVE_TELEFONO = "telefono";
}
