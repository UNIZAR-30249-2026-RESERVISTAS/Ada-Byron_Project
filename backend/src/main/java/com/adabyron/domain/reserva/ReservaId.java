package com.adabyron.domain.reserva;

import java.util.UUID;

/**
 * Value Object para representar el identificador de una reserva
 * Utiliza UUID para garantizar la unicidad y evitar colisiones.
*/
public record ReservaId(UUID valor) {

    public ReservaId {
        if (valor == null) {
            throw new IllegalArgumentException("ReservaId cannot be null");
        }
    }

    public static ReservaId generar() {
        return new ReservaId(UUID.randomUUID());
    }

    public static ReservaId fromString(String s) {
        try {
            return new ReservaId(UUID.fromString(s));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ReservaId inválido: " + s, e);
        }
    }
 
    @Override
    public String toString() { return valor.toString(); }
}
