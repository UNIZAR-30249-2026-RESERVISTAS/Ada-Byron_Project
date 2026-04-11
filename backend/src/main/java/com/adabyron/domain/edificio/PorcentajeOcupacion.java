package com.adabyron.domain.edificio;

public record PorcentajeOcupacion(double valor) {

    public PorcentajeOcupacion {
        if (Double.isNaN(valor) || valor <= 0.0 || valor > 1.0) {
            throw new IllegalArgumentException("El porcentaje de ocupacion debe estar en (0.0, 1.0]");
        }
    }

    public static PorcentajeOcupacion of(double valor) {
        return new PorcentajeOcupacion(valor);
    }
}