package com.migimvirtual.repositorios;

import com.migimvirtual.entidades.RegistroProgreso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistroProgresoRepository extends JpaRepository<RegistroProgreso, Long> {

    List<RegistroProgreso> findByUsuario_IdOrderByFechaDescIdDesc(Long usuarioId);

    void deleteByUsuario_Id(Long usuarioId);
}
