package com.mattfuncional.servicios;

import com.mattfuncional.entidades.Consulta;
import com.mattfuncional.repositorios.ConsultaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConsultaService {

    private final ConsultaRepository consultaRepository;

    public ConsultaService(ConsultaRepository consultaRepository) {
        this.consultaRepository = consultaRepository;
    }

    @Transactional
    public Consulta guardar(String nombre, String email, String telefono, String mensaje) {
        Consulta c = new Consulta(
                nombre != null ? nombre.trim() : "",
                email != null && !email.isEmpty() ? email.trim().toLowerCase() : null,
                telefono != null && !telefono.isEmpty() ? telefono.trim() : null,
                mensaje != null ? mensaje.trim() : ""
        );
        return consultaRepository.save(c);
    }

    public java.util.List<Consulta> getUltimasConsultas(int limite) {
        return consultaRepository.findAllByOrderByFechaCreacionDesc().stream()
                .limit(limite)
                .toList();
    }

    public long contar() {
        return consultaRepository.count();
    }

    /** Cuenta solo las consultas no vistas (para el badge del navbar). */
    public long contarNoVistas() {
        return consultaRepository.countByVistoFalse();
    }

    @Transactional
    public void eliminar(Long id) {
        if (id != null && consultaRepository.existsById(id)) {
            consultaRepository.deleteById(id);
        }
    }

    @Transactional
    public void marcarComoVisto(Long id) {
        consultaRepository.findById(id).ifPresent(c -> {
            c.setVisto(true);
            consultaRepository.save(c);
        });
    }
}
