package com.mattfuncional.entidades;

import jakarta.persistence.*;

/**
 * Una sala de transmisión por profesor: un único token/enlace para el TV.
 * La TV muestra siempre la pizarra indicada en pizarraId; el profesor
 * puede cambiar de pizarra sin cambiar el enlace.
 */
@Entity
@Table(name = "sala_transmision", uniqueConstraints = {
    @UniqueConstraint(columnNames = "profesor_id"),
    @UniqueConstraint(columnNames = "token")
})
public class SalaTransmision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profesor_id", nullable = false, unique = true)
    private Long profesorId;

    @Column(nullable = false, unique = true, length = 32)
    private String token;

    @Column(name = "pin_sala_hash", length = 80)
    private String pinSalaHash;

    @Column(name = "pizarra_id")
    private Long pizarraId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProfesorId() { return profesorId; }
    public void setProfesorId(Long profesorId) { this.profesorId = profesorId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getPinSalaHash() { return pinSalaHash; }
    public void setPinSalaHash(String pinSalaHash) { this.pinSalaHash = pinSalaHash; }

    public Long getPizarraId() { return pizarraId; }
    public void setPizarraId(Long pizarraId) { this.pizarraId = pizarraId; }
}
