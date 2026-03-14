package com.mattfuncional.servicios;

import com.mattfuncional.entidades.Profesor;
import com.mattfuncional.entidades.Usuario;
import com.mattfuncional.entidades.Exercise;
import com.mattfuncional.repositorios.ProfesorRepository;
import com.mattfuncional.repositorios.UsuarioRepository;
import com.mattfuncional.repositorios.ExerciseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProfesorService {

    @Autowired
    private ProfesorRepository profesorRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ExerciseRepository exerciseRepository;

    // --- MÉTODOS CON CACHÉ PARA FASE 3 ---

    @Cacheable(value = "profesores", key = "#id")
    public Profesor getProfesorById(Long id) {
        return profesorRepository.findById(id).orElse(null);
    }

    @Cacheable(value = "profesores", key = "'all'")
    public List<Profesor> getAllProfesores() {
        return profesorRepository.findAll();
    }

    @Cacheable(value = "profesores", key = "'all-with-usuarios'")
    public List<Profesor> getAllProfesoresWithUsuarios() {
        return profesorRepository.findAllWithUsuarios();
    }

    @Cacheable(value = "profesores", key = "'profesor-' + #id + '-with-relations'")
    public Profesor getProfesorByIdWithRelations(Long id) {
        return profesorRepository.findByIdWithRelations(id);
    }

    @Cacheable(value = "profesores", key = "'correo-' + #correo")
    public Profesor getProfesorByCorreo(String correo) {
        return profesorRepository.findFirstByCorreo(correo).orElse(null);
    }

    // --- MÉTODOS CON EVICCIÓN DE CACHÉ ---

    @CacheEvict(value = "profesores", allEntries = true)
    public Profesor guardarProfesor(Profesor profesor) {
        return profesorRepository.save(profesor);
    }

    @CacheEvict(value = {"profesores", "usuarios"}, allEntries = true)
    public void eliminarProfesor(Long id) {
        Profesor profesor = profesorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Profesor no encontrado con id: " + id));
        
        // No permitir eliminar al único profesor gestor del sistema
        if ("profesor@mattfuncional.com".equals(profesor.getCorreo())) {
            throw new RuntimeException("No se puede eliminar al profesor del sistema");
        }
        
        // 1. Eliminar el usuario asociado al profesor (si existe)
        try {
            Optional<Usuario> usuarioProfesor = usuarioRepository.findFirstByCorreo(profesor.getCorreo());
            if (usuarioProfesor.isPresent()) {
                Usuario usuario = usuarioProfesor.get();
                // Verificar que el usuario tenga rol ADMIN antes de eliminarlo
                if ("ADMIN".equals(usuario.getRol())) {
                    usuarioRepository.delete(usuario);
                } else {
                    // Si el usuario no tiene rol ADMIN, solo quitar la relación
                    usuario.setProfesor(null);
                    usuarioRepository.save(usuario);
                }
            }
        } catch (Exception e) {
            // Log del error pero continuar con la eliminación
            System.err.println("Error al eliminar usuario del profesor: " + e.getMessage());
        }
        
        // 2. Desasignar a todos los alumnos de este profesor (no eliminarlos, solo quitar la relación)
        try {
            List<Usuario> alumnosDelProfesor = usuarioRepository.findAllByProfesorId(id);
            for (Usuario alumno : alumnosDelProfesor) {
                alumno.setProfesor(null);
                usuarioRepository.save(alumno);
            }
        } catch (Exception e) {
            // Log del error pero continuar con la eliminación
            System.err.println("Error al desasignar alumnos del profesor: " + e.getMessage());
        }
        
        // 3. ELIMINAR TODOS LOS EJERCICIOS DEL PROFESOR (NUEVO)
        try {
            List<Exercise> ejerciciosDelProfesor = exerciseRepository.findByProfesor_Id(id);
            for (Exercise ejercicio : ejerciciosDelProfesor) {
                exerciseRepository.delete(ejercicio);
            }
            System.out.println("Se eliminaron " + ejerciciosDelProfesor.size() + " ejercicios del profesor");
        } catch (Exception e) {
            // Log del error pero continuar con la eliminación
            System.err.println("Error al eliminar ejercicios del profesor: " + e.getMessage());
        }
        
        // 4. Finalmente eliminar el profesor
        profesorRepository.deleteById(id);
    }
}
