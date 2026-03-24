package com.migimvirtual.repositorios;

import com.migimvirtual.entidades.ConfiguracionPaginaPublica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ConfiguracionPaginaPublicaRepository extends JpaRepository<ConfiguracionPaginaPublica, Long> {
    Optional<ConfiguracionPaginaPublica> findByClave(String clave);

    long countByClaveIn(Collection<String> claves);
}
