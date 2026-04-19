package com.adabyron.domain.persona.exception;

import com.adabyron.domain.persona.Rol;

public class RolInmutable extends RuntimeException {
    public RolInmutable(Rol rol) {
        super("El rol '" + rol.nombreUI() + "' no puede cambiar (Opcional 7)");
    }
}
