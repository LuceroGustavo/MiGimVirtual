package com.migimvirtual.servicios;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Guarda y lista backups en disco (ZIP de contenido y JSON de alumnos).
 * Mantiene como máximo {@link #MAX_BACKUPS} archivos por tipo; los más viejos se eliminan.
 * <p>
 * Al primer guardado crea automáticamente las carpetas {@code contenido/} y {@code alumnos/} bajo
 * {@code migimvirtual.backups.dir} (no hace falta crear {@code backup} a mano). En Ubuntu/producción
 * conviene una ruta absoluta y permisos de escritura; ver {@code Documentacion/servidor/DESPLIEGUE-SERVIDOR.md} §6.6.
 */
@Service
public class BackupStorageService {

    public static final int MAX_BACKUPS = 2;

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Value("${migimvirtual.backups.dir:backup}")
    private String backupRoot;

    public Path resolveRoot() {
        Path p = Paths.get(backupRoot);
        if (!p.isAbsolute()) {
            p = Paths.get(System.getProperty("user.dir")).resolve(backupRoot);
        }
        return p;
    }

    public Path dirContenido() throws IOException {
        Path d = resolveRoot().resolve("contenido");
        Files.createDirectories(d);
        return d;
    }

    public Path dirAlumnos() throws IOException {
        Path d = resolveRoot().resolve("alumnos");
        Files.createDirectories(d);
        return d;
    }

    public record BackupFileInfo(String nombre, long tamanoBytes, Instant ultimaModificacion) {}

    public List<BackupFileInfo> listarContenido() {
        try {
            return listar(dirContenido(), "contenido_", ".zip");
        } catch (IOException e) {
            return List.of();
        }
    }

    public List<BackupFileInfo> listarAlumnos() {
        try {
            return listar(dirAlumnos(), "alumnos_", ".json");
        } catch (IOException e) {
            return List.of();
        }
    }

    private List<BackupFileInfo> listar(Path dir, String prefix, String suffix) throws IOException {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        List<Path> paths = new ArrayList<>();
        try (Stream<Path> s = Files.list(dir)) {
            s.filter(p -> {
                String n = p.getFileName().toString();
                return n.startsWith(prefix) && n.endsWith(suffix);
            }).sorted(Comparator.comparing(Path::getFileName).reversed())
                    .forEach(paths::add);
        }
        List<BackupFileInfo> out = new ArrayList<>();
        for (Path p : paths) {
            out.add(new BackupFileInfo(
                    p.getFileName().toString(),
                    Files.size(p),
                    Files.getLastModifiedTime(p).toInstant()));
        }
        return out;
    }

    public String guardarContenidoZip(byte[] data) throws IOException {
        Path dir = dirContenido();
        String name = "contenido_" + LocalDateTime.now().format(STAMP) + ".zip";
        Files.write(dir.resolve(name), data);
        rotar(dir, "contenido_", ".zip");
        return name;
    }

    public String guardarAlumnosJson(byte[] data) throws IOException {
        Path dir = dirAlumnos();
        String name = "alumnos_" + LocalDateTime.now().format(STAMP) + ".json";
        Files.write(dir.resolve(name), data);
        rotar(dir, "alumnos_", ".json");
        return name;
    }

    private void rotar(Path dir, String prefix, String suffix) throws IOException {
        List<Path> files = new ArrayList<>();
        try (Stream<Path> s = Files.list(dir)) {
            s.filter(p -> {
                String n = p.getFileName().toString();
                return n.startsWith(prefix) && n.endsWith(suffix);
            }).sorted(Comparator.comparing(Path::getFileName).reversed())
                    .forEach(files::add);
        }
        for (int i = MAX_BACKUPS; i < files.size(); i++) {
            Files.deleteIfExists(files.get(i));
        }
    }

    public byte[] leerContenido(String nombreArchivo) throws IOException {
        validarNombreSeguro(nombreArchivo, "contenido_", ".zip");
        Path p = dirContenido().resolve(nombreArchivo);
        if (!Files.isRegularFile(p)) {
            throw new IOException("Archivo no encontrado: " + nombreArchivo);
        }
        return Files.readAllBytes(p);
    }

    public byte[] leerAlumnos(String nombreArchivo) throws IOException {
        validarNombreSeguro(nombreArchivo, "alumnos_", ".json");
        Path p = dirAlumnos().resolve(nombreArchivo);
        if (!Files.isRegularFile(p)) {
            throw new IOException("Archivo no encontrado: " + nombreArchivo);
        }
        return Files.readAllBytes(p);
    }

    private static void validarNombreSeguro(String nombre, String prefix, String suffix) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre inválido");
        }
        if (nombre.contains("..") || nombre.indexOf('/') >= 0 || nombre.indexOf('\\') >= 0) {
            throw new IllegalArgumentException("Nombre inválido");
        }
        if (!nombre.startsWith(prefix) || !nombre.endsWith(suffix)) {
            throw new IllegalArgumentException("Nombre inválido");
        }
    }
}
