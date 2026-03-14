package com.mattfuncional.dto;

public class EstadisticasRutinaDTO {
    private int totalRutinas;
    private int rutinasActivas;
    private int rutinasCompletadas;

    public EstadisticasRutinaDTO(int totalRutinas, int rutinasActivas, int rutinasCompletadas) {
        this.totalRutinas = totalRutinas;
        this.rutinasActivas = rutinasActivas;
        this.rutinasCompletadas = rutinasCompletadas;
    }

    // Getters y Setters
    public int getTotalRutinas() {
        return totalRutinas;
    }

    public void setTotalRutinas(int totalRutinas) {
        this.totalRutinas = totalRutinas;
    }

    public int getRutinasActivas() {
        return rutinasActivas;
    }

    public void setRutinasActivas(int rutinasActivas) {
        this.rutinasActivas = rutinasActivas;
    }

    public int getRutinasCompletadas() {
        return rutinasCompletadas;
    }

    public void setRutinasCompletadas(int rutinasCompletadas) {
        this.rutinasCompletadas = rutinasCompletadas;
    }
}