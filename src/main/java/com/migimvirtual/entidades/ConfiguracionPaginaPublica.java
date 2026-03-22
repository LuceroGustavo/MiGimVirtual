package com.migimvirtual.entidades;

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
    /** Enlace opcional a Google Maps u otro mapa; si está vacío y hay dirección, el pie muestra solo texto. */
    public static final String CLAVE_URL_MAPA = "url_mapa";
    public static final String CLAVE_TIKTOK = "tiktok";
    public static final String CLAVE_YOUTUBE = "youtube";
    public static final String CLAVE_FACEBOOK = "facebook";
    public static final String CLAVE_LINKEDIN = "linkedin";
    public static final String CLAVE_TWITTER = "twitter";
    public static final String CLAVE_EMAIL_CONTACTO = "email_contacto";
    /** Subtítulo bajo la marca en pie y bloques de contacto (ej. “Entrenamiento online personalizado”). */
    public static final String CLAVE_ESLOGAN = "eslogan";
}
