package com.mattfuncional.servicios;

import com.mattfuncional.entidades.SlotConfig;
import com.mattfuncional.enums.DiaSemana;
import com.mattfuncional.repositorios.SlotConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class SlotConfigService {
    @Autowired
    private SlotConfigRepository slotConfigRepository;

    public int getCapacidadMaxima(DiaSemana dia, LocalTime horaInicio, int defaultValue) {
        Optional<SlotConfig> config = slotConfigRepository.findFirstByDiaAndHoraInicio(dia, horaInicio);
        return config.map(SlotConfig::getCapacidadMaxima).orElse(defaultValue);
    }

    /**
     * Actualiza la capacidad máxima para un slot (dia, horaInicio).
     * Si existen duplicados, conserva uno y elimina el resto para evitar "Query did not return a unique result".
     */
    @Transactional
    public void setCapacidadMaxima(DiaSemana dia, LocalTime horaInicio, int capacidadMaxima) {
        List<SlotConfig> existentes = slotConfigRepository.findAllByDiaAndHoraInicio(dia, horaInicio);
        SlotConfig config;
        if (existentes != null && !existentes.isEmpty()) {
            config = existentes.get(0);
            config.setCapacidadMaxima(capacidadMaxima);
            slotConfigRepository.save(config);
            // Eliminar duplicados (todos menos el primero)
            for (int i = 1; i < existentes.size(); i++) {
                slotConfigRepository.delete(existentes.get(i));
            }
        } else {
            config = new SlotConfig();
            config.setDia(dia);
            config.setHoraInicio(horaInicio);
            config.setCapacidadMaxima(capacidadMaxima);
            slotConfigRepository.save(config);
        }
    }
}