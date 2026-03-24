package com.migimvirtual.config;

import com.migimvirtual.servicios.DuplicadosCheckService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Tras el arranque completo, comprueba correos duplicados en segundo plano para no
 * sumar trabajo síncrono al camino crítico de inicio.
 */
@Component
public class DuplicadosCheckRunner implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(DuplicadosCheckRunner.class);

    private final DuplicadosCheckService duplicadosCheckService;

    public DuplicadosCheckRunner(DuplicadosCheckService duplicadosCheckService) {
        this.duplicadosCheckService = duplicadosCheckService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        CompletableFuture.runAsync(this::ejecutarComprobacion);
    }

    private void ejecutarComprobacion() {
        try {
            List<String> mensajes = duplicadosCheckService.getMensajesCorreosDuplicados();
            if (!mensajes.isEmpty() && !(mensajes.size() == 1 && mensajes.get(0).startsWith("No se pudo verificar"))) {
                log.warn("[MiGymVirtual] Correos duplicados detectados en la base de datos: {}. Revisar en Usuarios del sistema o en la BD.", mensajes);
            }
        } catch (Exception e) {
            log.debug("Comprobación de duplicados omitida o fallida: {}", e.getMessage());
        }
    }
}
