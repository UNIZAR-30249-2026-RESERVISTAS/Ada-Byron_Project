package com.adabyron.domain.persona;

import java.util.UUID;

public record PersonaId(UUID valor) {

    public PersonaId {
        if (valor == null) {
            throw new IllegalArgumentException("El ID de persona no puede ser nulo");
        }
    } 

    /**
     * Método encargado de generar un nuevo ID único para una persona. 
     * Utiliza UUID.randomUUID() para garantizar la unicidad.
     */
    public static PersonaId generarNuevoId() {
        return new PersonaId(UUID.randomUUID());
    }

    /**
     * Método encargado de crear un PersonaId a partir de un string, para facilitar su uso en la UI y en los logs.
     */
    public static PersonaId fromString(String idStr) {
        try {
            return new PersonaId(UUID.fromString(idStr));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("ID de persona inválido: " + idStr, e);
        }
    }
}
