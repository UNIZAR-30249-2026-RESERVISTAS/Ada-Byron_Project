package com.adabyron.application.espacio;

import com.adabyron.domain.espacio.Espacio;


public record EspacioDTO(
        String id,
        String categoria,
        int ocupantes,
        double area,
        boolean reservable
) {
    public static EspacioDTO fromEntity(Espacio espacio) {
        return new EspacioDTO(
                espacio.getId().id(),
                espacio.getCategoria().getNombre(),
                espacio.getNumOcupantes(),
                espacio.getTamanyo(),
                espacio.isReservable()
        );
    }
}