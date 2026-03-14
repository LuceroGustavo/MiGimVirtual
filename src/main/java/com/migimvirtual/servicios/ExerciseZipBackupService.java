package com.migimvirtual.servicios;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.migimvirtual.entidades.Exercise;
import com.migimvirtual.entidades.GrupoMuscular;
import com.migimvirtual.entidades.Imagen;
import com.migimvirtual.entidades.Profesor;
import com.migimvirtual.entidades.Rutina;
import com.migimvirtual.entidades.Serie;
import com.migimvirtual.entidades.SerieEjercicio;
import com.migimvirtual.repositorios.PizarraItemRepository;
import com.migimvirtual.repositorios.ProfesorRepository;
import com.migimvirtual.repositorios.RutinaRepository;
import com.migimvirtual.repositorios.SerieEjercicioRepository;
import com.migimvirtual.repositorios.SerieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Exporta e importa ejercicios, series y rutinas en ZIP.
 * Backup completo: ejercicios.json + rutinas.json + series.json + imagenes/.
 * Al restaurar con "suplantar", todo queda tal cual estaba.
 */
@Service
public class ExerciseZipBackupService {

    private static final Logger logger = LoggerFactory.getLogger(ExerciseZipBackupService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final ExerciseService exerciseService;
    private final GrupoMuscularService grupoMuscularService;
    private final ImagenServicio imagenServicio;
    private final SerieEjercicioRepository serieEjercicioRepository;
    private final PizarraItemRepository pizarraItemRepository;
    private final RutinaRepository rutinaRepository;
    private final SerieRepository serieRepository;
    private final ProfesorRepository profesorRepository;
    private final PlatformTransactionManager transactionManager;
    private final ObjectMapper objectMapper;

    public ExerciseZipBackupService(ExerciseService exerciseService,
                                    GrupoMuscularService grupoMuscularService,
                                    ImagenServicio imagenServicio,
                                    SerieEjercicioRepository serieEjercicioRepository,
                                    PizarraItemRepository pizarraItemRepository,
                                    RutinaRepository rutinaRepository,
                                    SerieRepository serieRepository,
                                    ProfesorRepository profesorRepository,
                                    PlatformTransactionManager transactionManager) {
        this.exerciseService = exerciseService;
        this.grupoMuscularService = grupoMuscularService;
        this.imagenServicio = imagenServicio;
        this.serieEjercicioRepository = serieEjercicioRepository;
        this.pizarraItemRepository = pizarraItemRepository;
        this.rutinaRepository = rutinaRepository;
        this.serieRepository = serieRepository;
        this.profesorRepository = profesorRepository;
        this.transactionManager = transactionManager;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Genera un ZIP con ejercicios, rutinas plantilla y sus series. Restaurar devuelve todo tal cual.
     */
    @Transactional(readOnly = true)
    public byte[] exportarEjerciciosAZip() throws IOException {
        List<Exercise> ejercicios = exerciseService.findAllExercisesWithImages();
        if (ejercicios.isEmpty()) {
            throw new RuntimeException("No hay ejercicios para exportar");
        }
        // Orden: predeterminados primero (1-60), luego los creados por el usuario (61+), por id
        ejercicios = new ArrayList<>(ejercicios);
        ejercicios.sort((a, b) -> {
            boolean pa = a.getEsPredeterminado() != null && a.getEsPredeterminado() || a.getProfesor() == null;
            boolean pb = b.getEsPredeterminado() != null && b.getEsPredeterminado() || b.getProfesor() == null;
            if (pa != pb) return pa ? -1 : 1;
            return Long.compare(a.getId() == null ? 0 : a.getId(), b.getId() == null ? 0 : b.getId());
        });
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<Rutina> rutinasPlantilla = rutinaRepository.findByEsPlantillaTrue();

        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // 1. manifest.json (series = standalone + las que pertenecen a rutinas)
            int totalSeries = serieRepository.findByEsPlantillaTrueAndRutinaIsNull().size();
            for (Rutina r : rutinasPlantilla) {
                Rutina rConSeries = rutinaRepository.findByIdWithSeries(r.getId()).orElse(r);
                if (rConSeries.getSeries() != null) totalSeries += rConSeries.getSeries().size();
            }
            List<GrupoMuscular> gruposMusculares = grupoMuscularService.findAll();
            Map<String, Object> manifest = new HashMap<>();
            manifest.put("version", "1.0");
            manifest.put("tipo", "completo");
            manifest.put("fecha", timestamp);
            manifest.put("cantidadEjercicios", ejercicios.size());
            manifest.put("cantidadGruposMusculares", gruposMusculares.size());
            manifest.put("cantidadRutinas", rutinasPlantilla.size());
            manifest.put("cantidadSeries", totalSeries);
            byte[] manifestBytes = objectMapper.writeValueAsString(manifest).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            zos.putNextEntry(new ZipEntry("manifest.json"));
            zos.write(manifestBytes);
            zos.closeEntry();

            // 2. grupos-musculares.json (sistema + de profesores; incluye los sin ejercicios vinculados)
            List<Map<String, Object>> gruposParaJson = new ArrayList<>();
            for (GrupoMuscular g : gruposMusculares) {
                Map<String, Object> item = new HashMap<>();
                item.put("nombre", g.getNombre());
                item.put("esSistema", g.getProfesor() == null);
                gruposParaJson.add(item);
            }
            byte[] gruposJsonBytes = objectMapper.writeValueAsString(gruposParaJson).getBytes(StandardCharsets.UTF_8);
            zos.putNextEntry(new ZipEntry("grupos-musculares.json"));
            zos.write(gruposJsonBytes);
            zos.closeEntry();

            // 3. ejercicios.json (con referencia a archivo de imagen, sin Base64)
            List<Map<String, Object>> ejerciciosParaJson = new ArrayList<>();
            int index = 0;
            for (Exercise ej : ejercicios) {
                Map<String, Object> item = new HashMap<>();
                item.put("name", ej.getName());
                item.put("description", ej.getDescription());
                item.put("type", ej.getType());
                item.put("videoUrl", ej.getVideoUrl());
                item.put("instructions", ej.getInstructions());
                item.put("benefits", ej.getBenefits());
                item.put("contraindications", ej.getContraindications());
                boolean esPredet = ej.getEsPredeterminado() != null && ej.getEsPredeterminado() || ej.getProfesor() == null;
                item.put("esPredeterminado", esPredet);
                item.put("ordenExport", index);
                if (ej.getGrupos() != null && !ej.getGrupos().isEmpty()) {
                    item.put("muscleGroups", ej.getGrupos().stream()
                            .map(GrupoMuscular::getNombre)
                            .collect(Collectors.toList()));
                }
                if (ej.getImagen() != null) {
                    // Mismo nombre que en uploads/ejercicios/ (1.webp, 44.gif o nombre_uuid.ext para usuario)
                    String nombreOriginal = ej.getImagen().getRutaArchivo();
                    String imagenArchivo = (nombreOriginal != null && !nombreOriginal.isBlank())
                        ? "imagenes/" + nombreOriginal
                        : "imagenes/ejercicio_" + index + extensionDesdeMime(ej.getImagen().getMime());
                    item.put("imagenArchivo", imagenArchivo);
                    item.put("tieneImagen", true);
                    item.put("mimeType", ej.getImagen().getMime());
                } else {
                    item.put("imagenArchivo", null);
                    item.put("tieneImagen", false);
                }
                ejerciciosParaJson.add(item);
                index++;
            }
            byte[] ejerciciosJsonBytes = objectMapper.writeValueAsString(ejerciciosParaJson).getBytes(java.nio.charset.StandardCharsets.UTF_8);
            zos.putNextEntry(new ZipEntry("ejercicios.json"));
            zos.write(ejerciciosJsonBytes);
            zos.closeEntry();

            // 3. imagenes/ — mismo nombre que en uploads/ejercicios/ (sin modificación)
            // Predeterminados: 1.webp, 2.gif, ... 60.webp. Usuario: nombre_uuid.ext (ej. curl_biceps_a1b2c3d4.jpg).
            index = 0;
            for (Exercise ejercicio : ejercicios) {
                if (ejercicio.getImagen() != null) {
                    byte[] imagenBytes = imagenServicio.obtenerContenidoSiExiste(ejercicio.getImagen().getId());
                    if (imagenBytes != null && imagenBytes.length > 0) {
                        String nombreEnCarpeta = ejercicio.getImagen().getRutaArchivo();
                        String entryName = (nombreEnCarpeta != null && !nombreEnCarpeta.isBlank())
                            ? "imagenes/" + nombreEnCarpeta
                            : "imagenes/ejercicio_" + index + extensionDesdeMime(ejercicio.getImagen().getMime());
                        zos.putNextEntry(new ZipEntry(entryName));
                        zos.write(imagenBytes);
                        zos.closeEntry();
                    } else {
                        logger.warn("Ejercicio \"{}\" tiene imagen en BD pero el archivo no existe en disco; se exporta sin imagen.", ejercicio.getName());
                    }
                }
                index++;
            }
            // 5. rutinas.json y series.json (backup completo)
            // Las series son independientes: pueden no tener rutina (biblioteca) o pertenecer a una rutina.
            List<Map<String, Object>> rutinasParaJson = new ArrayList<>();
            List<Map<String, Object>> seriesParaJson = new ArrayList<>();

            // 4a. Series sin rutina (standalone) primero, con rutinaIndex null
            List<Serie> seriesStandalone = serieRepository.findByEsPlantillaTrueAndRutinaIsNull();
            seriesStandalone.sort((a, b) -> Integer.compare(a.getOrden(), b.getOrden()));
            for (Serie serie : seriesStandalone) {
                Serie sConEj = serieRepository.findByIdWithSerieEjercicios(serie.getId()).orElse(serie);
                seriesParaJson.add(serieToMap(sConEj, null));
            }

            // 4b. Rutinas y sus series (cada serie con rutinaIndex = índice de la rutina)
            int rutinaIndex = 0;
            for (Rutina rutina : rutinasPlantilla) {
                Rutina rConSeries = rutinaRepository.findByIdWithSeries(rutina.getId()).orElse(rutina);
                Map<String, Object> rutinaItem = new HashMap<>();
                rutinaItem.put("nombre", rutina.getNombre());
                rutinaItem.put("descripcion", rutina.getDescripcion());
                rutinaItem.put("estado", rutina.getEstado());
                rutinaItem.put("categoria", rutina.getCategoria());
                rutinaItem.put("creador", rutina.getCreador());
                rutinaItem.put("esPlantilla", rutina.isEsPlantilla());
                rutinasParaJson.add(rutinaItem);

                if (rConSeries.getSeries() != null) {
                    List<Serie> seriesOrdenadas = new ArrayList<>(rConSeries.getSeries());
                    seriesOrdenadas.sort((a, b) -> Integer.compare(a.getOrden(), b.getOrden()));
                    for (Serie serie : seriesOrdenadas) {
                        Serie sConEj = serieRepository.findByIdWithSerieEjercicios(serie.getId()).orElse(serie);
                        seriesParaJson.add(serieToMap(sConEj, rutinaIndex));
                    }
                }
                rutinaIndex++;
            }
            byte[] rutinasBytes = objectMapper.writeValueAsString(rutinasParaJson).getBytes(StandardCharsets.UTF_8);
            zos.putNextEntry(new ZipEntry("rutinas.json"));
            zos.write(rutinasBytes);
            zos.closeEntry();
            byte[] seriesBytes = objectMapper.writeValueAsString(seriesParaJson).getBytes(StandardCharsets.UTF_8);
            zos.putNextEntry(new ZipEntry("series.json"));
            zos.write(seriesBytes);
            zos.closeEntry();
        }

        logger.info("ZIP generado: {} ejercicios, {} rutinas, {} bytes", ejercicios.size(), rutinasPlantilla.size(), baos.size());
        return baos.toByteArray();
    }

    /**
     * Importa desde un archivo ZIP según los flags. Solo se importa/borra lo seleccionado.
     * @param pisarTodos si true, se borran los datos actuales del tipo seleccionado antes de importar.
     * @param importarGrupos importar grupos musculares (idempotente).
     * @param importarEjercicios importar ejercicios.
     * @param importarRutinas importar rutinas (necesario para series que pertenecen a rutinas).
     * @param importarSeries importar series (requiere rutinas y ejercicios en BD).
     */
    @Transactional
    public Map<String, Object> importarDesdeZip(MultipartFile archivoZip, boolean pisarTodos, Profesor profesorParaRestore,
                                                 boolean importarGrupos, boolean importarEjercicios, boolean importarRutinas, boolean importarSeries) throws IOException {
        Map<String, Object> result = new HashMap<>();
        if (archivoZip == null || archivoZip.isEmpty()) {
            result.put("success", false);
            result.put("message", "No se envió ningún archivo");
            return result;
        }
        String nombreOriginal = archivoZip.getOriginalFilename() != null ? archivoZip.getOriginalFilename() : "";
        if (!nombreOriginal.toLowerCase().endsWith(".zip")) {
            result.put("success", false);
            result.put("message", "El archivo debe ser un ZIP exportado desde este sistema");
            return result;
        }

        Map<String, byte[]> zipEntries = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(archivoZip.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String name = entry.getName();
                if (name.contains("..")) continue;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buf = new byte[8192];
                int n;
                while ((n = zis.read(buf)) > 0) baos.write(buf, 0, n);
                zipEntries.put(name, baos.toByteArray());
            }
        }

        byte[] ejerciciosJsonBytes = zipEntries.get("ejercicios.json");
        if ((importarEjercicios || importarSeries) && ejerciciosJsonBytes == null) {
            result.put("success", false);
            result.put("message", "El ZIP no contiene ejercicios.json (necesario para ejercicios o series)");
            return result;
        }

        List<Map<String, Object>> ejerciciosData = new ArrayList<>();
        if (ejerciciosJsonBytes != null) {
            ejerciciosData = objectMapper.readValue(
                    new String(ejerciciosJsonBytes, StandardCharsets.UTF_8),
                    new TypeReference<List<Map<String, Object>>>() {});
        }

        // Orden de importación: predeterminados primero, luego user; mismo orden que en el export (ordenExport)
        ejerciciosData.sort((a, b) -> {
            boolean pa = a.get("esPredeterminado") == null || Boolean.TRUE.equals(a.get("esPredeterminado"));
            boolean pb = b.get("esPredeterminado") == null || Boolean.TRUE.equals(b.get("esPredeterminado"));
            if (pa != pb) return pa ? -1 : 1;
            int oa = a.get("ordenExport") instanceof Number ? ((Number) a.get("ordenExport")).intValue() : 0;
            int ob = b.get("ordenExport") instanceof Number ? ((Number) b.get("ordenExport")).intValue() : 0;
            return Integer.compare(oa, ob);
        });

        boolean esBackupCompleto = getZipEntryBytes(zipEntries, "rutinas.json") != null && getZipEntryBytes(zipEntries, "series.json") != null;

        if (!importarGrupos && !importarEjercicios && !importarRutinas && !importarSeries) {
            result.put("success", false);
            result.put("message", "Marcá al menos una opción: Grupos, Ejercicios, Rutinas o Series.");
            return result;
        }

        Profesor profesorRestore = profesorParaRestore != null
                ? profesorParaRestore
                : profesorRepository.findAll().stream().findFirst().orElse(null);

        // 1. Grupos musculares (si está seleccionado y vienen en el ZIP)
        int gruposImportados = 0;
        if (importarGrupos) {
            byte[] bytesGruposMusculares = getZipEntryBytes(zipEntries, "grupos-musculares.json");
            if (bytesGruposMusculares != null) {
                List<Map<String, Object>> gruposData = objectMapper.readValue(
                        new String(bytesGruposMusculares, StandardCharsets.UTF_8),
                        new TypeReference<List<Map<String, Object>>>() {});
                for (Map<String, Object> gd : gruposData) {
                    String nombreGrupo = (String) gd.get("nombre");
                    if (nombreGrupo == null || nombreGrupo.isBlank()) continue;
                    boolean esSistema = gd.get("esSistema") == null || Boolean.TRUE.equals(gd.get("esSistema"));
                    grupoMuscularService.ensureGrupoExiste(nombreGrupo.trim(), esSistema, esSistema ? null : profesorRestore);
                    gruposImportados++;
                }
                logger.info("Grupos musculares importados/asegurados: {}", gruposImportados);
            }
        }

        // 2. Borrado solo de lo que vamos a importar (Suplantar)
        if (pisarTodos && (importarSeries || importarRutinas || importarEjercicios)) {
            DefaultTransactionDefinition defBorrado = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionTemplate txBorrado = new TransactionTemplate(transactionManager, defBorrado);
            Boolean okBorrado = txBorrado.execute(status -> {
                if (importarSeries || importarEjercicios) {
                    int se = serieEjercicioRepository.deleteAllWithExercise();
                    int pi = pizarraItemRepository.deleteAllItems();
                    logger.info("Referencias eliminadas: {} SerieEjercicio, {} PizarraItem", se, pi);
                }
                if (importarSeries) {
                    serieRepository.deleteAll();
                    logger.info("Series eliminadas para suplantar");
                }
                if (importarRutinas) {
                    rutinaRepository.deleteAll();
                    logger.info("Rutinas eliminadas para suplantar");
                }
                if (importarEjercicios) {
                    List<Exercise> existentes = exerciseService.findAllExercisesWithImages();
                    for (Exercise e : existentes) {
                        exerciseService.deleteExercise(e.getId());
                    }
                    logger.info("Ejercicios existentes borrados: {}", existentes.size());
                }
                return true;
            });
            if (!Boolean.TRUE.equals(okBorrado)) {
                result.put("success", false);
                result.put("message", "Error al borrar datos previos para suplantar");
                return result;
            }
        }

        int importados = 0;
        int omitidos = 0;
        int conImagen = 0;
        List<String> errores = new ArrayList<>();
        Map<String, byte[]> zipEntriesFinal = zipEntries; // para uso en lambda
        Map<String, Exercise> ejercicioPorNombre = new HashMap<>();

        // 3. Ejercicios (si está seleccionado)
        if (importarEjercicios) {
        for (Map<String, Object> data : ejerciciosData) {
            String name = (String) data.get("name");
            if (name == null || name.isBlank()) {
                errores.add("Ejercicio sin nombre en el ZIP");
                continue;
            }
            // Solo en modo "Agregar" se omiten duplicados por nombre. Con Suplantar siempre se importa.
            if (!pisarTodos && exerciseService.findByNameAndProfesorNull(name).isPresent()) {
                omitidos++;
                continue;
            }
            // Cada ejercicio en su propia transacción (REQUIRES_NEW): si uno falla, no afecta al resto
            DefaultTransactionDefinition def = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            TransactionTemplate newTxTemplate = new TransactionTemplate(transactionManager, def);
            final String nombreEjercicio = name;
            Boolean ok = newTxTemplate.execute(status -> {
                try {
                    Exercise ejercicio = new Exercise();
                    ejercicio.setName(nombreEjercicio);
                    ejercicio.setDescription((String) data.get("description"));
                    ejercicio.setType((String) data.get("type"));
                    ejercicio.setVideoUrl((String) data.get("videoUrl"));
                    ejercicio.setInstructions((String) data.get("instructions"));
                    ejercicio.setBenefits((String) data.get("benefits"));
                    ejercicio.setContraindications((String) data.get("contraindications"));
                    ejercicio.setProfesor(null);
                    // Respetar esPredeterminado del backup: solo los del sistema llevan estrella; ZIP antiguos sin clave = true
                    boolean esPredet = data.get("esPredeterminado") == null || Boolean.TRUE.equals(data.get("esPredeterminado"));
                    ejercicio.setEsPredeterminado(esPredet);

                    if (data.get("muscleGroups") != null) {
                        @SuppressWarnings("unchecked")
                        List<String> nombres = (List<String>) data.get("muscleGroups");
                        Long profesorIdParaGrupos = profesorRestore != null ? profesorRestore.getId() : null;
                        ejercicio.setGrupos(grupoMuscularService.resolveGruposByNames(nombres, profesorIdParaGrupos));
                    }

                    String imagenArchivo = (String) data.get("imagenArchivo");
                    if (imagenArchivo != null && !imagenArchivo.isBlank()) {
                        byte[] imgBytes = zipEntriesFinal.get(imagenArchivo);
                        if (imgBytes != null && imgBytes.length > 0) {
                            Imagen img = imagenServicio.guardarParaRestore(imgBytes, imagenArchivo);
                            ejercicio.setImagen(img);
                        }
                    }
                    // Suplantar: guardar sin validar duplicados (ya se borró todo). Resto: validar por nombre.
                    if (pisarTodos) {
                        exerciseService.saveExerciseForRestore(ejercicio, null);
                    } else {
                        exerciseService.saveExercise(ejercicio, null);
                    }
                    // Usar este mapa al restaurar series (evita FK: no depender de findAll en la misma tx).
                    ejercicioPorNombre.put(nombreEjercicio, ejercicio);
                    return true;
                } catch (Exception e) {
                    errores.add(nombreEjercicio + ": " + e.getMessage());
                    logger.warn("Error importando ejercicio {}: {}", nombreEjercicio, e.getMessage());
                    status.setRollbackOnly(); // solo esta transacción interna
                    return false;
                }
            });
            if (Boolean.TRUE.equals(ok)) {
                importados++;
                if (data.get("imagenArchivo") != null && !((String) data.get("imagenArchivo")).isBlank()) {
                    byte[] imgBytes = zipEntriesFinal.get((String) data.get("imagenArchivo"));
                    if (imgBytes != null && imgBytes.length > 0) conImagen++;
                }
            }
        }
        } // fin if (importarEjercicios)

        // 4. Rutinas y series (si está seleccionado y el ZIP es backup completo)
        int rutinasImportadas = 0;
        int seriesImportadas = 0;
        if (esBackupCompleto && (importarRutinas || importarSeries)) {
            // Para series necesitamos ejercicios en BD: si no importamos ejercicios ahora, usar los existentes.
            if (importarSeries && !importarEjercicios) {
                for (Exercise ex : exerciseService.findAllExercisesWithImages()) {
                    ejercicioPorNombre.putIfAbsent(ex.getName(), ex);
                }
            }
            if (!pisarTodos && importarEjercicios) {
                for (Exercise ex : exerciseService.findAllExercisesWithImages()) {
                    ejercicioPorNombre.putIfAbsent(ex.getName(), ex);
                }
            }
            if (profesorRestore == null) {
                throw new RuntimeException("No hay profesor en el sistema para restaurar rutinas y series");
            }
            byte[] rutinasBytes = getZipEntryBytes(zipEntries, "rutinas.json");
            byte[] seriesBytes = getZipEntryBytes(zipEntries, "series.json");
            if (rutinasBytes == null || seriesBytes == null) {
                logger.warn("Backup completo esperado pero faltan archivos: rutinas.json={}, series.json={}",
                        rutinasBytes != null, seriesBytes != null);
            }
            if (rutinasBytes != null && seriesBytes != null) {
                List<Map<String, Object>> rutinasData = objectMapper.readValue(
                        new String(rutinasBytes, StandardCharsets.UTF_8),
                        new TypeReference<List<Map<String, Object>>>() {});
                List<Map<String, Object>> seriesData = objectMapper.readValue(
                        new String(seriesBytes, StandardCharsets.UTF_8),
                        new TypeReference<List<Map<String, Object>>>() {});

                List<Rutina> rutinasCreadas = new ArrayList<>();
                // Crear rutinas del ZIP cuando importamos rutinas o series (las series referencian por índice)
                for (Map<String, Object> rd : rutinasData) {
                    String nombreRutina = (String) rd.get("nombre");
                    if (nombreRutina == null || nombreRutina.isBlank()) continue;
                    Optional<Rutina> rutinaExistente = rutinaRepository.findFirstByNombreAndEsPlantillaTrueAndProfesorId(nombreRutina, profesorRestore.getId());
                    if (!pisarTodos && rutinaExistente.isPresent()) {
                        rutinasCreadas.add(rutinaExistente.get());
                        continue;
                    }
                    Rutina rutina = new Rutina();
                    rutina.setNombre(nombreRutina);
                    rutina.setDescripcion((String) rd.get("descripcion"));
                    rutina.setEstado(rd.get("estado") != null ? (String) rd.get("estado") : "ACTIVA");
                    rutina.setCategoria((String) rd.get("categoria"));
                    rutina.setCreador(rd.get("creador") != null ? (String) rd.get("creador") : "ADMIN");
                    rutina.setEsPlantilla(rd.get("esPlantilla") == null || Boolean.TRUE.equals(rd.get("esPlantilla")));
                    rutina.setProfesor(profesorRestore);
                    rutina.setUsuario(null);
                    rutina = rutinaRepository.save(rutina);
                    rutinasCreadas.add(rutina);
                    if (importarRutinas) rutinasImportadas++;
                }

                if (importarSeries) {
                for (Map<String, Object> sd : seriesData) {
                    // rutinaIndex null o < 0 = serie sin rutina (standalone); si es válido, vincular a esa rutina
                    Object ri = sd.get("rutinaIndex");
                    Rutina rutina = null;
                    if (ri != null && ri instanceof Number) {
                        int rutinaIdx = ((Number) ri).intValue();
                        if (rutinaIdx >= 0 && rutinaIdx < rutinasCreadas.size()) {
                            rutina = rutinasCreadas.get(rutinaIdx);
                        }
                    }

                    String nombreSerie = (String) sd.get("nombre");
                    if (nombreSerie == null || nombreSerie.isBlank()) continue;

                    // En modo Agregar: no duplicar series (mismo nombre en la misma rutina o standalone para el profesor).
                    // Usamos listas para no fallar si hay varias con el mismo nombre (ej. duplicados previos).
                    if (!pisarTodos) {
                        if (rutina != null) {
                            if (!serieRepository.findAllByNombreAndRutinaId(nombreSerie, rutina.getId()).isEmpty()) continue;
                        } else {
                            if (!serieRepository.findAllByNombreAndRutinaIsNullAndProfesor_Id(nombreSerie, profesorRestore.getId()).isEmpty()) continue;
                        }
                    }

                    Serie serie = new Serie();
                    serie.setNombre(nombreSerie);
                    serie.setOrden(toInt(sd.get("orden"), 0));
                    serie.setDescripcion((String) sd.get("descripcion"));
                    // En restore completo todas las series se consideran plantilla para que el panel muestre el total correcto
                    serie.setEsPlantilla(true);
                    serie.setRepeticionesSerie(toInt(sd.get("repeticionesSerie"), 1));
                    serie.setCreador(sd.get("creador") != null ? (String) sd.get("creador") : "ADMIN");
                    serie.setRutina(rutina);
                    serie.setProfesor(profesorRestore);
                    serie = serieRepository.save(serie);
                    seriesImportadas++;

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> seList = (List<Map<String, Object>>) sd.get("serieEjercicios");
                    if (seList != null) {
                        int orden = 0;
                        for (Map<String, Object> seMap : seList) {
                            String exerciseName = (String) seMap.get("exerciseName");
                            Exercise ex = ejercicioPorNombre.get(exerciseName);
                            if (ex == null) continue;
                            SerieEjercicio se = new SerieEjercicio();
                            se.setSerie(serie);
                            se.setExercise(ex);
                            se.setValor(toInteger(seMap.get("valor")));
                            se.setUnidad((String) seMap.get("unidad"));
                            se.setPeso(toInteger(seMap.get("peso")));
                            se.setOrden(toInt(seMap.get("orden"), orden));
                            serieEjercicioRepository.save(se);
                            orden++;
                        }
                    }
                }
                } // fin if (importarSeries)
                logger.info("Restore: {} rutinas, {} series (importarRutinas={}, importarSeries={}, pisarTodos={})", rutinasImportadas, seriesImportadas, importarRutinas, importarSeries, pisarTodos);
            }
        }

        result.put("success", true);
        result.put("ejerciciosImportados", importados);
        result.put("ejerciciosOmitidos", omitidos);
        result.put("ejerciciosConImagen", conImagen);
        result.put("gruposMuscularesImportados", gruposImportados);
        result.put("rutinasImportadas", rutinasImportadas);
        result.put("seriesImportadas", seriesImportadas);
        if (!errores.isEmpty()) {
            result.put("errores", errores);
        }
        logger.info("Importación ZIP: {} ejercicios, {} rutinas, {} series, pisarTodos={}", importados, rutinasImportadas, seriesImportadas, pisarTodos);
        return result;
    }

