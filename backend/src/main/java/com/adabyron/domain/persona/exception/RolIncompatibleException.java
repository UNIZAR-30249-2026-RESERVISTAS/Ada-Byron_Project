package com.adabyron.domain.persona.exception;

import com.adabyron.domain.persona.Rol;


public class RolIncompatibleException extends RuntimeException {
    public RolIncompatibleException(Rol r1, Rol r2) {
        super("Los roles '" + r1.nombreUI() + "' y '" + r2.nombreUI() + "' no son compatibles (REQ-B2)");
    }

    // Constructor sobrecargado — para mensajes personalizados (usado en tests)
    public RolIncompatibleException(String mensaje) {
        super(mensaje);
    }
}
