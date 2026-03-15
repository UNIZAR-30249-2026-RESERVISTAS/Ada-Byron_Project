package com.adabyron.application.persona;

import com.adabyron.domain.persona.Departamento;

public record DepartamentoDTO(
    int id,
    String nombre,
    String codigoSIGEUZ
) {
    public static DepartamentoDTO fromEntity(Departamento departamento) {
        return new DepartamentoDTO(
            departamento.getId().valor(),
            departamento.getNombre(),
            departamento.getCodigoSIGEUZ()
        );
    }
}
