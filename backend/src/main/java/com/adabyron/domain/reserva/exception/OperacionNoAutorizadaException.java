package com.adabyron.domain.reserva.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación sin la autorización necesaria.
 * Por ejemplo, cuando un usuario no gerente intenta cancelar o revalidar una reserva.
 */
public class OperacionNoAutorizadaException extends RuntimeException {
    public OperacionNoAutorizadaException(String mensaje) {
        super(mensaje);
    }
}
