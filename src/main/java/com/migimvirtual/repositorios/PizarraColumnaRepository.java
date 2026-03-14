package com.migimvirtual.repositorios;

import com.migimvirtual.entidades.PizarraColumna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PizarraColumnaRepository extends JpaRepository<PizarraColumna, Long> {

    List<PizarraColumna> findByPizarraIdOrderByOrdenAsc(Long pizarraId);
}
