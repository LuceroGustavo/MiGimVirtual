package com.mattfuncional.dto;

import com.mattfuncional.entidades.Usuario;
import com.mattfuncional.enums.DiaSemana;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarioSemanalDTO {
    private LocalDate fechaInicioSemana;
    private LocalDate fechaFinSemana;
    private Map<DiaSemana, List<SlotHorarioDTO>> slotsPorDia;
    private int capacidadMaxima;
    private List<Usuario> usuariosEnSemana;

    public CalendarioSemanalDTO() {
    }

    public CalendarioSemanalDTO(LocalDate fechaInicioSemana, LocalDate fechaFinSemana) {
        this.fechaInicioSemana = fechaInicioSemana;
        this.fechaFinSemana = fechaFinSemana;
    }

    // Getters y Setters
    public LocalDate getFechaInicioSemana() {
        return fechaInicioSemana;
    }

    public void setFechaInicioSemana(LocalDate fechaInicioSemana) {
        this.fechaInicioSemana = fechaInicioSemana;
    }

    public LocalDate getFechaFinSemana() {
        return fechaFinSemana;
    }

    public void setFechaFinSemana(LocalDate fechaFinSemana) {
        this.fechaFinSemana = fechaFinSemana;
    }

    public Map<DiaSemana, List<SlotHorarioDTO>> getSlotsPorDia() {
        return slotsPorDia;
    }

    public void setSlotsPorDia(Map<DiaSemana, List<SlotHorarioDTO>> slotsPorDia) {
        this.slotsPorDia = slotsPorDia;
    }

    public int getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public void setCapacidadMaxima(int capacidadMaxima) {
        this.capacidadMaxima = capacidadMaxima;
    }

    public List<Usuario> getUsuariosEnSemana() {
        return usuariosEnSemana;
    }

    public void setUsuariosEnSemana(List<Usuario> usuariosEnSemana) {
        this.usuariosEnSemana = usuariosEnSemana;
    }

    // Clase interna para representar cada slot de horario
    public static class SlotHorarioDTO {
        private LocalTime horaInicio;
        private LocalTime horaFin;
        private List<Usuario> usuariosAsignados;
        private int capacidadActual;
        private int capacidadMaxima;
        private boolean disponible;
        /** Presente por usuario (id -> true/false). null = pendiente. Se rellena en el controlador según la fecha del día. */
        private Map<Long, Boolean> presentePorUsuarioId = new HashMap<>();
        /** Marca si el usuario fue agregado por excepción en este slot. */
        private Map<Long, Boolean> excepcionPorUsuarioId = new HashMap<>();

        public SlotHorarioDTO() {
        }

        public SlotHorarioDTO(LocalTime horaInicio, LocalTime horaFin, int capacidadMaxima) {
            this.horaInicio = horaInicio;
            this.horaFin = horaFin;
            this.capacidadMaxima = capacidadMaxima;
            this.usuariosAsignados = new java.util.ArrayList<>();
            this.capacidadActual = 0;
            this.disponible = true;
        }

        public Map<Long, Boolean> getPresentePorUsuarioId() { return presentePorUsuarioId; }
        public void setPresentePorUsuarioId(Map<Long, Boolean> presentePorUsuarioId) {
            this.presentePorUsuarioId = presentePorUsuarioId != null ? presentePorUsuarioId : new HashMap<>();
        }

        public Map<Long, Boolean> getExcepcionPorUsuarioId() { return excepcionPorUsuarioId; }
        public void setExcepcionPorUsuarioId(Map<Long, Boolean> excepcionPorUsuarioId) {
            this.excepcionPorUsuarioId = excepcionPorUsuarioId != null ? excepcionPorUsuarioId : new HashMap<>();
        }

        public LocalTime getHoraInicio() {
            return horaInicio;
        }

        public void setHoraInicio(LocalTime horaInicio) {
            this.horaInicio = horaInicio;
        }

        public LocalTime getHoraFin() {
            return horaFin;
        }

        public void setHoraFin(LocalTime horaFin) {
            this.horaFin = horaFin;
        }

        public List<Usuario> getUsuariosAsignados() {
            return usuariosAsignados;
        }

        public void setUsuariosAsignados(List<Usuario> usuariosAsignados) {
            this.usuariosAsignados = usuariosAsignados;
            this.capacidadActual = usuariosAsignados != null ? usuariosAsignados.size() : 0;
        }

        public int getCapacidadActual() {
            return capacidadActual;
        }

        public void setCapacidadActual(int capacidadActual) {
            this.capacidadActual = capacidadActual;
        }

        public int getCapacidadMaxima() {
            return capacidadMaxima;
        }

        public void setCapacidadMaxima(int capacidadMaxima) {
            this.capacidadMaxima = capacidadMaxima;
        }

        public boolean isDisponible() {
            return disponible && capacidadActual < capacidadMaxima;
        }

        public void setDisponible(boolean disponible) {
            this.disponible = disponible;
        }

        public String getHorarioFormateado() {
            return horaInicio.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")) +
                    " - " + horaFin.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
        }

        public double getPorcentajeOcupacion() {
            if (capacidadMaxima == 0)
                return 0;
            return (double) capacidadActual / capacidadMaxima * 100;
        }

        public String getColorOcupacion() {
            double porcentaje = getPorcentajeOcupacion();
            if (porcentaje >= 90)
                return "danger";
            if (porcentaje >= 70)
                return "warning";
            if (porcentaje >= 50)
                return "info";
            return "success";
        }
    }
}