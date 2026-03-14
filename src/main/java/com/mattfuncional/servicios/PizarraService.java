package com.mattfuncional.servicios;

import com.mattfuncional.dto.PizarraEstadoDTO;
import com.mattfuncional.entidades.Exercise;
import com.mattfuncional.entidades.GrupoMuscular;
import com.mattfuncional.entidades.Pizarra;
import com.mattfuncional.entidades.PizarraColumna;
import com.mattfuncional.entidades.PizarraItem;
import com.mattfuncional.entidades.PizarraTrabajo;
import com.mattfuncional.entidades.Profesor;
import com.mattfuncional.entidades.SalaTransmision;
import com.mattfuncional.repositorios.ExerciseRepository;
import com.mattfuncional.repositorios.PizarraColumnaRepository;
import com.mattfuncional.repositorios.PizarraItemRepository;
import com.mattfuncional.repositorios.PizarraRepository;
import com.mattfuncional.repositorios.PizarraTrabajoRepository;
import com.mattfuncional.repositorios.SalaTransmisionRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PizarraService {

    private static final SecureRandom TOKEN_RANDOM = new SecureRandom();

    private final PizarraRepository pizarraRepository;
    private final PizarraColumnaRepository columnaRepository;
    private final PizarraItemRepository itemRepository;
    private final ExerciseRepository exerciseRepository;
    private final PasswordEncoder passwordEncoder;
    private final PizarraTrabajoRepository pizarraTrabajoRepository;
    private final SalaTransmisionRepository salaTransmisionRepository;

    public PizarraService(PizarraRepository pizarraRepository,
                          PizarraColumnaRepository columnaRepository,
                          PizarraItemRepository itemRepository,
                          ExerciseRepository exerciseRepository,
                          PasswordEncoder passwordEncoder,
                          PizarraTrabajoRepository pizarraTrabajoRepository,
                          SalaTransmisionRepository salaTransmisionRepository) {
        this.pizarraRepository = pizarraRepository;
        this.columnaRepository = columnaRepository;
        this.itemRepository = itemRepository;
        this.exerciseRepository = exerciseRepository;
        this.passwordEncoder = passwordEncoder;
        this.pizarraTrabajoRepository = pizarraTrabajoRepository;
        this.salaTransmisionRepository = salaTransmisionRepository;
    }

    public List<Pizarra> listarPorProfesor(Long profesorId) {
        return pizarraRepository.findByProfesorIdOrderByFechaModificacionDesc(profesorId);
    }

    public Optional<Pizarra> obtenerPorId(Long id) {
        return pizarraRepository.findByIdWithColumnas(id);
    }

    public Optional<Pizarra> obtenerPorToken(String token) {
        return pizarraRepository.findByToken(token);
    }

    /**
     * Obtiene o crea la pizarra de trabajo del profesor (panel en vivo, 4 columnas).
     */
    public Pizarra getOrCreatePizarraTrabajo(Profesor profesor) {
        Optional<PizarraTrabajo> pt = pizarraTrabajoRepository.findByProfesorId(profesor.getId());
        if (pt.isPresent()) {
            return pizarraRepository.findByIdWithColumnas(pt.get().getPizarraId())
                    .orElseGet(() -> {
                        pizarraTrabajoRepository.delete(pt.get());
                        pizarraTrabajoRepository.flush();
                        return crearPizarraTrabajo(profesor);
                    });
        }
        return crearPizarraTrabajo(profesor);
    }

    private Pizarra crearPizarraTrabajo(Profesor profesor) {
        Pizarra p = crear(profesor, "Panel en vivo", 4);
        PizarraTrabajo pt = new PizarraTrabajo();
        pt.setProfesorId(profesor.getId());
        pt.setPizarraId(p.getId());
        pizarraTrabajoRepository.save(pt);
        return pizarraRepository.findByIdWithColumnas(p.getId()).orElse(p);
    }

    /**
     * Copia el contenido de la pizarra origen a la destino (solo del mismo profesor).
     * La destino queda con las mismas columnas e ítems (por valor).
     */
    public void clonarPizarra(Long origenId, Long destinoId, Long profesorId) {
        Pizarra origen = pizarraRepository.findByIdWithColumnas(origenId)
                .orElseThrow(() -> new RuntimeException("Pizarra origen no encontrada"));
        Pizarra destino = pizarraRepository.findByIdWithColumnas(destinoId)
                .orElseThrow(() -> new RuntimeException("Pizarra destino no encontrada"));
        if (!origen.getProfesor().getId().equals(profesorId) || !destino.getProfesor().getId().equals(profesorId)) {
            throw new RuntimeException("No tiene permisos");
        }
        destino.getColumnas().clear();
        List<PizarraColumna> destColumnas = columnaRepository.findByPizarraIdOrderByOrdenAsc(destino.getId());
        for (PizarraColumna dc : destColumnas) {
            for (PizarraItem it : itemRepository.findByColumnaIdOrderByOrdenAsc(dc.getId())) {
                itemRepository.delete(it);
            }
            columnaRepository.delete(dc);
        }
        destino.setCantidadColumnas(0);
        pizarraRepository.save(destino);

        int ordenCol = 0;
        for (PizarraColumna oc : columnaRepository.findByPizarraIdOrderByOrdenAsc(origen.getId())) {
            PizarraColumna nc = new PizarraColumna();
            nc.setPizarra(destino);
            nc.setTitulo(oc.getTitulo());
            nc.setOrden(ordenCol++);
            nc = columnaRepository.save(nc);
            int ordenItem = 0;
            for (PizarraItem oi : itemRepository.findByColumnaIdOrderByOrdenAsc(oc.getId())) {
                PizarraItem ni = new PizarraItem();
                ni.setColumna(nc);
                ni.setExercise(oi.getExercise());
                ni.setPeso(oi.getPeso());
                ni.setRepeticiones(oi.getRepeticiones());
                ni.setUnidad(oi.getUnidad());
                ni.setOrden(ordenItem++);
                itemRepository.save(ni);
            }
        }
        destino.setCantidadColumnas(ordenCol);
        pizarraRepository.save(destino);
    }

    /**
     * Guarda el contenido actual como una nueva pizarra con el nombre indicado.
     * La pizarra actual (origen) no se modifica; se crea una nueva en la lista del profesor.
     */
    public Pizarra guardarComoNuevaPizarra(Long pizarraOrigenId, String nuevoNombre, Long profesorId) {
        Pizarra origen = pizarraRepository.findByIdWithColumnas(pizarraOrigenId)
                .orElseThrow(() -> new RuntimeException("Pizarra no encontrada"));
        if (!origen.getProfesor().getId().equals(profesorId)) {
            throw new RuntimeException("No tiene permisos");
        }
        String nombre = nuevoNombre != null && !nuevoNombre.isBlank() ? nuevoNombre.trim() : "Pizarra";
        boolean yaExiste = pizarraRepository.findByProfesorIdOrderByFechaModificacionDesc(profesorId).stream()
                .anyMatch(p -> !p.getId().equals(pizarraOrigenId) && nombre.equalsIgnoreCase(p.getNombre() != null ? p.getNombre().trim() : ""));
        if (yaExiste) {
            throw new IllegalArgumentException("Ya tenés una pizarra guardada con el nombre \"" + nombre + "\". Usá otro nombre.");
        }
        int cantidadColumnas = columnaRepository.findByPizarraIdOrderByOrdenAsc(origen.getId()).size();
        if (cantidadColumnas < 1) cantidadColumnas = 1;
        Pizarra nueva = crear(origen.getProfesor(), nombre, cantidadColumnas);
        clonarPizarra(pizarraOrigenId, nueva.getId(), profesorId);
        return pizarraRepository.findByIdWithColumnas(nueva.getId()).orElse(nueva);
    }

    /**
     * Obtiene la sala de transmisión del profesor si ya existe (sin crearla).
     */
    public Optional<SalaTransmision> findSalaTransmisionByProfesor(Long profesorId) {
        return salaTransmisionRepository.findByProfesorId(profesorId);
    }

    /**
     * Obtiene o crea la sala de transmisión del profesor (un token por profesor).
     */
    public SalaTransmision getOrCreateSalaTransmision(Long profesorId) {
        return salaTransmisionRepository.findByProfesorId(profesorId)
                .orElseGet(() -> {
                    SalaTransmision s = new SalaTransmision();
                    s.setProfesorId(profesorId);
                    s.setToken(generarTokenSalaUnico());
                    return salaTransmisionRepository.save(s);
                });
    }

    private String generarTokenSalaUnico() {
        String token;
        do {
            int num = TOKEN_RANDOM.nextInt(1_000_000);
            token = "tv" + String.format("%06d", num);
        } while (salaTransmisionRepository.existsByToken(token) || pizarraRepository.existsByToken(token));
        return token;
    }

    /**
     * Genera un nuevo token para la sala del profesor (el enlace anterior deja de funcionar).
     * Se quita el PIN; el profesor debe configurarlo de nuevo. Se usa al cerrar sesión o al pulsar "Cambiar enlace".
     */
    public String rotarTokenSala(Long profesorId) {
        SalaTransmision s = salaTransmisionRepository.findByProfesorId(profesorId)
                .orElseGet(() -> {
                    SalaTransmision nueva = new SalaTransmision();
                    nueva.setProfesorId(profesorId);
                    return salaTransmisionRepository.save(nueva);
                });
        s.setToken(generarTokenSalaUnico());
        s.setPinSalaHash(null);
        salaTransmisionRepository.save(s);
        return s.getToken();
    }

    /**
     * Asigna la pizarra que se muestra en la sala del profesor y opcionalmente el PIN.
     */
    public void setPizarraYPinSala(Long profesorId, Long pizarraId, String pin) {
        SalaTransmision s = getOrCreateSalaTransmision(profesorId);
        s.setPizarraId(pizarraId);
        if (pin != null && !pin.trim().isEmpty()) {
            String limpio = pin.trim();
            if (limpio.length() != 4 || !limpio.matches("\\d{4}")) {
                throw new IllegalArgumentException("El código debe tener exactamente 4 dígitos");
            }
            s.setPinSalaHash(passwordEncoder.encode(limpio));
        } else {
            s.setPinSalaHash(null);
        }
        salaTransmisionRepository.save(s);
    }

    /**
     * Crea una nueva pizarra con N columnas vacías.
     */
    public Pizarra crear(Profesor profesor, String nombre, int cantidadColumnas) {
        if (cantidadColumnas < 1 || cantidadColumnas > 6) {
            throw new IllegalArgumentException("Cantidad de columnas debe ser entre 1 y 6");
        }
        Pizarra p = new Pizarra();
        p.setProfesor(profesor);
        p.setNombre(nombre != null && !nombre.isBlank() ? nombre.trim() : "Pizarra");
        p.setCantidadColumnas(cantidadColumnas);
        p.setToken(generarTokenUnico());
        p = pizarraRepository.save(p);

        for (int i = 0; i < cantidadColumnas; i++) {
            PizarraColumna col = new PizarraColumna();
            col.setPizarra(p);
            col.setTitulo("");
            col.setOrden(i);
            columnaRepository.save(col);
        }
        return pizarraRepository.findById(p.getId()).orElse(p);
    }

    /**
     * Actualiza nombre, títulos y vueltas de columnas.
     */
    public Pizarra actualizarBasico(Long id, String nombre, List<String> titulos, List<Integer> vueltas, Long profesorId) {
        Pizarra p = pizarraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pizarra no encontrada"));
        if (!p.getProfesor().getId().equals(profesorId)) {
            throw new RuntimeException("No tiene permisos para editar esta pizarra");
        }
        if (nombre != null) p.setNombre(nombre.trim());
        List<PizarraColumna> cols = columnaRepository.findByPizarraIdOrderByOrdenAsc(p.getId());
        if (titulos != null && !titulos.isEmpty()) {
            for (int i = 0; i < Math.min(titulos.size(), cols.size()); i++) {
                String titulo = titulos.get(i);
                cols.get(i).setTitulo(titulo != null ? titulo.trim() : "");
            }
        }
        if (vueltas != null) {
            for (int i = 0; i < Math.min(vueltas.size(), cols.size()); i++) {
                Integer v = vueltas.get(i);
                cols.get(i).setVueltas(v != null && v >= 1 && v <= 9 ? v : null);
            }
        }
        if (cols != null && !cols.isEmpty()) {
            columnaRepository.saveAll(cols);
        }
        return pizarraRepository.save(p);
    }

    /**
     * Agrega un item a una columna.
     */
    public PizarraItem agregarItem(Long columnaId, Long exerciseId, Integer peso, Integer repeticiones, String unidad, Long profesorId) {
        PizarraColumna col = columnaRepository.findById(columnaId)
                .orElseThrow(() -> new RuntimeException("Columna no encontrada"));
        if (!col.getPizarra().getProfesor().getId().equals(profesorId)) {
            throw new RuntimeException("No tiene permisos");
        }
        Exercise ex = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Ejercicio no encontrado"));
        int maxOrden = col.getItems().stream().mapToInt(PizarraItem::getOrden).max().orElse(-1);
        PizarraItem item = new PizarraItem();
        item.setColumna(col);
        item.setExercise(ex);
        item.setPeso(peso);
        item.setRepeticiones(repeticiones);
        item.setUnidad(unidad != null ? unidad : "reps");
        item.setOrden(maxOrden + 1);
        return itemRepository.save(item);
    }

    /**
     * Actualiza peso y repeticiones de un item.
     */
    public void actualizarItem(Long itemId, Integer peso, Integer repeticiones, String unidad, Long profesorId) {
        PizarraItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        if (!item.getColumna().getPizarra().getProfesor().getId().equals(profesorId)) {
            throw new RuntimeException("No tiene permisos");
        }
        if (peso != null) item.setPeso(peso);
        if (repeticiones != null) item.setRepeticiones(repeticiones);
        if (unidad != null) item.setUnidad(unidad);
        itemRepository.save(item);
    }

    /**
     * Elimina un item.
     */
    public void eliminarItem(Long itemId, Long profesorId) {
        PizarraItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        if (!item.getColumna().getPizarra().getProfesor().getId().equals(profesorId)) {
            throw new RuntimeException("No tiene permisos");
        }
        itemRepository.delete(item);
    }

    /**
     * Reordena items dentro de una columna.
     */
    public void reordenarItems(Long columnaId, List<Long> itemIdsEnOrden, Long profesorId) {
        PizarraColumna col = columnaRepository.findById(columnaId)
                .orElseThrow(() -> new RuntimeException("Columna no encontrada"));
        if (!col.getPizarra().getProfesor().getId().equals(profesorId)) {
            throw new RuntimeException("No tiene permisos");
        }
        for (int i = 0; i < itemIdsEnOrden.size(); i++) {
            final int orden = i;
            Long itemId = itemIdsEnOrden.get(i);
            itemRepository.findById(itemId).ifPresent(item -> {
                if (item.getColumna().getId().equals(columnaId)) {
                    item.setOrden(orden);
                    itemRepository.save(item);
                }
            });
        }
    }

    /**
     * Mueve un item de una columna a otra.
     */
    public void moverItem(Long itemId, Long columnaDestinoId, int ordenDestino, Long profesorId) {
        PizarraItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));
        PizarraColumna colDest = columnaRepository.findById(columnaDestinoId)
                .orElseThrow(() -> new RuntimeException("Columna destino no encontrada"));
        if (!item.getColumna().getPizarra().getProfesor().getId().equals(profesorId) ||
            !colDest.getPizarra().getId().equals(item.getColumna().getPizarra().getId())) {
            throw new RuntimeException("No tiene permisos o columnas no pertenecen a la misma pizarra");
        }
        item.setColumna(colDest);
        item.setOrden(ordenDestino);
        itemRepository.save(item);
    }

    /**
     * Construye el DTO para la API de sala (vista TV).
     * Primero intenta por SalaTransmision (token global del profesor); si no, por token de Pizarra.
     */
    public PizarraEstadoDTO construirEstadoParaSala(String token) {
        Long pizarraId = salaTransmisionRepository.findByToken(token)
                .map(SalaTransmision::getPizarraId)
                .orElse(null);
        if (pizarraId == null) {
            Pizarra p = pizarraRepository.findByToken(token)
                    .orElseThrow(() -> new RuntimeException("Pizarra no encontrada"));
            return construirEstadoDesdePizarra(p);
        }
        Pizarra p = pizarraRepository.findByIdWithColumnas(pizarraId)
                .orElseThrow(() -> new RuntimeException("Pizarra no encontrada"));
        return construirEstadoDesdePizarra(p);
    }

    private PizarraEstadoDTO construirEstadoDesdePizarra(Pizarra p) {
        PizarraEstadoDTO dto = new PizarraEstadoDTO();
        dto.setNombre(p.getNombre());
        dto.setCantidadColumnas(p.getCantidadColumnas());
        List<PizarraColumna> columnas = columnaRepository.findByPizarraIdOrderByOrdenAsc(p.getId());
        for (PizarraColumna col : columnas) {
            PizarraEstadoDTO.ColumnaDTO colDto = new PizarraEstadoDTO.ColumnaDTO();
            colDto.setId(col.getId());
            colDto.setTitulo(col.getTitulo());
            colDto.setVueltas(col.getVueltas());
            colDto.setOrden(col.getOrden());
            List<PizarraItem> items = itemRepository.findByColumnaIdOrderByOrdenAsc(col.getId());
            for (PizarraItem it : items) {
                PizarraEstadoDTO.ItemDTO itemDto = new PizarraEstadoDTO.ItemDTO();
                itemDto.setId(it.getId());
                itemDto.setExerciseId(it.getExercise().getId());
                itemDto.setEjercicioNombre(it.getExercise().getName());
                itemDto.setImagenUrl(it.getExercise().getImagen() != null ? it.getExercise().getImagen().getUrl() : "/img/not_imagen.png");
                String grupos = it.getExercise().getGrupos() != null
                        ? it.getExercise().getGrupos().stream().map(GrupoMuscular::getNombre).collect(Collectors.joining(", "))
                        : "";
                itemDto.setGrupoMuscular(grupos);
                itemDto.setPeso(it.getPeso());
                itemDto.setRepeticiones(it.getRepeticiones());
                itemDto.setUnidad(it.getUnidad());
                colDto.getItems().add(itemDto);
            }
            dto.getColumnas().add(colDto);
        }
        return dto;
    }

    /** Genera un token legible para la sala: "tv" + 6 dígitos (ej: tv45677). */
    private String generarTokenUnico() {
        String token;
        do {
            int num = TOKEN_RANDOM.nextInt(1_000_000); // 0 a 999999
            token = "tv" + String.format("%06d", num);
        } while (pizarraRepository.existsByToken(token));
        return token;
    }

    /**
     * Agrega una columna a la pizarra (máximo 6).
     */
    public void agregarColumna(Long pizarraId, Long profesorId) {
        Pizarra p = pizarraRepository.findById(pizarraId)
                .orElseThrow(() -> new RuntimeException("Pizarra no encontrada"));
        if (!p.getProfesor().getId().equals(profesorId)) {
            throw new RuntimeException("No tiene permisos");
        }
        List<PizarraColumna> columnas = columnaRepository.findByPizarraIdOrderByOrdenAsc(p.getId());
        if (columnas.size() >= 6) {
            throw new IllegalArgumentException("Máximo 6 columnas");
        }
        PizarraColumna nueva = new PizarraColumna();
        nueva.setPizarra(p);
        nueva.setTitulo("");
        nueva.setOrden(columnas.size());
        columnaRepository.save(nueva);
        p.setCantidadColumnas(columnas.size() + 1);
        pizarraRepository.save(p);
    }

    /**
     * Quita una columna (y sus items). Mínimo 1 columna. Reordena el resto.
     */
    public void quitarColumna(Long columnaId, Long profesorId) {
        PizarraColumna col = columnaRepository.findById(columnaId)
                .orElseThrow(() -> new RuntimeException("Columna no encontrada"));
        Pizarra p = col.getPizarra();
        if (!p.getProfesor().getId().equals(profesorId)) {
            throw new RuntimeException("No tiene permisos");
        }
        List<PizarraColumna> columnas = columnaRepository.findByPizarraIdOrderByOrdenAsc(p.getId());
        if (columnas.size() <= 1) {
            throw new IllegalArgumentException("Debe haber al menos 1 columna");
        }
        columnaRepository.delete(col);
        List<PizarraColumna> restantes = columnaRepository.findByPizarraIdOrderByOrdenAsc(p.getId());
        for (int i = 0; i < restantes.size(); i++) {
            restantes.get(i).setOrden(i);
        }
        columnaRepository.saveAll(restantes);
        p.setCantidadColumnas(restantes.size());
        pizarraRepository.save(p);
    }

    public void eliminar(Long id, Long profesorId) {
        Pizarra p = pizarraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pizarra no encontrada"));
        if (!p.getProfesor().getId().equals(profesorId)) {
            throw new RuntimeException("No tiene permisos");
        }
        pizarraTrabajoRepository.findByProfesorId(profesorId).ifPresent(pt -> {
            if (pt.getPizarraId().equals(id)) {
                pizarraTrabajoRepository.delete(pt);
                pizarraTrabajoRepository.flush();
            }
        });
        pizarraRepository.delete(p);
    }

    /**
     * Establece o quita el PIN de 4 dígitos para ver la sala en TV.
     * @param pin 4 dígitos, o null/vacío para quitar el código.
     */
    public void setPinSala(Long pizarraId, Long profesorId, String pin) {
        Pizarra p = pizarraRepository.findById(pizarraId)
                .orElseThrow(() -> new RuntimeException("Pizarra no encontrada"));
        if (!p.getProfesor().getId().equals(profesorId)) {
            throw new RuntimeException("No tiene permisos");
        }
        if (pin == null || pin.trim().isEmpty()) {
            p.setPinSalaHash(null);
        } else {
            String limpio = pin.trim();
            if (limpio.length() != 4 || !limpio.matches("\\d{4}")) {
                throw new IllegalArgumentException("El código debe tener exactamente 4 dígitos");
            }
            p.setPinSalaHash(passwordEncoder.encode(limpio));
        }
        pizarraRepository.save(p);
    }

    /**
     * Verifica si el PIN es correcto para la sala con el token dado (SalaTransmision o Pizarra).
     */
    public boolean verificarPinSala(String token, String pin) {
        if (pin == null || pin.trim().isEmpty()) return false;
        Optional<SalaTransmision> st = salaTransmisionRepository.findByToken(token);
        if (st.isPresent()) {
            return st.get().getPinSalaHash() != null && passwordEncoder.matches(pin.trim(), st.get().getPinSalaHash());
        }
        Pizarra p = pizarraRepository.findByToken(token).orElse(null);
        if (p == null || p.getPinSalaHash() == null) return false;
        return passwordEncoder.matches(pin.trim(), p.getPinSalaHash());
    }

    /** Indica si la sala con este token requiere PIN (SalaTransmision o Pizarra). */
    public boolean requierePinSala(String token) {
        Optional<SalaTransmision> st = salaTransmisionRepository.findByToken(token);
        if (st.isPresent()) {
            return st.get().getPinSalaHash() != null && !st.get().getPinSalaHash().isEmpty();
        }
        return pizarraRepository.findByToken(token)
                .map(p -> p.getPinSalaHash() != null && !p.getPinSalaHash().isEmpty())
                .orElse(false);
    }
}
