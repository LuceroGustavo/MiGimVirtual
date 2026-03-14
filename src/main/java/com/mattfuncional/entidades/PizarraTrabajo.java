package com.mattfuncional.entidades;

import jakarta.persistence.*;

/**
 * Asocia un profesor con su "pizarra de trabajo" (panel en vivo).
 * Una sola pizarra por profesor para trabajar y transmitir.
 */
@Entity
@Table(name = "pizarra_trabajo", uniqueConstraints = @UniqueConstraint(columnNames = "profesor_id"))
public class PizarraTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "profesor_id", nullable = false, unique = true)
    private Long profesorId;

    @Column(name = "pizarra_id", nullable = false)
    private Long pizarraId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProfesorId() { return profesorId; }
    public void setProfesorId(Long profesorId) { this.profesorId = profesorId; }

    public Long getPizarraId() { return pizarraId; }
    public void setPizarraId(Long pizarraId) { this.pizarraId = pizarraId; }
}
