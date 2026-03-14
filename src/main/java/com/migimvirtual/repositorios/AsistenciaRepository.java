package com.migimvirtual.repositorios;

import com.migimvirtual.entidades.Asistencia;
import com.migimvirtual.entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {
    List<Asistencia> findByUsuarioOrderByFechaDesc(Usuario usuario);
    List<Asistencia> findByUsuario_IdOrderByFechaDesc(Long usuarioId);
    List<Asistencia> findByUsuarioAndFecha(Usuario usuario, LocalDate fecha);
    List<Asistencia> findByFechaBetween(LocalDate inicio, LocalDate fin);

    /** Asistencias donde este usuario figura como "registrado por" (para poder soltar la FK al borrar el usuario). */
    List<Asistencia> findByRegistradoPor_Id(Long registradoPorId);

    /** Elimina todas las asistencias del alumno (para poder borrar el usuario sin violar FK). */
    void deleteByUsuario_Id(Long usuarioId);

    /** Carga asistencias con usuario inicializado (evita LazyInitialization al construir el mapa). */
    @Query("SELECT a FROM Asistencia a LEFT JOIN FETCH a.usuario WHERE a.fecha BETWEEN :inicio AND :fin")
    List<Asistencia> findByFechaBetweenWithUsuario(@Param("inicio") LocalDate inicio, @Param("fin") LocalDate fin);

    /** Cuenta asistencias con fecha anterior a la indicada (para depuración). */
    long countByFechaBefore(LocalDate fecha);

    /** Elimina asistencias con fecha anterior a la indicada. Devuelve cantidad eliminada. */
    @Modifying
    @Query("DELETE FROM Asistencia a WHERE a.fecha < :fecha")
    int deleteByFechaBefore(@Param("fecha") LocalDate fecha);
} 