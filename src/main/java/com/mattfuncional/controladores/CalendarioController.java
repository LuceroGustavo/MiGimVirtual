package com.mattfuncional.controladores;

import com.mattfuncional.dto.CalendarioSemanalDTO;
import com.mattfuncional.servicios.CalendarioService;
import com.mattfuncional.servicios.CalendarioExcepcionService;
import com.mattfuncional.servicios.AsistenciaService;
import com.mattfuncional.entidades.Usuario;
import com.mattfuncional.entidades.Profesor;
import com.mattfuncional.servicios.ProfesorService;
import com.mattfuncional.servicios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/calendario")
public class CalendarioController {

    @Autowired
    private CalendarioService calendarioService;

    @Autowired
    private ProfesorService profesorService;

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CalendarioExcepcionService calendarioExcepcionService;

    // @GetMapping("/semanal")
    // public String calendarioSemanal(@RequestParam(required = false) String fecha,
    // @AuthenticationPrincipal Usuario usuarioActual,
    // Model model) {
    //
    // LocalDate fechaCalendario = fecha != null ? LocalDate.parse(fecha) :
    // LocalDate.now();
    //
    // CalendarioSemanalDTO calendario =
    // calendarioService.generarCalendarioSemanal(fechaCalendario, null);
    // Map<String, Object> estadisticas =
    // calendarioService.obtenerEstadisticasSemana(fechaCalendario, null);
    //
    // model.addAttribute("calendario", calendario);
    // model.addAttribute("estadisticas", estadisticas);
    // model.addAttribute("fechaActual", fechaCalendario);
    // model.addAttribute("usuarioActual", usuarioActual);
    //
    // return "calendario/semanal";
    // }

