package com.adabyron.domain.espacio.exception;

public class EspacioNotFoundException extends RuntimeException {
    public EspacioNotFoundException(String id) {
        super("No se encontró un espacio con ID: " + id);
    }
}
