package com.mattfuncional.servicios;

import com.mattfuncional.repositorios.ProfesorRepository;
import com.mattfuncional.repositorios.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Detecta correos duplicados en usuario y profesor para avisar en logs y en la UI.
 * Ayuda a identificar datos que pueden causar "Query did not return a unique result".
 */
@Service
public class DuplicadosCheckService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    /**
     * Devuelve mensajes descriptivos de correos duplicados (usuario y profesor).
     * Lista vacía si no hay duplicados.
     */
    public List<String> getMensajesCorreosDuplicados() {
        List<String> mensajes = new ArrayList<>();
        try {
            List<Object[]> usuarios = usuarioRepository.findCorreosDuplicados();
            if (usuarios != null) {
                for (Object[] row : usuarios) {
                    String correo = row[0] != null ? row[0].toString() : "?";
                    Object cnt = row.length > 1 ? row[1] : "?";
                    mensajes.add("Usuario: " + correo + " (" + cnt + " registros)");
                }
            }
            List<Object[]> profesores = profesorRepository.findCorreosDuplicados();
            if (profesores != null) {
                for (Object[] row : profesores) {
                    String correo = row[0] != null ? row[0].toString() : "?";
                    Object cnt = row.length > 1 ? row[1] : "?";
                    mensajes.add("Profesor: " + correo + " (" + cnt + " registros)");
                }
            }
        } catch (Exception e) {
            mensajes.add("No se pudo verificar duplicados: " + e.getMessage());
        }
        return mensajes;
    }

    /** Indica si hay al menos un correo duplicado. */
    public boolean hayCorreosDuplicados() {
        return !getMensajesCorreosDuplicados().isEmpty();
    }
}
