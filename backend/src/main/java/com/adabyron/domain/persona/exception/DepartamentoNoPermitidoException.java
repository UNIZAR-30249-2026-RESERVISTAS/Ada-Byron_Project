package com.adabyron.domain.persona.exception;

import com.adabyron.domain.persona.Rol;

public class DepartamentoNoPermitidoException extends RuntimeException {
    public DepartamentoNoPermitidoException(Rol rol) {
        super("El rol '" + rol.nombreUI() + "' no puede estar adscrito a ningún departamento (REQ-B5)");
    }
}