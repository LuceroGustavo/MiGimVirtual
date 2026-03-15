package com.migimvirtual.servicios;

import com.migimvirtual.entidades.Asistencia;
import com.migimvirtual.entidades.GrupoMuscular;
import com.migimvirtual.entidades.Usuario;
import com.migimvirtual.repositorios.AsistenciaRepository;
import com.migimvirtual.repositorios.RutinaRepository;
import com.migimvirtual.repositorios.UsuarioRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Exporta alumnos del profesor a Excel.
 * Incluye: datos del alumno, cantidad de asignaciones, y una columna "Último trabajo" (fecha + grupos + observaciones del último progreso).
 */
@Service
public class AlumnoExportService {

    private static final DateTimeFormatter FECHA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_FORMAT_CORTO = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final DateTimeFormatter HORA_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final UsuarioRepository usuarioRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final RutinaRepository rutinaRepository;

    public AlumnoExportService(UsuarioRepository usuarioRepository,
                               AsistenciaRepository asistenciaRepository,
                               RutinaRepository rutinaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.rutinaRepository = rutinaRepository;
    }

    /**
     * Genera un Excel con los alumnos del profesor. Retorna el archivo como byte[].
     */
    @Transactional(readOnly = true)
    public byte[] exportarAlumnosAExcel(Long profesorId) throws Exception {
        List<Usuario> alumnos = usuarioRepository.findByProfesor_IdAndRol(profesorId, "ALUMNO");
        if (alumnos == null) alumnos = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Alumnos");
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dateStyle = crearEstiloFecha(workbook);
            CellStyle titleStyle = crearEstiloTitulo(workbook);
            CellStyle wrapStyle = crearEstiloWrap(workbook);

            int rowNum = 0;

            // 1. Título con fecha de exportación
            String titulo = "Exportación de alumnos fecha " + LocalDate.now().format(FECHA_FORMAT);
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(titulo);
            titleCell.setCellStyle(titleStyle);

            rowNum++; // fila en blanco

            // 2. Cabecera: datos del alumno + Cantidad de asignaciones + Último trabajo (una columna al final)
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = getHeaders();
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 3. Una fila por alumno (sin columnas Ev); última columna = Último trabajo (fecha + dato)
            for (Usuario u : alumnos) {
                int countRutinas = rutinaRepository.findByUsuarioIdAndEsPlantillaFalse(u.getId()).size();
                List<Asistencia> asistencias = asistenciaRepository.findByUsuario_IdOrderByFechaDesc(u.getId());
                Asistencia ultima = (asistencias != null && !asistencias.isEmpty()) ? asistencias.get(0) : null;
                String textoUltimoTrabajo = formatearUltimoTrabajo(ultima);

                Row row = sheet.createRow(rowNum++);
                int col = 0;
                row.createCell(col++).setCellValue(toString(u.getNombre()));
                row.createCell(col++).setCellValue(toString(u.getCorreo()));
                row.createCell(col++).setCellValue(toString(u.getCelular()));
                row.createCell(col++).setCellValue(u.getEdad());
                row.createCell(col++).setCellValue(toString(u.getSexo()));
                row.createCell(col++).setCellValue(toString(u.getEstadoAlumno()));
                col = setCellFecha(row, col, u.getFechaAlta(), dateStyle);
                col = setCellFecha(row, col, u.getFechaBaja(), dateStyle);
                row.createCell(col++).setCellValue("Virtual");
                row.createCell(col++).setCellValue("");
                row.createCell(col++).setCellValue(toString(u.getObjetivosPersonales()));
                row.createCell(col++).setCellValue(toString(u.getRestriccionesMedicas()));
                row.createCell(col++).setCellValue(toString(u.getNotasProfesor()));
                row.createCell(col++).setCellValue(countRutinas);
                Cell cellUltimo = row.createCell(col);
                cellUltimo.setCellValue(textoUltimoTrabajo);
                cellUltimo.setCellStyle(wrapStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static String[] getHeaders() {
        return new String[]{
                "Nombre", "Correo", "Celular", "Edad", "Sexo", "Estado",
                "Fecha de alta", "Fecha baja", "Tipo de asistencia", "Días y horarios",
                "Objetivos personales", "Restricciones médicas", "Notas profesor",
                "Cantidad de asignaciones",
                "Último trabajo"
        };
    }

    /**
     * Formato: primera línea = fecha (dd/MM/yy), segunda línea = grupos - observaciones (ej. "CARDIO - CORE - trabajo muy bien").
     * Si no hay último trabajo, devuelve cadena vacía.
     */
    private static String formatearUltimoTrabajo(Asistencia ultima) {
        if (ultima == null) return "";
        String fecha = ultima.getFecha() != null ? ultima.getFecha().format(FECHA_FORMAT_CORTO) : "";
        Set<GrupoMuscular> grupos = ultima.getGruposTrabajados();
        String gruposStr = (grupos != null && !grupos.isEmpty())
                ? grupos.stream().map(GrupoMuscular::getNombre).filter(n -> n != null && !n.isBlank()).collect(Collectors.joining(" - "))
                : "";
        String obs = ultima.getObservaciones() != null ? ultima.getObservaciones().trim() : "";
        String dato = gruposStr;
        if (!obs.isEmpty()) dato = dato.isEmpty() ? obs : dato + " - " + obs;
        if (dato.isEmpty() && fecha.isEmpty()) return "";
        if (dato.isEmpty()) return fecha;
        if (fecha.isEmpty()) return dato;
        return fecha + "\n" + dato;
    }

    private static CellStyle crearEstiloTitulo(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 12);
        s.setFont(f);
        return s;
    }

    private static String toString(Object o) {
        return o == null ? "" : String.valueOf(o).trim();
    }

    private static int setCellFecha(Row row, int col, LocalDate fecha, CellStyle dateStyle) {
        Cell cell = row.createCell(col++);
        if (fecha != null) {
            cell.setCellValue(fecha.format(FECHA_FORMAT));
            cell.setCellStyle(dateStyle);
        }
        return col;
    }

    private static CellStyle crearEstiloCabecera(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        Font f = wb.createFont();
        f.setBold(true);
        s.setFont(f);
        return s;
    }

    private static CellStyle crearEstiloFecha(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setDataFormat(wb.createDataFormat().getFormat("dd/mm/yyyy"));
        return s;
    }

    private static CellStyle crearEstiloWrap(Workbook wb) {
        CellStyle s = wb.createCellStyle();
        s.setWrapText(true);
        return s;
    }
}
