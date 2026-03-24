package com.adabyron.domain.espacio;

import java.util.List;
import java.util.Optional;

public interface EspacioRepository {

    Espacio save(Espacio espacio);

    Optional<Espacio> findById(EspacioId id);

    List<Espacio> findAllById(List<String> ids);

    List<Espacio> findByCategoria(Categoria categoria);

    List<Espacio> findByNumOcupantes(int numOcupantes);
}
