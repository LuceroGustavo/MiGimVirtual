package com.mattfuncional.servicios;

import com.mattfuncional.entidades.ConfiguracionPaginaPublica;
import com.mattfuncional.repositorios.ConfiguracionPaginaPublicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfiguracionPaginaPublicaService {

    @Autowired
    private ConfiguracionPaginaPublicaRepository configRepository;

    public String getValor(String clave) {
        return configRepository.findByClave(clave)
                .map(ConfiguracionPaginaPublica::getValor)
                .orElse(null);
    }

    public String getWhatsapp() {
        return getValor(ConfiguracionPaginaPublica.CLAVE_WHATSAPP);
    }

    public String getInstagram() {
        return getValor(ConfiguracionPaginaPublica.CLAVE_INSTAGRAM);
    }

    public String getDireccion() {
        return getValor(ConfiguracionPaginaPublica.CLAVE_DIRECCION);
    }

    public String getDiasHorarios() {
        return getValor(ConfiguracionPaginaPublica.CLAVE_DIAS_HORARIOS);
    }

    public String getTelefono() {
        return getValor(ConfiguracionPaginaPublica.CLAVE_TELEFONO);
    }

    public Map<String, String> getAllConfig() {
        Map<String, String> map = new HashMap<>();
        configRepository.findAll().forEach(c -> map.put(c.getClave(), c.getValor()));
        return map;
    }

    @Transactional
    public void actualizar(String clave, String valor) {
        ConfiguracionPaginaPublica entity = configRepository.findByClave(clave)
                .orElseGet(() -> {
                    ConfiguracionPaginaPublica nuevo = new ConfiguracionPaginaPublica();
                    nuevo.setClave(clave);
                    return nuevo;
                });
        entity.setValor(valor != null ? valor : "");
        configRepository.save(entity);
    }

    @Transactional
    public void asegurarConfigInicial() {
        List<String> claves = List.of(
                ConfiguracionPaginaPublica.CLAVE_WHATSAPP,
                ConfiguracionPaginaPublica.CLAVE_INSTAGRAM,
                ConfiguracionPaginaPublica.CLAVE_DIRECCION,
                ConfiguracionPaginaPublica.CLAVE_DIAS_HORARIOS,
                ConfiguracionPaginaPublica.CLAVE_TELEFONO
        );
        for (String clave : claves) {
            if (configRepository.findByClave(clave).isEmpty()) {
                String valorDefault = switch (clave) {
                    case ConfiguracionPaginaPublica.CLAVE_WHATSAPP -> "5491112345678";
                    case ConfiguracionPaginaPublica.CLAVE_INSTAGRAM -> "#";
                    case ConfiguracionPaginaPublica.CLAVE_DIRECCION -> "Aconcagua 17, Ramos Mejía";
                    case ConfiguracionPaginaPublica.CLAVE_DIAS_HORARIOS -> "Lunes a Viernes 7:00–21:00";
                    case ConfiguracionPaginaPublica.CLAVE_TELEFONO -> "";
                    default -> "";
                };
                configRepository.save(new ConfiguracionPaginaPublica(clave, valorDefault));
            }
        }
    }
}
