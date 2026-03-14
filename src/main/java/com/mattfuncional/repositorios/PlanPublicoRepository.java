package com.mattfuncional.repositorios;

import com.mattfuncional.entidades.PlanPublico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanPublicoRepository extends JpaRepository<PlanPublico, Long> {
    List<PlanPublico> findByActivoTrueOrderByOrdenAsc();
    List<PlanPublico> findAllByOrderByOrdenAsc();
}
