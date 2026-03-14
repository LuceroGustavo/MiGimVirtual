package com.mattfuncional.servicios;

import com.mattfuncional.entidades.MedicionFisica;
import com.mattfuncional.entidades.Usuario;
import com.mattfuncional.repositorios.MedicionFisicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicionFisicaService {
    @Autowired
    private MedicionFisicaRepository medicionFisicaRepository;

    public MedicionFisica guardarMedicion(MedicionFisica medicion) {
        return medicionFisicaRepository.save(medicion);
    }

    public List<MedicionFisica> obtenerMedicionesPorUsuario(Long usuarioId) {
        return medicionFisicaRepository.findByUsuario_IdOrderByFechaDesc(usuarioId);
    }
} 