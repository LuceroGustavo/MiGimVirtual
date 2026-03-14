package com.mattfuncional.config;

import com.mattfuncional.servicios.DuplicadosCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Al arrancar la aplicación, escribe en log si hay correos duplicados en usuario/profesor.
 */
@Component
@Order(100)
public class DuplicadosCheckRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DuplicadosCheckRunner.class);

    private final DuplicadosCheckService duplicadosCheckService;

    public DuplicadosCheckRunner(DuplicadosCheckService duplicadosCheckService) {
        this.duplicadosCheckService = duplicadosCheckService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<String> mensajes = duplicadosCheckService.getMensajesCorreosDuplicados();
        if (!mensajes.isEmpty() && !(mensajes.size() == 1 && mensajes.get(0).startsWith("No se pudo verificar"))) {
            log.warn("[Mattfuncional] Correos duplicados detectados en la base de datos: {}. Revisar en Usuarios del sistema o en la BD.", mensajes);
        }
    }
}
