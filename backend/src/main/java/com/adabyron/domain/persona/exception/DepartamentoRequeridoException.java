package com.adabyron.domain.persona.exception;

import com.adabyron.domain.persona.Rol;

public class DepartamentoRequeridoException extends RuntimeException {
    public DepartamentoRequeridoException(Rol rol) {
        super("El rol '" + rol.nombreUI() + "' requiere adscripción a un departamento (REQ-B3)");
    }
}