    @GetMapping("/semanal/profesor/{profesorId}")
    public String calendarioSemanalProfesor(@PathVariable Long profesorId,
            @RequestParam(required = false) String fecha,
            @AuthenticationPrincipal Usuario usuarioActual,
            Model model) {
        // Solo el profesor correspondiente (o developer con profesor por defecto) puede ver este calendario
        if (usuarioActual != null) {
            Profesor profesorAcceso = getProfesorAcceso(usuarioActual);
            if (profesorAcceso != null && !profesorAcceso.getId().equals(profesorId)) {
                return "redirect:/profesor/dashboard";
            }
        }

        LocalDate fechaCalendario = fecha != null ? LocalDate.parse(fecha) : LocalDate.now();

        CalendarioSemanalDTO calendario = calendarioService.generarCalendarioSemanal(fechaCalendario, profesorId);
        // No se marcan ausentes automáticamente: por defecto todos quedan pendientes (feriados, profesor faltó, etc.)
        Map<String, Boolean> asistenciaMap = asistenciaService.getMapaPresentePorUsuarioYFecha(
                calendario.getFechaInicioSemana(), calendario.getFechaFinSemana());
        Map<String, Object> estadisticas = calendarioService.obtenerEstadisticasSemana(fechaCalendario, profesorId);

        Profesor profesor = profesorService.getProfesorById(profesorId);
        model.addAttribute("profesor", profesor);
        model.addAttribute("estadisticas", estadisticas);
        model.addAttribute("calendario", calendario);

        // Preparar lista de días de la semana (como Enum, para orden y acceso por
        // índice)
        java.util.List<com.mattfuncional.enums.DiaSemana> diasSemana = java.util.Arrays
                .asList(com.mattfuncional.enums.DiaSemana.values());
        model.addAttribute("diasSemana", diasSemana);

        // Preparar lista de listas de slots por día (en el mismo orden que diasSemana)
        java.util.List<java.util.List<com.mattfuncional.dto.CalendarioSemanalDTO.SlotHorarioDTO>> slotsPorDiaList = new java.util.ArrayList<>();
        int totalFilas = 0;
        for (com.mattfuncional.enums.DiaSemana dia : diasSemana) {
            java.util.List<com.mattfuncional.dto.CalendarioSemanalDTO.SlotHorarioDTO> slotsList = calendario.getSlotsPorDia()
                    .get(dia);
            if (slotsList == null)
                slotsList = new java.util.ArrayList<>();
            slotsPorDiaList.add(slotsList);
            if (slotsList.size() > totalFilas)
                totalFilas = slotsList.size();
        }
        // Rellenar presente/ausente por slot (para que la vista muestre verde/rojo/gris sin depender del mapa)
        LocalDate inicioSemana = calendario.getFechaInicioSemana();
        for (int diaIdx = 0; diaIdx < slotsPorDiaList.size(); diaIdx++) {
            java.time.LocalDate fechaDia = inicioSemana.plusDays(diaIdx);
            java.util.List<com.mattfuncional.dto.CalendarioSemanalDTO.SlotHorarioDTO> slotsDelDia = slotsPorDiaList.get(diaIdx);
            if (slotsDelDia == null) continue;
            for (com.mattfuncional.dto.CalendarioSemanalDTO.SlotHorarioDTO slot : slotsDelDia) {
                if (slot == null || slot.getUsuariosAsignados() == null) continue;
                for (Usuario u : slot.getUsuariosAsignados()) {
                    if (u != null && u.getId() != null) {
                        String clave = String.valueOf(u.getId()) + "_" + fechaDia.toString();
                        Boolean presente = asistenciaMap != null ? asistenciaMap.get(clave) : null;
                        slot.getPresentePorUsuarioId().put(u.getId(), presente);
                    }
                }
            }
        }

        model.addAttribute("slotsPorDiaList", slotsPorDiaList);
        model.addAttribute("totalFilas", totalFilas);

        // Preparar lista de horas (por índice, tomando la hora de inicio del primer día
        // que tenga slot en esa fila)
        java.util.List<String> horas = new java.util.ArrayList<>();
        for (int i = 0; i < totalFilas; i++) {
            String hora = "";
            for (java.util.List<com.mattfuncional.dto.CalendarioSemanalDTO.SlotHorarioDTO> slots : slotsPorDiaList) {
                if (slots != null && slots.size() > i && slots.get(i) != null) {
                    java.time.LocalTime horaInicio = slots.get(i).getHoraInicio();
                    if (horaInicio != null) {
                        hora = horaInicio.toString();
                        break;
                    }
                }
            }
            horas.add(hora);
        }
        model.addAttribute("horas", horas);
        model.addAttribute("profesorId", profesorId);
        model.addAttribute("fechaSemana", calendario.getFechaInicioSemana());

        List<Usuario> alumnosParaExcepcion = usuarioService.getAlumnosByProfesorId(profesorId);
        alumnosParaExcepcion = alumnosParaExcepcion.stream()
                .filter(a -> a != null && !"INACTIVO".equals(a.getEstadoAlumno()))
                .filter(a -> a.getTipoAsistencia() == com.mattfuncional.enums.TipoAsistencia.PRESENCIAL
                        || a.getTipoAsistencia() == com.mattfuncional.enums.TipoAsistencia.SEMIPRESENCIAL)
                .toList();
        model.addAttribute("alumnosParaExcepcion", alumnosParaExcepcion);

        return "calendario/semanal-profesor";
    }

    @GetMapping("/api/usuarios-horario")
    @ResponseBody
    public java.util.List<Usuario> obtenerUsuariosEnHorario(@RequestParam String dia,
            @RequestParam String horaInicio,
            @RequestParam String horaFin) {
        return calendarioService.obtenerUsuariosEnHorario(
                com.mattfuncional.enums.DiaSemana.valueOf(dia),
                java.time.LocalTime.parse(horaInicio),
                java.time.LocalTime.parse(horaFin));
    }

    @GetMapping("/api/disponibilidad")
    @ResponseBody
    public Map<String, Boolean> verificarDisponibilidad(@RequestParam String dia,
            @RequestParam String horaInicio,
            @RequestParam String horaFin) {
        boolean disponible = calendarioService.verificarDisponibilidad(
                com.mattfuncional.enums.DiaSemana.valueOf(dia),
                java.time.LocalTime.parse(horaInicio),
                java.time.LocalTime.parse(horaFin));

        return java.util.Map.of("disponible", disponible);
    }

