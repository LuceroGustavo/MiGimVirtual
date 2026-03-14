package com.migimvirtual.repositorios;

import com.migimvirtual.entidades.Consulta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {
    List<Consulta> findAllByOrderByFechaCreacionDesc();
    long countByVistoFalse();
}
