package com.mattfuncional.entidades;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.time.LocalDate;

import com.mattfuncional.enums.TipoAsistencia;

@Entity
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre; // Nombre completo del alumno (un solo campo)
    private int edad;
    private String sexo;
    private double peso;
    private String password;
    private String rol;
    private String avatar;

    @Email(message = "Debe ingresar un correo válido")
    @Column(unique = true)
    private String correo;

    @OneToMany(mappedBy = "usuario")
    private List<Rutina> rutinas;

    private TipoAsistencia tipoAsistencia;
    @ElementCollection
    @CollectionTable(name = "usuario_dias_horarios_asistencia", joinColumns = @JoinColumn(name = "usuario_id"))
    @AttributeOverrides({
        @AttributeOverride(name = "dia", column = @Column(name = "dia")),
        @AttributeOverride(name = "horaEntrada", column = @Column(name = "hora_entrada")),
        @AttributeOverride(name = "horaSalida", column = @Column(name = "hora_salida"))
    })
    private List<DiaHorarioAsistencia> diasHorariosAsistencia;

    private String notasProfesor;
    private String objetivosPersonales;
    private String restriccionesMedicas;
    private String celular;
    private String estadoAlumno; // ACTIVO / INACTIVO
    private LocalDate fechaAlta;
    private LocalDate fechaBaja;
    private String historialEstado;
    private String contactoEmergenciaNombre;
    private String contactoEmergenciaTelefono;
    private LocalDate fechaInicio;
    private String historialAsistencia; // (JSON o texto, para futuro)
    private String detalleAsistencia; // Detalle libre de asistencia (ej: "Martes y jueves de 18 a 19 hs", "Pase libre", etc)

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MedicionFisica> medicionesFisicas;
    

    // Constructor sin argumentos
    public Usuario() {
    }

    // Constructor con argumentos
    public Usuario(String nombre, int edad, String sexo, double peso, String password) {
        this.nombre = nombre;
        this.edad = edad;
        this.sexo = sexo;
        this.peso = peso;
        this.password = password;
    }

    @ManyToOne
    @JoinColumn(name = "profesor_id")
    private Profesor profesor;

    // Getters y Setters
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

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public double getPeso() {
        return peso;
    }

    public void setPeso(double peso) {
        this.peso = peso;
    }

    public List<Rutina> getRutinas() {
        return rutinas;
    }

    public void setRutinas(List<Rutina> rutinas) {
        this.rutinas = rutinas;
    }

    @Override
    public String getPassword() {
        return password != null ? password : "";
    }

    @Override
    public String getUsername() {
        return this.correo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRol() {
        return rol;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (rol == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol));
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public TipoAsistencia getTipoAsistencia() {
        return tipoAsistencia;
    }

    public void setTipoAsistencia(TipoAsistencia tipoAsistencia) {
        this.tipoAsistencia = tipoAsistencia;
    }

    public List<DiaHorarioAsistencia> getDiasHorariosAsistencia() {
        return diasHorariosAsistencia;
    }

    public void setDiasHorariosAsistencia(List<DiaHorarioAsistencia> diasHorariosAsistencia) {
        this.diasHorariosAsistencia = diasHorariosAsistencia;
    }

    public String getNotasProfesor() {
        return notasProfesor;
    }

    public void setNotasProfesor(String notasProfesor) {
        this.notasProfesor = notasProfesor;
    }

    public String getObjetivosPersonales() {
        return objetivosPersonales;
    }

    public void setObjetivosPersonales(String objetivosPersonales) {
        this.objetivosPersonales = objetivosPersonales;
    }

    public String getRestriccionesMedicas() {
        return restriccionesMedicas;
    }

    public void setRestriccionesMedicas(String restriccionesMedicas) {
        this.restriccionesMedicas = restriccionesMedicas;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getEstadoAlumno() {
        return estadoAlumno;
    }

    public void setEstadoAlumno(String estadoAlumno) {
        this.estadoAlumno = estadoAlumno;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(LocalDate fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public LocalDate getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(LocalDate fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public String getHistorialEstado() {
        return historialEstado;
    }

    public void setHistorialEstado(String historialEstado) {
        this.historialEstado = historialEstado;
    }

    public String getContactoEmergenciaNombre() {
        return contactoEmergenciaNombre;
    }

    public void setContactoEmergenciaNombre(String contactoEmergenciaNombre) {
        this.contactoEmergenciaNombre = contactoEmergenciaNombre;
    }

    public String getContactoEmergenciaTelefono() {
        return contactoEmergenciaTelefono;
    }

    public void setContactoEmergenciaTelefono(String contactoEmergenciaTelefono) {
        this.contactoEmergenciaTelefono = contactoEmergenciaTelefono;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public String getHistorialAsistencia() {
        return historialAsistencia;
    }

    public void setHistorialAsistencia(String historialAsistencia) {
        this.historialAsistencia = historialAsistencia;
    }

    public List<MedicionFisica> getMedicionesFisicas() {
        return medicionesFisicas;
    }

    public void setMedicionesFisicas(List<MedicionFisica> medicionesFisicas) {
        this.medicionesFisicas = medicionesFisicas;
    }

    public String getDetalleAsistencia() {
        return detalleAsistencia;
    }

    public void setDetalleAsistencia(String detalleAsistencia) {
        this.detalleAsistencia = detalleAsistencia;
    }
    
}
