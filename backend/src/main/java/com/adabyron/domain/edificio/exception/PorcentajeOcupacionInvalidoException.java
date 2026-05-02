package com.adabyron.domain.edificio.exception;

public class PorcentajeOcupacionInvalidoException extends RuntimeException {
    public PorcentajeOcupacionInvalidoException() {
        super("El porcentaje de ocupacion debe estar en (0.0, 1.0]");
    }
}
