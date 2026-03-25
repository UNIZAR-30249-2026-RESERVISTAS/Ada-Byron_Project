package com.adabyron.domain.espacio.exception;

/**
 * Excepción lanzada cuando se intenta configurar un horario inválido para un espacio.
 * Por ejemplo, cuando el horario del espacio no está contenido en el horario del edificio.
 */
public class HorarioInvalidoException extends RuntimeException {
    public HorarioInvalidoException(String mensaje) {
        super(mensaje);
    }
}
