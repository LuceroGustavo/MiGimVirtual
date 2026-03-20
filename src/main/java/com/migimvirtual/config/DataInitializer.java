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
            System.out.println("=== Iniciando DataInitializer ===");

        try {
            // Verificar si ya se ejecutó antes (optimización)
            if (isDataAlreadyInitialized()) {
                System.out.println("✅ Datos ya inicializados - Saltando inicialización completa");
                createProfesorUsuarioIfNeeded();
                createDeveloperUsuarioIfNeeded();
                grupoMuscularService.asegurarGruposSistema();
                categoriaService.asegurarCategoriasSistema();
                asegurarEjerciciosPredeterminadosSiNecesario();
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
            // Asegurar las categorías del sistema (FUERZA, CARDIO, FLEXIBILIDAD, FUNCIONAL, HIIT)
            categoriaService.asegurarCategoriasSistema();
            // Asegurar los 60 ejercicios predeterminados (necesarios para scripts de prueba 03 y 04)
            asegurarEjerciciosPredeterminadosSiNecesario();
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
     * Carga los 60 ejercicios predeterminados si no existen.
     * Necesario para que los scripts de prueba (03_series, 04_rutinas) funcionen correctamente.
     */
    private void asegurarEjerciciosPredeterminadosSiNecesario() {
        try {
            if (exerciseService.countEjerciciosPredeterminados() == 0) {
                System.out.println("📦 Cargando 60 ejercicios predeterminados...");
                int cargados = exerciseCargaDefaultOptimizado.asegurarEjerciciosPredeterminados();
                System.out.println("✅ Ejercicios predeterminados cargados: " + cargados);
            }
        } catch (Exception e) {
            System.err.println("⚠️ No se pudieron cargar ejercicios predeterminados: " + e.getMessage());
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