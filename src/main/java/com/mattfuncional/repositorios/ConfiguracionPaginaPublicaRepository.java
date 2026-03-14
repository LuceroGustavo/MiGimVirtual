package com.mattfuncional.repositorios;

import com.mattfuncional.entidades.ConfiguracionPaginaPublica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionPaginaPublicaRepository extends JpaRepository<ConfiguracionPaginaPublica, Long> {
    Optional<ConfiguracionPaginaPublica> findByClave(String clave);
}
