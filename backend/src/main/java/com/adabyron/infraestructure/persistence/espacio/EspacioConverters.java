package com.adabyron.infraestructure.persistence.espacio;

import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.espacio.HorarioDisponible;

public class EspacioConverters {

    /**
     * Convierte una entidad JPA a objeto de dominio.
     * Si las horas de apertura/cierre son null, el horarioEspecifico será null
     * y el espacio usará el horario del edificio por defecto (REQ-C5).
     */
    public static Espacio toDomain(EspacioJpaEntity entity) {
        // Reconstruimos HorarioDisponible solo si ambas horas están presentes
        HorarioDisponible horarioEspecifico = null;
        if (entity.getHoraApertura() != null && entity.getHoraCierre() != null) {
            horarioEspecifico = new HorarioDisponible(
                entity.getHoraApertura(),
                entity.getHoraCierre()
            );
        }

        return new Espacio(
            new EspacioId(entity.getId()),
            entity.getNumOcupantes(),
            Categoria.desdeNombre(entity.getCategoria()),
            entity.getTamanyo(),
            entity.isReservable(),
            horarioEspecifico
        );
    }

    /**
     * Convierte un objeto de dominio a entidad JPA.
     * Si el espacio no tiene horario específico (usa el del edificio),
     * las columnas hora_apertura y hora_cierre serán null.
     */
    public static EspacioJpaEntity toEntity(Espacio domain) {
        EspacioJpaEntity entity = new EspacioJpaEntity();
        entity.setId(domain.getId().id());
        entity.setCategoria(domain.getCategoria().getNombre());
        entity.setNumOcupantes(domain.getNumOcupantes());
        entity.setTamanyo(domain.getTamanyo());
        entity.setReservable(domain.isReservable());

        // Mapear horario específico si existe
        if (domain.tieneHorarioEspecifico()) {
            HorarioDisponible horario = domain.getHorarioDisponible();
            entity.setHoraApertura(horario.horaApertura());
            entity.setHoraCierre(horario.horaCierre());
        } else {
            entity.setHoraApertura(null);
            entity.setHoraCierre(null);
        }

        return entity;
    }
}
