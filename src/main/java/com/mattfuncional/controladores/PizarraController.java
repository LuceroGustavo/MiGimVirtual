package com.mattfuncional.controladores;

import com.mattfuncional.entidades.Exercise;
import com.mattfuncional.entidades.GrupoMuscular;
import com.mattfuncional.entidades.Pizarra;
import com.mattfuncional.entidades.Profesor;
import com.mattfuncional.entidades.Usuario;
import com.mattfuncional.servicios.ExerciseService;
import com.mattfuncional.servicios.GrupoMuscularService;
import com.mattfuncional.servicios.PizarraService;
import com.mattfuncional.servicios.ProfesorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/profesor/pizarra")
public class PizarraController {

    @Autowired
    private PizarraService pizarraService;

    @Autowired
    private ProfesorService profesorService;

    @Autowired
    private ExerciseService exerciseService;

    @Autowired
    private GrupoMuscularService grupoMuscularService;

    private Profesor getProfesorAcceso(Usuario usuario) {
        if (usuario == null) return null;
        if ("DEVELOPER".equals(usuario.getRol())) {
            return profesorService.getProfesorByCorreo("profesor@mattfuncional.com");
        }
        if (usuario.getProfesor() != null) return usuario.getProfesor();
        return profesorService.getProfesorByCorreo(usuario.getCorreo());
    }

    /** Redirige al panel de trabajo (pizarra en vivo). */
    @GetMapping
    public String index(@AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return "redirect:/login";
        return "redirect:/profesor/pizarra/panel";
    }

    /** Panel de trabajo: obtiene o crea la pizarra de trabajo (4 columnas) y abre el editor. */
    @GetMapping("/panel")
    public String panel(@AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return "redirect:/login";
        Pizarra p = pizarraService.getOrCreatePizarraTrabajo(profesor);
        return "redirect:/profesor/pizarra/editar/" + p.getId();
    }

    /** API: listado de pizarras del profesor (id, nombre, cantidadColumnas) para el modal "Insertar existente". */
    @GetMapping("/api/listado")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> apiListado(@AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        List<Pizarra> pizarras = pizarraService.listarPorProfesor(profesor.getId());
        List<Map<String, Object>> out = new ArrayList<>();
        for (Pizarra p : pizarras) {
            Map<String, Object> m = new java.util.HashMap<>();
            m.put("id", p.getId());
            m.put("nombre", p.getNombre() != null ? p.getNombre() : "");
            m.put("cantidadColumnas", p.getCantidadColumnas());
            out.add(m);
        }
        return ResponseEntity.ok(out);
    }

    /** Lista de pizarras guardadas (administrar). */
    @GetMapping("/lista")
    public String listar(Model model, @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return "redirect:/login";
        List<Pizarra> pizarras = pizarraService.listarPorProfesor(profesor.getId());
        model.addAttribute("pizarras", pizarras);
        return "profesor/pizarra-lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model model, @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return "redirect:/login";
        model.addAttribute("cantidadColumnas", 1);
        return "profesor/pizarra-nueva";
    }

