package com.adabyron.domain.reserva.exception;

import java.util.UUID;

public class ReservaNotFoundException extends RuntimeException {
    public ReservaNotFoundException(UUID id) {
        super("No se encontró una reserva con ID: " + id);
    }
}
