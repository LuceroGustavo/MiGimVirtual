package com.migimvirtual.config;

import com.migimvirtual.entidades.Profesor;
import com.migimvirtual.entidades.Usuario;
import com.migimvirtual.repositorios.UsuarioRepository;
import com.migimvirtual.servicios.ConfiguracionPaginaPublicaService;
import com.migimvirtual.servicios.CategoriaService;
import com.migimvirtual.servicios.ExerciseCargaDefaultOptimizado;
import com.migimvirtual.servicios.ExerciseService;
import com.migimvirtual.servicios.GrupoMuscularService;
import com.migimvirtual.servicios.PlanPublicoService;
import com.migimvirtual.servicios.ProfesorService;
import com.migimvirtual.servicios.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfesorService profesorService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private GrupoMuscularService grupoMuscularService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private PlanPublicoService planPublicoService;

    @Autowired
    private ConfiguracionPaginaPublicaService configuracionPaginaPublicaService;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private ExerciseCargaDefaultOptimizado exerciseCargaDefaultOptimizado;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        long startTime = System.currentTimeMillis();
        log.info("=== Iniciando DataInitializer ===");

        try {
            boolean yaInicializado = isDataAlreadyInitialized();
            if (yaInicializado) {
                log.info("Sistema ya inicializado — modo rápido (grupos/categorías/config con salida temprana)");
            }

            createProfesorUsuarioIfNeeded();
            createDeveloperUsuarioIfNeeded();
            grupoMuscularService.asegurarGruposSistema();
            categoriaService.asegurarCategoriasSistema();
            asegurarEjerciciosPredeterminadosSiNecesario();
            planPublicoService.asegurarPlanesIniciales();
            configuracionPaginaPublicaService.asegurarConfigInicial();

            if (!yaInicializado) {
                assignAvatarsIfNeeded();
                markAsInitialized();
            }
        } catch (Exception e) {
            log.error("Error en DataInitializer: {}", e.getMessage(), e);
        }

        log.info("=== DataInitializer completado en {}ms ===", System.currentTimeMillis() - startTime);
    }

    private static final String CORREO_PROFESOR = "profesor@migymvirtual.com";
    private static final String CORREO_DEVELOPER = "lucerogustavosi@gmail.com";
    private static final String PASSWORD_DEVELOPER = "Qbasic.1977";

    /**
     * Verifica si los datos ya fueron inicializados previamente
     */
    private boolean isDataAlreadyInitialized() {
        try {
            return usuarioRepository.findFirstByCorreo(CORREO_PROFESOR)
                    .map(u -> u.getAvatar() != null && !u.getAvatar().isEmpty())
                    .orElse(false);
        } catch (Exception e) {
            log.warn("Error verificando estado de inicialización: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Crea el único usuario que gestiona el panel: Profesor (rol ADMIN) vinculado a la entidad Profesor.
     * Este usuario crea alumnos, asigna rutinas y lleva el control.
     */
    private void createProfesorUsuarioIfNeeded() {
        try {
            Optional<Usuario> usuarioExistente = usuarioRepository.findFirstByCorreo(CORREO_PROFESOR);
            if (usuarioExistente.isPresent()) {
                Usuario usuario = usuarioExistente.get();
                boolean actualizado = false;
                if (!"ADMIN".equals(usuario.getRol())) {
                    usuario.setRol("ADMIN");
                    actualizado = true;
                }

                if (usuario.getProfesor() == null) {
                    Profesor profesor = profesorService.getProfesorByCorreo(CORREO_PROFESOR);
                    if (profesor == null) {
                        profesor = new Profesor();
                        profesor.setNombre("Profesor");
                        profesor.setApellido("");
                        profesor.setEdad(30);
                        profesor.setSexo("No especificado");
                        profesor.setEstablecimiento("-");
                        profesor.setCorreo(CORREO_PROFESOR);
                        profesor.setTelefono("-");
                        profesorService.guardarProfesor(profesor);
                        log.info("Entidad Profesor creada");
                    }
                    usuario.setProfesor(profesor);
                    actualizado = true;
                }

                if (actualizado) {
                    usuarioRepository.save(usuario);
                    log.info("Usuario administrador actualizado");
                } else {
                    log.info("Usuario administrador ya existe");
                }
                return;
            }
            log.info("Creando usuario Administrador (único gestor del panel)...");

            Profesor profesor = profesorService.getProfesorByCorreo(CORREO_PROFESOR);
            if (profesor == null) {
                profesor = new Profesor();
                profesor.setNombre("Profesor");
                profesor.setApellido("");
                profesor.setEdad(30);
                profesor.setSexo("No especificado");
                profesor.setEstablecimiento("-");
                profesor.setCorreo(CORREO_PROFESOR);
                profesor.setTelefono("-");
                profesorService.guardarProfesor(profesor);
                log.info("Entidad Profesor creada");
            }

            Usuario usuario = new Usuario();
            usuario.setNombre("Administrador");
            usuario.setCorreo(CORREO_PROFESOR);
            usuario.setPassword(passwordEncoder.encode("profesor"));
            usuario.setRol("ADMIN");
            usuario.setEdad(30);
            usuario.setSexo("No especificado");
            usuario.setAvatar("/img/avatar1.png");
            usuario.setProfesor(profesor);

            usuarioRepository.save(usuario);
            log.info("Usuario Administrador creado y vinculado (correo: {})", CORREO_PROFESOR);
        } catch (Exception e) {
            log.error("Error creando usuario profesor: {}", e.getMessage(), e);
        }
    }

    private void createDeveloperUsuarioIfNeeded() {
        try {
            Optional<Usuario> usuarioExistente = usuarioRepository.findFirstByCorreo(CORREO_DEVELOPER);
            if (usuarioExistente.isPresent()) {
                Usuario usuario = usuarioExistente.get();
                boolean actualizado = false;
                if (!"DEVELOPER".equals(usuario.getRol())) {
                    usuario.setRol("DEVELOPER");
                    actualizado = true;
                }
                if (usuario.getProfesor() != null) {
                    usuario.setProfesor(null);
                    actualizado = true;
                }
                if (actualizado) {
                    usuarioRepository.save(usuario);
                }
                log.info("Usuario developer ya existe");
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setNombre("Developer");
            usuario.setCorreo(CORREO_DEVELOPER);
            usuario.setPassword(passwordEncoder.encode(PASSWORD_DEVELOPER));
            usuario.setRol("DEVELOPER");
            usuario.setEdad(0);
            usuario.setSexo("No especificado");
            usuario.setAvatar("/img/avatar1.png");

            usuarioRepository.save(usuario);
            log.info("Usuario developer creado (correo: {})", CORREO_DEVELOPER);
        } catch (Exception e) {
            log.error("Error creando usuario developer: {}", e.getMessage(), e);
        }
    }

    /**
     * Asigna avatares solo si es necesario
     */
    private void assignAvatarsIfNeeded() {
        try {
            // Solo ejecutar si hay usuarios sin avatar
            long usuariosSinAvatar = usuarioRepository.countByAvatarIsNullOrAvatar("");
            if (usuariosSinAvatar > 0) {
                log.info("Asignando avatares a {} usuarios...", usuariosSinAvatar);
                usuarioService.asignarAvataresAUsuariosExistentes();
                log.info("Avatares asignados correctamente");
            } else {
                log.info("Todos los usuarios ya tienen avatares asignados");
            }
        } catch (Exception e) {
            log.error("Error asignando avatares: {}", e.getMessage(), e);
        }
    }

    /**
     * Carga los 60 ejercicios predeterminados si no existen.
     * Necesario para que los scripts de prueba (03_series, 04_rutinas) funcionen correctamente.
     */
    private void asegurarEjerciciosPredeterminadosSiNecesario() {
        try {
            if (exerciseService.countEjerciciosPredeterminados() == 0) {
                log.info("Cargando 60 ejercicios predeterminados...");
                int cargados = exerciseCargaDefaultOptimizado.asegurarEjerciciosPredeterminados();
                log.info("Ejercicios predeterminados cargados: {}", cargados);
            }
        } catch (Exception e) {
            log.warn("No se pudieron cargar ejercicios predeterminados: {}", e.getMessage());
        }
    }

    /**
     * Marca el sistema como inicializado
     */
    private void markAsInitialized() {
        try {
            usuarioRepository.findFirstByCorreo(CORREO_PROFESOR).ifPresent(u ->
                    log.info("Sistema inicializado correctamente — Usuario: {} ({}) — Rol: {}",
                            u.getNombre(), u.getCorreo(), u.getRol()));
        } catch (Exception e) {
            log.warn("Error verificando inicialización: {}", e.getMessage());
        }
    }
}