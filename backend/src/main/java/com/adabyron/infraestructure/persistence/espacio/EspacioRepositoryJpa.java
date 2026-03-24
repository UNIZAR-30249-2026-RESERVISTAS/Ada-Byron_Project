package com.adabyron.infraestructure.persistence.espacio;

import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.espacio.EspacioRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EspacioRepositoryJpa implements EspacioRepository {
    private final SpringDataEspacioRepository jpa;

    public EspacioRepositoryJpa(SpringDataEspacioRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Espacio save(Espacio espacio) {
        EspacioJpaEntity entity = EspacioConverters.toEntity(espacio);
        jpa.save(entity);
        return espacio;
    }

    @Override
    public Optional<Espacio> findById(EspacioId id) {
        return jpa.findById(id.id()).map(EspacioConverters::toDomain);
    }

    @Override 
    public List<Espacio> findAllById(List<String> ids) {
        return jpa.findAllById(ids).stream()
                  .map(EspacioConverters::toDomain)
                  .toList();
    }

    @Override
    public List<Espacio> findByCategoria(Categoria categoria){
        return jpa.findByCategoria(categoria.getNombre()).stream()
                .map(EspacioConverters::toDomain)
                .toList();
    }

    @Override
    public List<Espacio> findByNumOcupantes(int numOcupantes){
        return jpa.findByNumOcupantes(numOcupantes).stream()
                .map(EspacioConverters::toDomain)
                .toList();
    }



}
