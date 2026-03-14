package com.mattfuncional.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Set;

@Entity
@Table(uniqueConstraints = { 
    @UniqueConstraint(columnNames = { "name", "profesor_id" }, 
                      name = "uk_exercise_name_profesor") 
})
public class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "exercise_grupos",
               joinColumns = @JoinColumn(name = "exercise_id"),
               inverseJoinColumns = @JoinColumn(name = "grupo_muscular_id"))
    private Set<GrupoMuscular> grupos;

    private String type;
    private String videoUrl;
    private String instructions;
    private String benefits;
    private String contraindications;

    @OneToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @JoinColumn(name = "imagen_id")
    @JsonIgnore
    private Imagen imagen;

    @ManyToOne
    @JoinColumn(name = "profesor_id", nullable = true)
    private Profesor profesor; // null = ejercicio predeterminado
    
    @Column(nullable = false)
    private Boolean esPredeterminado = false;

    // Constructor sin argumentos
    public Exercise() {
    }

    // Constructor con argumentos
    public Exercise(String name, String description, Set<GrupoMuscular> grupos, String type,
            String videoUrl, String instructions, String benefits, String contraindications) {
        this.name = name;
        this.description = description;
        this.grupos = grupos;
        this.type = type;
        this.videoUrl = videoUrl;
        this.instructions = instructions;
        this.benefits = benefits;
        this.contraindications = contraindications;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<GrupoMuscular> getGrupos() {
        return grupos;
    }

    public void setGrupos(Set<GrupoMuscular> grupos) {
        this.grupos = grupos;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getBenefits() {
        return benefits;
    }

    public void setBenefits(String benefits) {
        this.benefits = benefits;
    }

    public String getContraindications() {
        return contraindications;
    }

    public void setContraindications(String contraindications) {
        this.contraindications = contraindications;
    }

    public Imagen getImagen() {
        return imagen;
    }

    public void setImagen(Imagen imagen) {
        this.imagen = imagen;
    }

    public Profesor getProfesor() {
        return profesor;
    }

    public void setProfesor(Profesor profesor) {
        this.profesor = profesor;
    }
    
    public Boolean getEsPredeterminado() {
        return esPredeterminado;
    }
    
    public void setEsPredeterminado(Boolean esPredeterminado) {
        this.esPredeterminado = esPredeterminado;
    }
    
    /**
     * Verifica si el ejercicio es predeterminado
     * Un ejercicio es predeterminado si tiene el flag activado o si no tiene profesor asignado
     */
    public boolean isPredeterminado() {
        return esPredeterminado != null && esPredeterminado || profesor == null;
    }
    
    /**
     * Verifica si el ejercicio puede ser editado por un usuario específico.
     * - DEVELOPER y ADMIN pueden editar todo (incluidos predeterminados).
     * - Ejercicios predeterminados solo pueden ser editados por DEVELOPER o ADMIN.
     * - Ejercicios personalizados solo pueden ser editados por su propietario (profesor).
     */
    public boolean puedeSerEditadoPor(com.mattfuncional.entidades.Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        String rol = usuario.getRol() != null ? usuario.getRol() : "";
        // DEVELOPER y ADMIN pueden editar todo, incluidos predeterminados
        if ("DEVELOPER".equals(rol) || "ADMIN".equals(rol)) {
            return true;
        }
        // Si es predeterminado, solo DEVELOPER/ADMIN pueden editar (ya cubierto arriba)
        if (isPredeterminado()) {
            return false;
        }
        // Si tiene profesor, solo el propietario puede editar
        if (profesor != null && usuario.getProfesor() != null) {
            return profesor.getId().equals(usuario.getProfesor().getId());
        }
        return false;
    }
}
