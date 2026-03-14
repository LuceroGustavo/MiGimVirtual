package com.migimvirtual.repositorios;

import com.migimvirtual.entidades.PlanPublico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanPublicoRepository extends JpaRepository<PlanPublico, Long> {
    List<PlanPublico> findByActivoTrueOrderByOrdenAsc();
    List<PlanPublico> findAllByOrderByOrdenAsc();
}
