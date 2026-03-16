package com.adabyron.domain.espacio;

import jakarta.validation.constraints.NotNull;

public record CategoriaId(int valor) {

    public static final CategoriaId AULA = new CategoriaId(1);
    public static final CategoriaId SEMINARIO = new CategoriaId(2);
    public static final CategoriaId LABORATORIO = new CategoriaId(3);
    public static final CategoriaId DESPACHO = new CategoriaId(4);
    public static final CategoriaId SALA_COMUN = new CategoriaId(5);

    public CategoriaId{
        if (valor < 1 || valor > 5) {
            throw new IllegalArgumentException("CategoriaId inválido:" + valor + ". Solo existen los departamentos 1, 2, 3, 4 y 5 REQ-C4");
        }
    }
}
