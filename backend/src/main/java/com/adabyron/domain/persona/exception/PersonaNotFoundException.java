package com.adabyron.domain.persona.exception;

import java.util.UUID;

public class PersonaNotFoundException extends RuntimeException {
    public PersonaNotFoundException(UUID id) {
        super("No se encontró una persona con ID: " + id);
    }

    public PersonaNotFoundException(String email) {
        super("No se encontró una persona con email: " + email);
    }
}
