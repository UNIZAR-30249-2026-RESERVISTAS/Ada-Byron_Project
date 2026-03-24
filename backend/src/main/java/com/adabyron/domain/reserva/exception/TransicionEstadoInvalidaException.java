package com.adabyron.domain.reserva.exception;

import com.adabyron.domain.reserva.EstadoReserva;

public class TransicionEstadoInvalidaException extends RuntimeException {
    public TransicionEstadoInvalidaException(EstadoReserva actual, EstadoReserva destino) {
        super("Transición de estado inválida: " + actual + " → " + destino);
    }
}
