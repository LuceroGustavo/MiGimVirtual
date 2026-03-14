package com.mattfuncional.repositorios;

import com.mattfuncional.entidades.MedicionFisica;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
public interface MedicionFisicaRepository extends JpaRepository<MedicionFisica, Long> {
    /** Últimas mediciones del usuario (más reciente primero). */
    List<MedicionFisica> findByUsuario_IdOrderByFechaDesc(Long usuarioId);

    void deleteByUsuario_Id(Long usuarioId);
} 