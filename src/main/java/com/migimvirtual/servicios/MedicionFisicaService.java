package com.migimvirtual.servicios;

import com.migimvirtual.entidades.MedicionFisica;
import com.migimvirtual.entidades.Usuario;
import com.migimvirtual.repositorios.MedicionFisicaRepository;
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