    /**
     * Marca el estado de asistencia: PENDIENTE (sin registro), PRESENTE o AUSENTE.
     * Acepta "estado" (PENDIENTE/PRESENTE/AUSENTE) o, por compatibilidad, "presente" (true/false).
     */
    @PostMapping("/api/marcar-asistencia")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> marcarAsistencia(
            @RequestParam Long usuarioId,
            @RequestParam String fecha,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Boolean presente,
            @AuthenticationPrincipal Usuario usuarioActual) {
        Usuario alumno = usuarioService.getUsuarioById(usuarioId);
        if (alumno == null) {
            return ResponseEntity.badRequest().body(java.util.Map.of("ok", false, "error", "Usuario no encontrado"));
        }
        String estadoNorm;
        if (estado != null && !estado.isBlank()) {
            estadoNorm = estado.toUpperCase().trim();
            if (!java.util.Set.of("PENDIENTE", "PRESENTE", "AUSENTE").contains(estadoNorm)) {
                return ResponseEntity.badRequest().body(java.util.Map.of("ok", false, "error", "Estado inválido"));
            }
        } else {
            // Compatibilidad: si envían el viejo parámetro "presente" (true/false)
            estadoNorm = Boolean.TRUE.equals(presente) ? "PRESENTE" : "AUSENTE";
        }
        LocalDate fechaDate = LocalDate.parse(fecha);
        if ("PENDIENTE".equals(estadoNorm)) {
            asistenciaService.eliminarRegistroAsistencia(alumno, fechaDate);
            return ResponseEntity.ok(java.util.Map.of("ok", true, "estado", "PENDIENTE", "presente", false));
        }
        boolean presenteVal = "PRESENTE".equals(estadoNorm);
        asistenciaService.guardarOActualizarProgreso(alumno, fechaDate, presenteVal, null, null, usuarioActual);
        return ResponseEntity.ok(java.util.Map.of("ok", true, "estado", estadoNorm, "presente", presenteVal));
    }

    @PostMapping("/excepcion")
    public String crearExcepcion(
            @RequestParam Long profesorId,
            @RequestParam Long usuarioId,
            @RequestParam String fecha,
            @RequestParam String horaInicio,
            @RequestParam String horaFin,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) String fechaSemana) {
        LocalDate fechaDate = LocalDate.parse(fecha);
        LocalTime horaInicioTime = LocalTime.parse(horaInicio);
        LocalTime horaFinTime = LocalTime.parse(horaFin);
        calendarioExcepcionService.crearExcepcion(profesorId, usuarioId, fechaDate, horaInicioTime, horaFinTime, motivo);
        String fechaRedirect = (fechaSemana != null && !fechaSemana.isBlank()) ? fechaSemana : fecha;
        return "redirect:/calendario/semanal/profesor/" + profesorId + "?fecha=" + fechaRedirect;
    }

    @PostMapping("/actualizar-capacidad-maxima")
    public String actualizarCapacidadMaxima(@RequestParam int capacidadMaxima, @RequestParam Long profesorId) {
        // Actualiza la capacidad máxima global para todos los slots existentes
        for (com.mattfuncional.enums.DiaSemana dia : com.mattfuncional.enums.DiaSemana.values()) {
            java.time.LocalTime hora = java.time.LocalTime.of(6, 0);
            while (hora.isBefore(java.time.LocalTime.of(21, 0))) {
                calendarioService.getSlotConfigService().setCapacidadMaxima(dia, hora, capacidadMaxima);
                hora = hora.plusHours(1);
            }
        }
        return "redirect:/calendario/semanal/profesor/" + profesorId;
    }

    /** Profesor con el que trabaja el usuario actual (developer usa profesor por defecto). */
    private Profesor getProfesorAcceso(Usuario usuario) {
        if (usuario == null) return null;
        if ("DEVELOPER".equals(usuario.getRol())) {
            return profesorService.getProfesorByCorreo("profesor@mattfuncional.com");
        }
        if (usuario.getProfesor() != null) return usuario.getProfesor();
        return profesorService.getProfesorByCorreo(usuario.getCorreo());
    }
}