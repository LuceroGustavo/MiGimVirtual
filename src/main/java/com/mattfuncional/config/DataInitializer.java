package com.mattfuncional.config;

import com.mattfuncional.entidades.Profesor;
import com.mattfuncional.entidades.Usuario;
import com.mattfuncional.repositorios.UsuarioRepository;
import com.mattfuncional.servicios.ConfiguracionPaginaPublicaService;
import com.mattfuncional.servicios.GrupoMuscularService;
import com.mattfuncional.servicios.PlanPublicoService;
import com.mattfuncional.servicios.ProfesorService;
import com.mattfuncional.servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfesorService profesorService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private GrupoMuscularService grupoMuscularService;

    @Autowired
    private PlanPublicoService planPublicoService;

    @Autowired
    private ConfiguracionPaginaPublicaService configuracionPaginaPublicaService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        long startTime = System.currentTimeMillis();
            System.out.println("=== Iniciando DataInitializer ===");

        try {
            // Verificar si ya se ejecutó antes (optimización)
            if (isDataAlreadyInitialized()) {
                System.out.println("✅ Datos ya inicializados - Saltando inicialización completa");
                createProfesorUsuarioIfNeeded();
                createDeveloperUsuarioIfNeeded();
                grupoMuscularService.asegurarGruposSistema();
                planPublicoService.asegurarPlanesIniciales();
                configuracionPaginaPublicaService.asegurarConfigInicial();
                System.out.println("=== DataInitializer completado en " + (System.currentTimeMillis() - startTime) + "ms ===");
                return;
            }

            // Crear el usuario principal que maneja el panel: el Profesor (rol ADMIN, vinculado a entidad Profesor)
            createProfesorUsuarioIfNeeded();
            // Crear usuario developer del sistema
            createDeveloperUsuarioIfNeeded();

            // Asegurar los 6 grupos musculares del sistema (BRAZOS, PIERNAS, PECHO, ESPALDA, CARDIO, ELONGACION)
            grupoMuscularService.asegurarGruposSistema();
            // Asegurar planes y configuración de la página pública
            planPublicoService.asegurarPlanesIniciales();
            configuracionPaginaPublicaService.asegurarConfigInicial();
            
            // Asignar avatares solo si es necesario
            assignAvatarsIfNeeded();
            
            // Marcar como inicializado
            markAsInitialized();
            
        } catch (Exception e) {
            System.err.println("❌ Error en DataInitializer: " + e.getMessage());
            e.printStackTrace();
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("=== DataInitializer completado en " + totalTime + "ms ===");
    }

    private static final String CORREO_PROFESOR = "profesor@mattfuncional.com";
    private static final String CORREO_DEVELOPER = "developer@mattfuncional.com";
    private static final String PASSWORD_DEVELOPER = "Qbasic.1977.mattfuncional";

    /**
     * Verifica si los datos ya fueron inicializados previamente
     */
    private boolean isDataAlreadyInitialized() {
        try {
            return usuarioRepository.findFirstByCorreo(CORREO_PROFESOR)
                    .map(u -> u.getAvatar() != null && !u.getAvatar().isEmpty())
                    .orElse(false);
        } catch (Exception e) {
            System.out.println("⚠️ Error verificando estado de inicialización: " + e.getMessage());
            return false;
        }
    }

    /**
     * Crea el único usuario que gestiona el panel: Profesor (rol ADMIN) vinculado a la entidad Profesor.
     * Este usuario crea alumnos, asigna rutinas y lleva el control.
     */
    private void createProfesorUsuarioIfNeeded() {
        try {
            java.util.Optional<Usuario> usuarioExistente = usuarioRepository.findFirstByCorreo(CORREO_PROFESOR);
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
                        System.out.println("✅ Entidad Profesor creada");
                    }
                    usuario.setProfesor(profesor);
                    actualizado = true;
                }

                if (actualizado) {
                    usuarioRepository.save(usuario);
                    System.out.println("✅ Usuario administrador actualizado");
                } else {
                    System.out.println("ℹ️ Usuario administrador ya existe");
                }
                return;
            }
            System.out.println("🔧 Creando usuario Administrador (único gestor del panel)...");

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
                System.out.println("✅ Entidad Profesor creada");
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
            System.out.println("✅ Usuario Administrador creado y vinculado (correo: " + CORREO_PROFESOR + ")");
        } catch (Exception e) {
            System.err.println("❌ Error creando usuario profesor: " + e.getMessage());
        }
    }

    private void createDeveloperUsuarioIfNeeded() {
        try {
            java.util.Optional<Usuario> usuarioExistente = usuarioRepository.findFirstByCorreo(CORREO_DEVELOPER);
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
                System.out.println("ℹ️ Usuario developer ya existe");
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
            System.out.println("✅ Usuario developer creado (correo: " + CORREO_DEVELOPER + ")");
        } catch (Exception e) {
            System.err.println("❌ Error creando usuario developer: " + e.getMessage());
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
                System.out.println("🎨 Asignando avatares a " + usuariosSinAvatar + " usuarios...");
                usuarioService.asignarAvataresAUsuariosExistentes();
                System.out.println("✅ Avatares asignados correctamente");
            } else {
                System.out.println("ℹ️ Todos los usuarios ya tienen avatares asignados");
            }
            } catch (Exception e) {
            System.err.println("❌ Error asignando avatares: " + e.getMessage());
        }
    }

    /**
     * Marca el sistema como inicializado
     */
    private void markAsInitialized() {
        try {
            usuarioRepository.findFirstByCorreo(CORREO_PROFESOR).ifPresent(u -> {
                System.out.println("✅ Sistema inicializado correctamente:");
                System.out.println("   Usuario: " + u.getNombre() + " (" + u.getCorreo() + ")");
                System.out.println("   Rol: " + u.getRol());
            });
        } catch (Exception e) {
            System.err.println("⚠️ Error verificando inicialización: " + e.getMessage());
        }
    }
}