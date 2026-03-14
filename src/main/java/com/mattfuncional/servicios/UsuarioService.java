package com.mattfuncional.servicios;

import com.mattfuncional.entidades.Asistencia;
import com.mattfuncional.entidades.Usuario;
import com.mattfuncional.entidades.Profesor;
import com.mattfuncional.entidades.Rutina;
import com.mattfuncional.repositorios.AsistenciaRepository;
import com.mattfuncional.repositorios.CalendarioExcepcionRepository;
import com.mattfuncional.repositorios.MedicionFisicaRepository;
import com.mattfuncional.repositorios.RutinaRepository;
import com.mattfuncional.repositorios.UsuarioRepository;
import com.mattfuncional.repositorios.ProfesorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
    @Transactional
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private MedicionFisicaRepository medicionFisicaRepository;

    @Autowired
    private CalendarioExcepcionRepository calendarioExcepcionRepository;

    @Autowired
    private RutinaRepository rutinaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RutinaService rutinaService;

    // --- MÉTODOS CON CACHÉ PARA FASE 3 ---

    @Cacheable(value = "usuarios", key = "#id")
    public Usuario getUsuarioById(Long id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    @Cacheable(value = "usuarios", key = "'all'")
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    @Cacheable(value = "usuarios", key = "'alumnos'")
    public List<Usuario> getAlumnos() {
        return usuarioRepository.findAllAlumnosIncludingOrphans();
    }

    public List<Usuario> getUsuariosSistema() {
        return usuarioRepository.findByRolIn(java.util.List.of("ADMIN", "AYUDANTE"));
    }

    /**
     * Listado para la pantalla "Usuarios del sistema".
     * Developer ve todos (ADMIN, AYUDANTE, DEVELOPER). Admin solo ve ADMIN y AYUDANTE (no ve developer).
     */
    public List<Usuario> getUsuariosSistemaPara(Usuario usuarioActual) {
        if (usuarioActual != null && "DEVELOPER".equals(usuarioActual.getRol())) {
            return usuarioRepository.findByRolIn(java.util.List.of("ADMIN", "AYUDANTE", "DEVELOPER"));
        }
        return usuarioRepository.findByRolIn(java.util.List.of("ADMIN", "AYUDANTE"));
    }

    /**
     * Elimina un usuario del sistema (ADMIN/AYUDANTE/DEVELOPER).
     * Developer puede eliminar a cualquiera. Admin no puede eliminar developer ni a sí mismo.
     * @return true si se eliminó, false si no tiene permiso o no existe
     */
    @CacheEvict(value = "usuarios", allEntries = true)
    @Transactional
    public boolean eliminarUsuarioSistema(Long usuarioId, Usuario quienElimina) {
        if (quienElimina == null || usuarioId == null) return false;
        Usuario objetivo = usuarioRepository.findById(usuarioId).orElse(null);
        if (objetivo == null) return false;
        String rolObjetivo = objetivo.getRol();
        if (!java.util.Set.of("ADMIN", "AYUDANTE", "DEVELOPER").contains(rolObjetivo)) return false;
        if ("DEVELOPER".equals(quienElimina.getRol())) {
            // Developer puede eliminar a todos (incluido otro developer o a sí mismo)
        } else if ("ADMIN".equals(quienElimina.getRol())) {
            if ("DEVELOPER".equals(rolObjetivo)) return false;
            if (objetivo.getId() != null && objetivo.getId().equals(quienElimina.getId())) return false;
        } else {
            return false;
        }
        // Soltar FK en asistencia.registrado_por_id para poder borrar el usuario
        List<Asistencia> asistenciasDondeRegistro = asistenciaRepository.findByRegistradoPor_Id(usuarioId);
        for (Asistencia a : asistenciasDondeRegistro) {
            a.setRegistradoPor(null);
            asistenciaRepository.save(a);
        }
        usuarioRepository.delete(objetivo);
        return true;
    }

    /** Invalida la caché de usuarios. Útil tras importar alumnos desde backup. */
    @CacheEvict(value = "usuarios", allEntries = true)
    public void evictCacheUsuarios() {
        // Solo invalida caché; el efecto está en la anotación
    }

    @Cacheable(value = "usuarios", key = "'profesor-' + #profesorId")
    public List<Usuario> getAlumnosByProfesorId(Long profesorId) {
        return usuarioRepository.findAlumnosByProfesorIdWithRelations(profesorId);
    }

    @Cacheable(value = "usuarios", key = "'alumnos-sin-profesor'")
    public List<Usuario> getAlumnosSinProfesor() {
        return usuarioRepository.findAllAlumnosWithProfesor().stream()
            .filter(u -> u.getProfesor() == null)
            .collect(Collectors.toList());
    }

    @Cacheable(value = "usuarios", key = "'user-' + #id + '-with-relations'")
    public Optional<Usuario> getUsuarioByIdWithRelations(Long id) {
        return usuarioRepository.findByIdWithAllRelations(id);
    }

    /**
     * Carga un alumno con todas las relaciones necesarias para la ficha de detalle
     * (profesor, rutinas y horarios de asistencia). No usa caché para evitar entidades
     * desconectadas con colecciones lazy sin inicializar.
     */
    public Usuario getUsuarioByIdParaFicha(Long id) {
        Optional<Usuario> opt = usuarioRepository.findByIdWithAllRelations(id);
        if (opt.isEmpty()) return null;
        Usuario u = opt.get();
        Hibernate.initialize(u.getDiasHorariosAsistencia());
        return u;
    }

    // --- MÉTODOS CON EVICCIÓN DE CACHÉ ---

    @CacheEvict(value = "usuarios", allEntries = true)
    public Usuario crearAlumno(Usuario usuario) {
        usuario.setRol("ALUMNO");
        // Alumnos no usan login: no se guarda contraseña (queda null en BD)

        if (usuario.getEstadoAlumno() == null || usuario.getEstadoAlumno().trim().isEmpty()) {
            usuario.setEstadoAlumno("ACTIVO");
        }
        if (usuario.getFechaAlta() == null) {
            usuario.setFechaAlta(java.time.LocalDate.now());
        }
        if ("INACTIVO".equalsIgnoreCase(usuario.getEstadoAlumno()) && usuario.getFechaBaja() == null) {
            usuario.setFechaBaja(java.time.LocalDate.now());
        } else if ("ACTIVO".equalsIgnoreCase(usuario.getEstadoAlumno())) {
            usuario.setFechaBaja(null);
        }
        appendHistorialEstado(usuario, "ALTA");
        
        // Asignar avatar aleatorio si no tiene uno
        if (usuario.getAvatar() == null || usuario.getAvatar().trim().isEmpty()) {
            int avatarNumber = (int) (Math.random() * 8) + 1; // Número aleatorio entre 1 y 8
            usuario.setAvatar("/img/avatar" + avatarNumber + ".png");
        }
        
        return usuarioRepository.save(usuario);
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    public Usuario crearUsuarioSistema(String nombre, String correo, String password, String rol, Profesor profesor) {
        if (correo == null || correo.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Correo y contraseña son obligatorios");
        }
        String rolNorm = rol != null ? rol.toUpperCase().trim() : "AYUDANTE";
        if (!java.util.Set.of("ADMIN", "AYUDANTE").contains(rolNorm)) {
            throw new IllegalArgumentException("Rol inválido");
        }
        if (usuarioRepository.findFirstByCorreo(correo).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre != null && !nombre.isBlank() ? nombre.trim() : correo);
        usuario.setCorreo(correo.trim());
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rolNorm);
        usuario.setEdad(0);
        usuario.setSexo("No especificado");
        usuario.setAvatar("/img/avatar1.png");
        if (profesor != null) {
            usuario.setProfesor(profesor);
        }
        return usuarioRepository.save(usuario);
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    public void actualizarRolUsuario(Long usuarioId, String rol) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        String rolNorm = rol != null ? rol.toUpperCase().trim() : "";
        if (!java.util.Set.of("ADMIN", "AYUDANTE").contains(rolNorm)) {
            throw new IllegalArgumentException("Rol inválido");
        }
        usuario.setRol(rolNorm);
        usuarioRepository.save(usuario);
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    public void cambiarPasswordUsuario(Long usuarioId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setPassword(passwordEncoder.encode(newPassword.trim()));
        usuarioRepository.save(usuario);
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    public void actualizarDatosUsuarioSistema(Long usuarioId, String nombre, String correo) {
        if (usuarioId == null) {
            throw new IllegalArgumentException("Usuario inválido");
        }
        String nombreNorm = nombre != null ? nombre.trim() : "";
        String correoNorm = correo != null ? correo.trim() : "";
        if (nombreNorm.isEmpty() || correoNorm.isEmpty()) {
            throw new IllegalArgumentException("Nombre y correo son obligatorios");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuarioRepository.findFirstByCorreo(correoNorm).ifPresent(existente -> {
            if (!existente.getId().equals(usuarioId)) {
                throw new IllegalArgumentException("Ya existe un usuario con ese correo");
            }
        });
        usuario.setNombre(nombreNorm);
        usuario.setCorreo(correoNorm);
        usuarioRepository.save(usuario);
    }

    @CacheEvict(value = "usuarios", allEntries = true)
    public Usuario actualizarUsuario(Usuario usuario) {
        Usuario usuarioExistente = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + usuario.getId()));

        // Actualizar campos básicos
        usuarioExistente.setNombre(usuario.getNombre());
        usuarioExistente.setEdad(usuario.getEdad());
        usuarioExistente.setSexo(usuario.getSexo());
        usuarioExistente.setPeso(usuario.getPeso());
        usuarioExistente.setCorreo(usuario.getCorreo());
        usuarioExistente.setTipoAsistencia(usuario.getTipoAsistencia());
        usuarioExistente.setDiasHorariosAsistencia(usuario.getDiasHorariosAsistencia());
        usuarioExistente.setCelular(usuario.getCelular());
        usuarioExistente.setNotasProfesor(usuario.getNotasProfesor());
        usuarioExistente.setObjetivosPersonales(usuario.getObjetivosPersonales());
        usuarioExistente.setRestriccionesMedicas(usuario.getRestriccionesMedicas());
        usuarioExistente.setContactoEmergenciaNombre(usuario.getContactoEmergenciaNombre());
        usuarioExistente.setContactoEmergenciaTelefono(usuario.getContactoEmergenciaTelefono());
        if (usuarioExistente.getFechaAlta() == null) {
            usuarioExistente.setFechaAlta(java.time.LocalDate.now());
        }

        String estadoAnterior = usuarioExistente.getEstadoAlumno();
        String nuevoEstado = usuario.getEstadoAlumno();
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            nuevoEstado = estadoAnterior != null ? estadoAnterior : "ACTIVO";
        }
        usuarioExistente.setEstadoAlumno(nuevoEstado);

        if (estadoAnterior == null || !estadoAnterior.equalsIgnoreCase(nuevoEstado)) {
            if ("INACTIVO".equalsIgnoreCase(nuevoEstado)) {
                usuarioExistente.setFechaBaja(java.time.LocalDate.now());
                appendHistorialEstado(usuarioExistente, "BAJA");
                rutinaService.inactivarTodasRutinasDelAlumno(usuarioExistente.getId());
            } else if ("ACTIVO".equalsIgnoreCase(nuevoEstado)) {
                usuarioExistente.setFechaBaja(null);
                appendHistorialEstado(usuarioExistente, "REACTIVADO");
            }
        }

        // Actualizar relaciones
        if (usuario.getProfesor() != null) {
            usuarioExistente.setProfesor(usuario.getProfesor());
        }

        // IMPORTANTE: No tocar las colecciones que tienen orphanRemoval
        // Las medicionesFisicas y rutinas se mantienen como están
        // Solo se actualizan si explícitamente se proporcionan nuevas listas

        return usuarioRepository.save(usuarioExistente);
    }

    private void appendHistorialEstado(Usuario usuario, String evento) {
        String fecha = java.time.LocalDate.now().toString();
        String linea = fecha + " - " + evento;
        String historial = usuario.getHistorialEstado();
        if (historial == null || historial.trim().isEmpty()) {
            usuario.setHistorialEstado(linea);
        } else {
            usuario.setHistorialEstado(historial + "\n" + linea);
        }
    }

    /**
     * Elimina un alumno (usuario con rol ALUMNO). Antes elimina o desasigna todas las
     * referencias para no violar FKs: asistencia, mediciones físicas, excepciones de
     * calendario, y desasigna rutinas asignadas.
     */
    @CacheEvict(value = "usuarios", allEntries = true)
    @Transactional
    public void eliminarUsuario(Long id) {
        // 1) Asistencias donde este usuario es el alumno (FK usuario_id)
        asistenciaRepository.deleteByUsuario_Id(id);
        // 2) Asistencias donde este usuario figura como "registrado por" (soltar FK)
        List<Asistencia> asistenciasDondeRegistro = asistenciaRepository.findByRegistradoPor_Id(id);
        for (Asistencia a : asistenciasDondeRegistro) {
            a.setRegistradoPor(null);
            asistenciaRepository.save(a);
        }
        // 3) Mediciones físicas del alumno
        medicionFisicaRepository.deleteByUsuario_Id(id);
        // 4) Excepciones de calendario (días por excepción)
        calendarioExcepcionRepository.deleteByUsuario_Id(id);
        // 5) Eliminar todas las rutinas asignadas al alumno (activas e inactivas); así no quedan rutinas huérfanas en "Rutinas asignadas"
        List<Rutina> rutinasDelAlumno = rutinaRepository.findByUsuarioId(id);
        for (Rutina r : rutinasDelAlumno) {
            rutinaService.eliminarRutina(r.getId());
        }
        usuarioRepository.deleteById(id);
    }

    /**
     * Asigna avatares a usuarios existentes que no tienen uno
     */
    @CacheEvict(value = "usuarios", allEntries = true)
    public void asignarAvataresAUsuariosExistentes() {
        System.out.println("=== Iniciando asignación de avatares ===");
        
        List<Usuario> todosLosUsuarios = usuarioRepository.findAll();
        System.out.println("Total de usuarios encontrados: " + todosLosUsuarios.size());
        
        List<Usuario> usuariosSinAvatar = todosLosUsuarios.stream()
                .filter(u -> u.getAvatar() == null || 
                           u.getAvatar().trim().isEmpty() || 
                           u.getAvatar().equals("/img/not_imagen.png"))
                .collect(Collectors.toList());
        
        System.out.println("Usuarios sin avatar válido: " + usuariosSinAvatar.size());
        
        for (Usuario usuario : usuariosSinAvatar) {
            int avatarNumber = (int) (Math.random() * 8) + 1; // Número aleatorio entre 1 y 8
            String avatarPath = "/img/avatar" + avatarNumber + ".png";
            usuario.setAvatar(avatarPath);
            usuarioRepository.save(usuario);
            System.out.println("Avatar asignado a " + usuario.getNombre() + " (ID: " + usuario.getId() + "): " + avatarPath);
        }
        
        System.out.println("=== Asignación de avatares completada ===");
    }

    // --- MÉTODOS EXISTENTES (sin cambios) ---

    public Usuario getUsuarioActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String correo = authentication.getName();
            Usuario usuario = usuarioRepository.findFirstByCorreo(correo).orElse(null);
            
            // Si es un profesor y no tiene la relación cargada, intentar cargarla
            if (usuario != null && "ADMIN".equals(usuario.getRol()) && usuario.getProfesor() == null) {
                try {
                    Profesor profesor = profesorRepository.findFirstByCorreo(correo).orElse(null);
                    if (profesor != null) {
                        usuario.setProfesor(profesor);
                    }
                } catch (Exception e) {
                    // Si hay error al cargar el profesor, continuar sin él
                }
            }
            
            return usuario;
        }
        return null;
    }

    /**
     * Obtiene el usuario actual con todas las relaciones cargadas (profesor, rutinas, etc.)
     */
    public Usuario getUsuarioActualWithRelations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String correo = authentication.getName();
            // Usar la consulta optimizada que carga las relaciones
            return usuarioRepository.findByIdWithAllRelations(
                usuarioRepository.findFirstByCorreo(correo).map(Usuario::getId).orElse(null)
            ).orElse(null);
        }
        return null;
    }


    public void actualizarPasswordDeUsuario(Usuario usuario, String nuevaPassword) {
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);
    }

    /**
     * Verifica si una contraseña coincide con la contraseña encriptada del usuario
     */
    public boolean verificarPassword(String passwordPlana, String passwordEncriptada) {
        return passwordEncoder.matches(passwordPlana, passwordEncriptada);
    }

    /**
     * Busca un profesor por su correo electrónico
     */
    public Profesor findProfesorByCorreo(String correo) {
        return profesorRepository.findFirstByCorreo(correo).orElse(null);
    }

    /**
     * Crea un usuario para un profesor
     */
    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public Usuario crearUsuarioParaProfesor(Profesor profesor, String password) {
        Usuario usuario = new Usuario();
        usuario.setNombre(profesor.getNombre() + (profesor.getApellido() != null && !profesor.getApellido().isEmpty() ? " " + profesor.getApellido() : ""));
        usuario.setCorreo(profesor.getCorreo());
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol("ADMIN");
        // Establecer la relación con el profesor para que pueda acceder a sus ejercicios
        usuario.setProfesor(profesor);
        
        // Asignar avatar aleatorio
        int avatarNumber = (int) (Math.random() * 8) + 1;
        usuario.setAvatar("/img/avatar" + avatarNumber + ".png");
        
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        
        // Verificar que la relación se estableció correctamente
        if (usuarioGuardado.getProfesor() == null) {
            throw new RuntimeException("Error al establecer la relación con el profesor para el usuario: " + usuarioGuardado.getCorreo());
        }
        
        return usuarioGuardado;
    }
}
