package com.mattfuncional.entidades;

import com.mattfuncional.enums.DiaSemana;
import jakarta.persistence.Embeddable;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Embeddable
public class DiaHorarioAsistencia {
    private DiaSemana dia;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;

    public DiaHorarioAsistencia() {
    }

    public DiaHorarioAsistencia(DiaSemana dia, LocalTime horaEntrada, LocalTime horaSalida) {
        this.dia = dia;
        this.horaEntrada = horaEntrada;
        this.horaSalida = horaSalida;
    }

    public DiaSemana getDia() {
        return dia;
    }

    public void setDia(DiaSemana dia) {
        this.dia = dia;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    // Métodos útiles para el calendario
    public String getHorarioFormateado() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return horaEntrada.format(formatter) + " - " + horaSalida.format(formatter);
    }

    public boolean seSolapaCon(DiaHorarioAsistencia otro) {
        if (!this.dia.equals(otro.dia)) {
            return false;
        }
        return !(this.horaSalida.isBefore(otro.horaEntrada) || otro.horaSalida.isBefore(this.horaEntrada));
    }

    public int getDuracionMinutos() {
        return (int) java.time.Duration.between(horaEntrada, horaSalida).toMinutes();
    }

    @Override
    public String toString() {
        return dia + ": " + getHorarioFormateado();
    }
}