    @PostMapping("/crear")
    public String crear(@RequestParam String nombre,
                       @RequestParam(defaultValue = "1") int cantidadColumnas,
                       @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return "redirect:/login";
        Pizarra p = pizarraService.crear(profesor, nombre, cantidadColumnas);
        return "redirect:/profesor/pizarra/editar/" + p.getId();
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model,
                        @AuthenticationPrincipal Usuario usuario,
                        @RequestParam(name = "grupoId", required = false) Long grupoId,
                        @RequestParam(name = "search", required = false) String search) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return "redirect:/login";
        Pizarra p = pizarraService.obtenerPorId(id)
                .orElseThrow(() -> new RuntimeException("Pizarra no encontrada"));
        if (!p.getProfesor().getId().equals(profesor.getId())) {
            return "redirect:/profesor/pizarra/lista?error=permiso";
        }
        List<Exercise> ejercicios = exerciseService.findEjerciciosDisponiblesParaProfesorWithImages(profesor.getId());
        if (grupoId != null) {
            ejercicios = ejercicios.stream()
                    .filter(e -> e.getGrupos() != null && e.getGrupos().stream().anyMatch(g -> grupoId.equals(g.getId())))
                    .toList();
        }
        if (search != null && !search.trim().isEmpty()) {
            String searchLower = search.toLowerCase();
            ejercicios = ejercicios.stream()
                    .filter(e -> e.getName().toLowerCase().contains(searchLower) ||
                            (e.getDescription() != null && e.getDescription().toLowerCase().contains(searchLower)))
                    .toList();
        }
        List<GrupoMuscular> gruposMusculares = grupoMuscularService.findDisponiblesParaProfesor(profesor.getId());
        List<Pizarra> todasPizarras = pizarraService.listarPorProfesor(profesor.getId());
        var salaActual = pizarraService.findSalaTransmisionByProfesor(profesor.getId());
        model.addAttribute("pizarra", p);
        model.addAttribute("ejercicios", ejercicios);
        model.addAttribute("gruposMusculares", gruposMusculares);
        model.addAttribute("selectedGrupoId", grupoId);
        model.addAttribute("pizarrasParaInsertar", todasPizarras);
        model.addAttribute("tokenSala", salaActual.map(s -> s.getToken()).orElse(null));
        model.addAttribute("salaTienePin", salaActual.map(s -> s.getPinSalaHash() != null && !s.getPinSalaHash().isEmpty()).orElse(false));
        return "profesor/pizarra-editor";
    }

    @PostMapping("/actualizar-basico")
    @ResponseBody
    public ResponseEntity<?> actualizarBasico(@RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        Long id = Long.valueOf(body.get("id").toString());
        String nombre = body.get("nombre") != null ? body.get("nombre").toString().trim() : null;
        List<String> titulos = new ArrayList<>();
        Object titulosObj = body.get("titulos");
        if (titulosObj instanceof List<?> list) {
            for (Object t : list) {
                titulos.add(t != null ? t.toString().trim() : "");
            }
        }
        List<Integer> vueltas = new ArrayList<>();
        Object vueltasObj = body.get("vueltas");
        if (vueltasObj instanceof List<?> list) {
            for (Object v : list) {
                if (v == null || v.toString().trim().isEmpty()) {
                    vueltas.add(null);
                } else {
                    try {
                        vueltas.add(Integer.valueOf(v.toString().trim()));
                    } catch (NumberFormatException e) {
                        vueltas.add(null);
                    }
                }
            }
        }
        pizarraService.actualizarBasico(id, nombre, titulos, vueltas.isEmpty() ? null : vueltas, profesor.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/agregar-item")
    @ResponseBody
    public ResponseEntity<?> agregarItem(@RequestBody Map<String, Object> body,
                                         @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        Long columnaId = Long.valueOf(body.get("columnaId").toString());
        Long exerciseId = Long.valueOf(body.get("exerciseId").toString());
        Integer peso = body.get("peso") != null ? Integer.valueOf(body.get("peso").toString()) : null;
        Integer reps = body.get("repeticiones") != null ? Integer.valueOf(body.get("repeticiones").toString()) : null;
        String unidad = (String) body.get("unidad");
        if (unidad == null) unidad = "reps";
        var item = pizarraService.agregarItem(columnaId, exerciseId, peso, reps, unidad, profesor.getId());
        return ResponseEntity.ok(Map.of("id", item.getId()));
    }

    @PostMapping("/actualizar-item")
    @ResponseBody
    public ResponseEntity<?> actualizarItem(@RequestBody Map<String, Object> body,
                                            @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        Long itemId = Long.valueOf(body.get("itemId").toString());
        Integer peso = body.get("peso") != null ? Integer.valueOf(body.get("peso").toString()) : null;
        Integer reps = body.get("repeticiones") != null ? Integer.valueOf(body.get("repeticiones").toString()) : null;
        String unidad = (String) body.get("unidad");
        pizarraService.actualizarItem(itemId, peso, reps, unidad, profesor.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/eliminar-item")
    @ResponseBody
    public ResponseEntity<?> eliminarItem(@RequestBody Map<String, Object> body,
                                          @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        Long itemId = Long.valueOf(body.get("itemId").toString());
        pizarraService.eliminarItem(itemId, profesor.getId());
        return ResponseEntity.ok().build();
    }

    /** Reordena los ítems de una columna según el orden indicado (lista de itemIds). */
    @PostMapping("/reordenar-items")
    @ResponseBody
    public ResponseEntity<?> reordenarItems(@RequestBody Map<String, Object> body,
                                             @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        Long columnaId = Long.valueOf(body.get("columnaId").toString());
        @SuppressWarnings("unchecked")
        List<Number> raw = (List<Number>) body.get("itemIds");
        if (raw == null) return ResponseEntity.badRequest().body(Map.of("error", "itemIds requerido"));
        List<Long> itemIds = raw.stream().map(Number::longValue).toList();
        try {
            pizarraService.reordenarItems(columnaId, itemIds, profesor.getId());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/agregar-columna")
    @ResponseBody
    public ResponseEntity<?> agregarColumna(@RequestBody Map<String, Object> body,
                                          @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        Long pizarraId = Long.valueOf(body.get("pizarraId").toString());
        try {
            pizarraService.agregarColumna(pizarraId, profesor.getId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/quitar-columna")
    @ResponseBody
    public ResponseEntity<?> quitarColumna(@RequestBody Map<String, Object> body,
                                          @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        Long columnaId = Long.valueOf(body.get("columnaId").toString());
        try {
            pizarraService.quitarColumna(columnaId, profesor.getId());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Transmisión global: asigna la pizarra actual a la sala del profesor y opcionalmente el PIN. */
    @PostMapping("/transmitir")
    @ResponseBody
    public ResponseEntity<?> transmitir(@RequestBody Map<String, Object> body,
                                         @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        Long pizarraId = body.get("pizarraId") != null ? Long.valueOf(body.get("pizarraId").toString()) : null;
        String pin = body.get("pin") != null ? body.get("pin").toString() : null;
        if (pizarraId == null) return ResponseEntity.badRequest().body(Map.of("error", "pizarraId requerido"));
        try {
            pizarraService.setPizarraYPinSala(profesor.getId(), pizarraId, pin);
            var sala = pizarraService.getOrCreateSalaTransmision(profesor.getId());
            return ResponseEntity.ok(Map.of("token", sala.getToken()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Genera un nuevo enlace para la TV (el anterior deja de funcionar). Hay que configurar el código de nuevo. */
    @PostMapping("/transmitir/nuevo-enlace")
    @ResponseBody
    public ResponseEntity<?> transmitirNuevoEnlace(@RequestBody Map<String, Object> body,
                                                    @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        Long pizarraId = body.get("pizarraId") != null ? Long.valueOf(body.get("pizarraId").toString()) : null;
        if (pizarraId == null) return ResponseEntity.badRequest().body(Map.of("error", "pizarraId requerido"));
        try {
            String nuevoToken = pizarraService.rotarTokenSala(profesor.getId());
            pizarraService.setPizarraYPinSala(profesor.getId(), pizarraId, null);
            return ResponseEntity.ok(Map.of("token", nuevoToken));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/pin")
    @ResponseBody
    public ResponseEntity<?> setPinSala(@PathVariable Long id, @RequestBody Map<String, String> body,
                                       @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        String pin = body != null ? body.get("pin") : null;
        try {
            pizarraService.setPinSala(id, profesor.getId(), pin);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Guarda el contenido actual como una nueva pizarra (no reemplaza la actual). */
    @PostMapping("/guardar-como-nueva")
    @ResponseBody
    public ResponseEntity<?> guardarComoNueva(@RequestBody Map<String, Object> body,
                                              @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        Long pizarraId = body.get("pizarraId") != null ? Long.valueOf(body.get("pizarraId").toString()) : null;
        String nombre = body.get("nombre") != null ? body.get("nombre").toString().trim() : null;
        if (pizarraId == null || nombre == null || nombre.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "pizarraId y nombre requeridos"));
        }
        try {
            pizarraService.guardarComoNuevaPizarra(pizarraId, nombre, profesor.getId());
            return ResponseEntity.ok(Map.of("ok", true, "mensaje", "Pizarra guardada como \"" + nombre + "\". Seguís editando en el panel."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Clona el contenido de una pizarra en otra (solo del mismo profesor). */
    @PostMapping("/clonar")
    @ResponseBody
    public ResponseEntity<?> clonar(@RequestBody Map<String, Object> body,
                                   @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return ResponseEntity.status(401).build();
        Long origenId = body.get("pizarraOrigenId") != null ? Long.valueOf(body.get("pizarraOrigenId").toString()) : null;
        Long destinoId = body.get("pizarraDestinoId") != null ? Long.valueOf(body.get("pizarraDestinoId").toString()) : null;
        if (origenId == null || destinoId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "pizarraOrigenId y pizarraDestinoId requeridos"));
        }
        try {
            pizarraService.clonarPizarra(origenId, destinoId, profesor.getId());
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id, @AuthenticationPrincipal Usuario usuario) {
        Profesor profesor = getProfesorAcceso(usuario);
        if (profesor == null) return "redirect:/login";
        pizarraService.eliminar(id, profesor.getId());
        return "redirect:/profesor/pizarra/lista";
    }
}
