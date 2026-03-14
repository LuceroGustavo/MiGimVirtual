package com.mattfuncional.servicios;

import com.mattfuncional.entidades.PlanPublico;
import com.mattfuncional.repositorios.PlanPublicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanPublicoService {

    @Autowired
    private PlanPublicoRepository planPublicoRepository;

    public List<PlanPublico> getPlanesActivosParaPublica() {
        return planPublicoRepository.findByActivoTrueOrderByOrdenAsc();
    }

    public List<PlanPublico> getAllPlanes() {
        return planPublicoRepository.findAllByOrderByOrdenAsc();
    }

    public PlanPublico getById(Long id) {
        return planPublicoRepository.findById(id).orElse(null);
    }

    @Transactional
    public PlanPublico guardar(PlanPublico plan) {
        return planPublicoRepository.save(plan);
    }

    @Transactional
    public void eliminar(Long id) {
        planPublicoRepository.deleteById(id);
    }

    /** Mueve el plan una posición hacia arriba (menor orden). Retorna true si se movió. */
    @Transactional
    public boolean moverArriba(Long id) {
        List<PlanPublico> planes = planPublicoRepository.findAllByOrderByOrdenAsc();
        int idx = -1;
        for (int i = 0; i < planes.size(); i++) {
            if (planes.get(i).getId().equals(id)) {
                idx = i;
                break;
            }
        }
        if (idx <= 0) return false;
        PlanPublico actual = planes.get(idx);
        PlanPublico anterior = planes.get(idx - 1);
        int ordActual = actual.getOrden();
        actual.setOrden(anterior.getOrden());
        anterior.setOrden(ordActual);
        planPublicoRepository.save(actual);
        planPublicoRepository.save(anterior);
        return true;
    }

    /** Mueve el plan una posición hacia abajo (mayor orden). Retorna true si se movió. */
    @Transactional
    public boolean moverAbajo(Long id) {
        List<PlanPublico> planes = planPublicoRepository.findAllByOrderByOrdenAsc();
        int idx = -1;
        for (int i = 0; i < planes.size(); i++) {
            if (planes.get(i).getId().equals(id)) {
                idx = i;
                break;
            }
        }
        if (idx < 0 || idx >= planes.size() - 1) return false;
        PlanPublico actual = planes.get(idx);
        PlanPublico siguiente = planes.get(idx + 1);
        int ordActual = actual.getOrden();
        actual.setOrden(siguiente.getOrden());
        siguiente.setOrden(ordActual);
        planPublicoRepository.save(actual);
        planPublicoRepository.save(siguiente);
        return true;
    }

    /** Crea los 4 planes iniciales si no existen. */
    @Transactional
    public void asegurarPlanesIniciales() {
        if (planPublicoRepository.count() > 0) {
            return;
        }
        crearPlan("1 vez por semana", "Acceso una vez por semana.", 15000.0, 1, 0);
        crearPlan("2 veces por semana", "Acceso dos veces por semana.", 25000.0, 2, 1);
        crearPlan("3 veces por semana", "Acceso tres veces por semana.", 35000.0, 3, 2);
        crearPlan("Opción libre", "Acceso libre sin restricción de días.", 45000.0, null, 3);
    }

    private void crearPlan(String nombre, String descripcion, Double precio, Integer vecesPorSemana, int orden) {
        PlanPublico p = new PlanPublico();
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setVecesPorSemana(vecesPorSemana);
        p.setOrden(orden);
        p.setActivo(true);
        planPublicoRepository.save(p);
    }
}
