package com.adabyron.domain.edificio;

import com.adabyron.domain.edificio.exception.PorcentajeOcupacionInvalidoException;

public record PorcentajeOcupacion(double valor) {

    public PorcentajeOcupacion {
        if (Double.isNaN(valor) || valor <= 0.0 || valor > 1.0) {
            throw new PorcentajeOcupacionInvalidoException();
        }
    }

    public static PorcentajeOcupacion of(double valor) {
        return new PorcentajeOcupacion(valor);
    }
}