package com.adabyron.infraestructure.persistence.edificio;

import com.adabyron.domain.edificio.EdificioRepository;
import com.adabyron.domain.edificio.PorcentajeOcupacion;
import org.springframework.stereotype.Repository;

@Repository
public class EdificioRepositoryJpa implements EdificioRepository {

    private static final int ID_UNICO = 1;
    private static final double PORCENTAJE_DEFAULT = 1.0;

    private final SpringDataEdificioRepository jpa;

    public EdificioRepositoryJpa(SpringDataEdificioRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public PorcentajeOcupacion obtenerPorcentajeOcupacionMaxima() {
        EdificioJpaEntity entity = cargarOCrear();
        return PorcentajeOcupacion.of(entity.getPorcentajeOcupacionMaxima());
    }

    @Override
    public PorcentajeOcupacion guardarPorcentajeOcupacionMaxima(PorcentajeOcupacion porcentaje) {
        EdificioJpaEntity entity = cargarOCrear();
        entity.setPorcentajeOcupacionMaxima(porcentaje.valor());
        jpa.save(entity);
        return porcentaje;
    }

    private EdificioJpaEntity cargarOCrear() {
        return jpa.findById(ID_UNICO)
                .orElseGet(() -> jpa.save(new EdificioJpaEntity(ID_UNICO, PORCENTAJE_DEFAULT)));
    }
}
