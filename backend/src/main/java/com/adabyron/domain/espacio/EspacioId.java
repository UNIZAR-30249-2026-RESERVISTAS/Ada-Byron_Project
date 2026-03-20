package com.adabyron.domain.espacio;

import java.util.regex.Pattern;

public record EspacioId(String id) {
    private static final Pattern FORMATO_VALIDO = Pattern.compile("^[0-9]{3}$|^S[0-9]{4}$");

    public EspacioId {
        if (id == null || !FORMATO_VALIDO.matcher(id).matches()) {
            throw new IllegalArgumentException(
                    "Formato de EspacioId inválido: " + id +
                            ". Debe ser '0xx' para plantas o 'S1xxx' para sótanos."
            );
        }
    }

    public boolean esSotano() {
        return id.startsWith("S");
    }

    public String obtenerPlanta() {
        if (esSotano()) {
            // Si es S1001, la planta es la que sigue a la S (el '1')
            return "S" + id.substring(1, 2);
        }
        // Si es 001 o 205, la planta es el primer dígito
        return "P" + id.substring(0, 1);
    }
}
