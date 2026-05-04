package com.adabyron.domain.espacio;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EspacioRepository {

    Espacio save(Espacio espacio);

    Optional<Espacio> findById(EspacioId id);

    List<Espacio> findAllById(List<String> ids);

    List<Espacio> findByCategoria(Categoria categoria);

    List<Espacio> findByNumOcupantes(int numOcupantes);

    List<Espacio> findDisponibles(LocalDateTime inicio, LocalDateTime fin);

    List<Espacio> findDisponiblesByCategoria(LocalDateTime inicio, LocalDateTime fin, String categoria);

    List<String> findIdsByAforo(int ocupantesNecesarios);
}