    /** Obtiene bytes de una entrada del ZIP por nombre (exacto o ignorando mayúsculas / path). */
    private static byte[] getZipEntryBytes(Map<String, byte[]> zipEntries, String nombreArchivo) {
        if (zipEntries.containsKey(nombreArchivo)) return zipEntries.get(nombreArchivo);
        String lower = nombreArchivo.toLowerCase();
        for (Map.Entry<String, byte[]> e : zipEntries.entrySet()) {
            if (e.getKey().toLowerCase().equals(lower) || e.getKey().replace("\\", "/").toLowerCase().endsWith("/" + lower))
                return e.getValue();
        }
        return null;
    }

    /** Construye el mapa JSON de una serie para exportar (rutinaIndex null = serie sin rutina). */
    private Map<String, Object> serieToMap(Serie serie, Integer rutinaIndex) {
        Map<String, Object> serieItem = new HashMap<>();
        serieItem.put("rutinaIndex", rutinaIndex);
        serieItem.put("orden", serie.getOrden());
        serieItem.put("nombre", serie.getNombre());
        serieItem.put("descripcion", serie.getDescripcion());
        serieItem.put("esPlantilla", serie.isEsPlantilla());
        serieItem.put("repeticionesSerie", serie.getRepeticionesSerie());
        serieItem.put("creador", serie.getCreador());
        List<Map<String, Object>> seList = new ArrayList<>();
        if (serie.getSerieEjercicios() != null) {
            List<SerieEjercicio> seOrdenados = new ArrayList<>(serie.getSerieEjercicios());
            seOrdenados.sort((a, b) -> Integer.compare(
                    a.getOrden() != null ? a.getOrden().intValue() : 0,
                    b.getOrden() != null ? b.getOrden().intValue() : 0));
            for (SerieEjercicio se : seOrdenados) {
                if (se.getExercise() != null) {
                    Map<String, Object> seItem = new LinkedHashMap<>();
                    seItem.put("exerciseName", se.getExercise().getName());
                    seItem.put("valor", se.getValor());
                    seItem.put("unidad", se.getUnidad());
                    seItem.put("peso", se.getPeso());
                    seItem.put("orden", se.getOrden() != null ? se.getOrden().intValue() : 0);
                    seList.add(seItem);
                }
            }
        }
        serieItem.put("serieEjercicios", seList);
        return serieItem;
    }

    private static int toInt(Object o, int defaultValue) {
        return o instanceof Number ? ((Number) o).intValue() : defaultValue;
    }

    private static Integer toInteger(Object o) {
        if (o == null || !(o instanceof Number)) return null;
        return ((Number) o).intValue();
    }

    private static String extensionDesdeMime(String mime) {
        if (mime == null) return ".jpg";
        if (mime.contains("png")) return ".png";
        if (mime.contains("webp")) return ".webp";
        if (mime.contains("gif")) return ".gif";
        return ".jpg";
    }
}
