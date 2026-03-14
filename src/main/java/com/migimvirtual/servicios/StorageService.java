package com.migimvirtual.servicios;

import com.migimvirtual.entidades.Imagen;
import com.migimvirtual.repositorios.ImagenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @deprecated Este servicio está deprecado. Usar ImagenServicio directamente.
 * Se mantiene por compatibilidad pero redirige a ImagenServicio.
 */
@Service
@Deprecated
public class StorageService {

    @Autowired
    private ImagenRepository imagenRepository;
    
    @Autowired
    private ImagenServicio imagenServicio;

    public Imagen store(MultipartFile file) throws IOException {
        // Redirigir a ImagenServicio que guarda en filesystem
        return imagenServicio.guardar(file);
    }

    public Imagen findById(String id) {
        return imagenRepository.findById(id).orElse(null);
    }
}
