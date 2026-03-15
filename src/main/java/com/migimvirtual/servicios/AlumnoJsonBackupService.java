package com.migimvirtual.servicios;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.migimvirtual.entidades.*;
import com.migimvirtual.repositorios.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Exporta e importa alumnos en formato JSON (backup completo).
 * Incluye: datos del alumno y mediciones físicas.
 * No incluye: rutinas asignadas (se reasignan manualmente tras importar).
 */
@Service
public class AlumnoJsonBackupService {

    private static final String VERSION = "1.0";
    private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter HORA_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final UsuarioRepository usuarioRepository;
    private final MedicionFisicaRepository medicionFisicaRepository;
    private final GrupoMuscularService grupoMuscularService;
    private final UsuarioService usuarioService;
    private final PlatformTransactionManager transactionManager;
    private final ObjectMapper objectMapper;

    public AlumnoJsonBackupService(UsuarioRepository usuarioRepository,
                                   MedicionFisicaRepository medicionFisicaRepository,
                                   GrupoMuscularService grupoMuscularService,
                                   UsuarioService usuarioService,
                                   PlatformTransactionManager transactionManager) {
        this.usuarioRepository = usuarioRepository;
        this.medicionFisicaRepository = medicionFisicaRepository;
        this.grupoMuscularService = grupoMuscularService;
        this.usuarioService = usuarioService;
        this.transactionManager = transactionManager;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Exporta todos los alumnos del profesor a JSON.
     */
    @Transactional(readOnly = true)
    public byte[] exportarAlumnosAJson(Long profesorId) throws Exception {
        List<Usuario> alumnos = usuarioRepository.findByProfesor_IdAndRol(profesorId, "ALUMNO");
        if (alumnos == null) alumnos = new ArrayList<>();

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", VERSION);
        root.put("fecha", LocalDateTime.now().toString());
        root.put("profesorId", profesorId);
        root.put("cantidadAlumnos", alumnos.size());
        root.put("tipo", "backup_alumnos");

        List<Map<String, Object>> alumnosList = new ArrayList<>();
        for (Usuario u : alumnos) {
            Map<String, Object> alumnoMap = alumnoToMap(u);
            alumnosList.add(alumnoMap);
        }
        root.put("alumnos", alumnosList);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(root);
    }

    private Map<String, Object> alumnoToMap(Usuario u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("nombre", u.getNombre());
        m.put("correo", u.getCorreo());
        m.put("celular", u.getCelular());
        m.put("edad", u.getEdad());
        m.put("sexo", u.getSexo());
        m.put("peso", u.getPeso());
        m.put("estadoAlumno", u.getEstadoAlumno());
        m.put("fechaAlta", u.getFechaAlta() != null ? u.getFechaAlta().format(FECHA_FORMAT) : null);
        m.put("fechaBaja", u.getFechaBaja() != null ? u.getFechaBaja().format(FECHA_FORMAT) : null);
        m.put("fechaInicio", u.getFechaInicio() != null ? u.getFechaInicio().format(FECHA_FORMAT) : null);
        m.put("notasProfesor", u.getNotasProfesor());
        m.put("objetivosPersonales", u.getObjetivosPersonales());
        m.put("restriccionesMedicas", u.getRestriccionesMedicas());

        // Mediciones (cargar por repositorio; Usuario ya no tiene colección medicionesFisicas)
        List<Map<String, Object>> medicionesList = new ArrayList<>();
        List<MedicionFisica> mediciones = medicionFisicaRepository.findByUsuario_IdOrderByFechaDesc(u.getId());
        if (mediciones != null && !mediciones.isEmpty()) {
            List<MedicionFisica> ordenadas = mediciones.stream()
                    .sorted(Comparator.comparing(MedicionFisica::getFecha, Comparator.nullsLast(Comparator.reverseOrder())))
                    .collect(Collectors.toList());
            for (MedicionFisica mf : ordenadas) {
                Map<String, Object> mm = new LinkedHashMap<>();
                mm.put("fecha", mf.getFecha() != null ? mf.getFecha().format(FECHA_FORMAT) : null);
                mm.put("peso", mf.getPeso());
                mm.put("altura", mf.getAltura());
                mm.put("cintura", mf.getCintura());
                mm.put("pecho", mf.getPecho());
                mm.put("cadera", mf.getCadera());
                mm.put("biceps", mf.getBiceps());
                mm.put("muslo", mf.getMuslo());
                medicionesList.add(mm);
            }
        }
        m.put("mediciones", medicionesList);

        return m;
    }

    /**
     * Importa alumnos desde JSON. Agregar o Suplantar.
     * @param pisarTodos true = borra todos los alumnos del profesor e importa; false = agrega nuevos (omite correo duplicado)
     */
    @Transactional
    public Map<String, Object> importarDesdeJson(MultipartFile archivo, Profesor profesor, boolean pisarTodos) throws IOException {
        Map<String, Object> result = new HashMap<>();
        if (profesor == null) {
            result.put("success", false);
            result.put("message", "No se pudo determinar el profesor.");
            return result;
        }

        String json = new String(archivo.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
        @SuppressWarnings("unchecked")
        Map<String, Object> root = objectMapper.readValue(json, Map.class);

        if (!"backup_alumnos".equals(root.get("tipo")) && root.get("alumnos") == null) {
            result.put("success", false);
            result.put("message", "El archivo no es un backup válido de alumnos (falta 'alumnos' o 'tipo').");
            return result;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> alumnosData = (List<Map<String, Object>>) root.get("alumnos");
        if (alumnosData == null) alumnosData = new ArrayList<>();

        if (pisarTodos) {
            DefaultTransactionDefinition defBorrado = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionTemplate txBorrado = new TransactionTemplate(transactionManager, defBorrado);
            Boolean okBorrado = txBorrado.execute(status -> {
                List<Usuario> existentes = usuarioRepository.findByProfesor_IdAndRol(profesor.getId(), "ALUMNO");
                for (Usuario u : existentes) {
                    if (u.getId() != null) {
                        usuarioService.eliminarUsuario(u.getId());
                    }
                }
                return true;
            });
            if (!Boolean.TRUE.equals(okBorrado)) {
                result.put("success", false);
                result.put("message", "Error al borrar alumnos existentes para suplantar.");
                return result;
            }
        }

        int importados = 0;
        int omitidos = 0;
        List<String> errores = new ArrayList<>();

        for (int i = 0; i < alumnosData.size(); i++) {
            try {
                Map<String, Object> ad = alumnosData.get(i);
                String correo = (String) ad.get("correo");
                String nombre = toString(ad.get("nombre"));
                if (!pisarTodos) {
                    boolean yaExiste = false;
                    if (correo != null && !correo.isBlank()) {
                        yaExiste = usuarioRepository.findByCorreo(correo.trim()).isPresent();
                    } else if (nombre != null && !nombre.isBlank()) {
                        yaExiste = usuarioRepository.findFirstByProfesor_IdAndRolAndNombre(
                                profesor.getId(), "ALUMNO", nombre.trim()).isPresent();
                    }
                    if (yaExiste) {
                        omitidos++;
                        continue;
                    }
                }
                Usuario nuevo = mapToUsuario(ad, profesor);
                usuarioRepository.save(nuevo);

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> mediciones = (List<Map<String, Object>>) ad.get("mediciones");
                if (mediciones != null) {
                    for (Map<String, Object> mm : mediciones) {
                        MedicionFisica mf = mapToMedicionFisica(mm, nuevo);
                        if (mf != null) {
                            medicionFisicaRepository.save(mf);
                        }
                    }
                }

                importados++;
            } catch (Exception e) {
                errores.add("Fila " + (i + 1) + ": " + e.getMessage());
            }
        }

        if (importados > 0 || omitidos > 0) {
            usuarioService.evictCacheUsuarios();
        }
        result.put("success", errores.isEmpty());
        result.put("alumnosImportados", importados);
        result.put("alumnosOmitidos", omitidos);
        if (!errores.isEmpty()) {
            result.put("errores", errores);
            result.put("message", "Se importaron " + importados + " alumnos con " + errores.size() + " error(es).");
        } else {
            result.put("message", "Se importaron " + importados + " alumnos correctamente." + (omitidos > 0 ? " Omitidos (correo existente): " + omitidos : ""));
        }
        return result;
    }

    private Usuario mapToUsuario(Map<String, Object> m, Profesor profesor) {
        Usuario u = new Usuario();
        u.setProfesor(profesor);
        u.setRol("ALUMNO");
        u.setNombre(toString(m.get("nombre")));
        u.setCorreo(blankToNull(toString(m.get("correo"))));
        u.setCelular(toString(m.get("celular")));
        u.setEdad(toInt(m.get("edad"), 0));
        u.setSexo(toString(m.get("sexo")));
        u.setPeso(toDouble(m.get("peso"), 0.0));
        u.setEstadoAlumno(toString(m.get("estadoAlumno")) != null ? toString(m.get("estadoAlumno")) : "ACTIVO");
        u.setFechaAlta(parseFecha(m.get("fechaAlta")));
        u.setFechaBaja(parseFecha(m.get("fechaBaja")));
        u.setFechaInicio(parseFecha(m.get("fechaInicio")));
        u.setNotasProfesor(toString(m.get("notasProfesor")));
        u.setObjetivosPersonales(toString(m.get("objetivosPersonales")));
        u.setRestriccionesMedicas(toString(m.get("restriccionesMedicas")));
        u.setAvatar("/img/avatar" + ((int) (Math.random() * 8) + 1) + ".png");
        if (u.getFechaAlta() == null) u.setFechaAlta(LocalDate.now());
        return u;
    }

    private MedicionFisica mapToMedicionFisica(Map<String, Object> m, Usuario usuario) {
        LocalDate fecha = parseFecha(m.get("fecha"));
        if (fecha == null) return null;
        MedicionFisica mf = new MedicionFisica();
        mf.setUsuario(usuario);
        mf.setFecha(fecha);
        mf.setPeso(toDouble(m.get("peso"), null));
        mf.setAltura(toDouble(m.get("altura"), null));
        mf.setCintura(toDouble(m.get("cintura"), null));
        mf.setPecho(toDouble(m.get("pecho"), null));
        mf.setCadera(toDouble(m.get("cadera"), null));
        mf.setBiceps(toDouble(m.get("biceps"), null));
        mf.setMuslo(toDouble(m.get("muslo"), null));
        return mf;
    }

    private static String toString(Object o) {
        return o == null ? null : String.valueOf(o).trim();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    private static int toInt(Object o, int def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(String.valueOf(o));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static Double toDouble(Object o, Double def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).doubleValue();
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static LocalDate parseFecha(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o).trim();
        if (s.isEmpty()) return null;
        try {
            return LocalDate.parse(s, FECHA_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private static LocalTime parseHora(Object o) {
        if (o == null) return null;
        String s = String.valueOf(o).trim();
        if (s.isEmpty()) return null;
        try {
            return LocalTime.parse(s, HORA_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

}
