package com.adabyron.infraestructure.persistence.espacio;

import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;

public class EspacioConverters {

    public static Espacio toDomain(EspacioJpaEntity entity){
        return new Espacio(
                new EspacioId(entity.getId()),
                entity.getNumOcupantes(),
                Categoria.desdeNombre(entity.getCategoria()),
                entity.getTamanyo(),
                entity.isReservable()
        );
    }

    public static EspacioJpaEntity toEntity(Espacio domain){
        EspacioJpaEntity entity = new EspacioJpaEntity();
        entity.setId(domain.getId().id());
        entity.setCategoria(domain.getCategoria().getNombre());
        entity.setNumOcupantes(domain.getNumOcupantes());
        entity.setTamanyo(domain.getTamanyo());
        entity.setReservable(domain.isReservable());
        return entity;
    }
}
