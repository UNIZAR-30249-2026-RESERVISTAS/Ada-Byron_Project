package com.adabyron.domain.edificio;

public interface EdificioRepository {
    PorcentajeOcupacion obtenerPorcentajeOcupacionMaxima();
    PorcentajeOcupacion guardarPorcentajeOcupacionMaxima(PorcentajeOcupacion porcentaje);
}
