package com.adabyron.domain.persona;

/**
 * Objeto Valor - Referencia a un Departamento.
 *
 * Un Departamento es una Entidad fuera del agregado Persona.
 * Persona no contiene el objeto Departamento, sino solo una referencia a su id.
 * Esto es una regla de DDD, los agregados se referencia por su ID.
 */
public record DepartamentoId(int valor) {
    public static final DepartamentoId INFORMATICA_INGENIERIA_SISTEMAS = new DepartamentoId(1);
    public static final DepartamentoId INGENIERIA_ELECTRONICA_COMUNICACIONES = new DepartamentoId(2);

    public DepartamentoId {
        if (valor != 1 && valor != 2) {
            throw new IllegalArgumentException("DepartamentoId inválido:" + valor + ". Solo existen los departamentos 1 y 2 REQ-B3");
        }
    }

    @Override
    public String toString() {
        return toString().valueOf(valor); // Devolvemos el valor como un string para facilitar su uso en la UI y en los logs.
    }
}
