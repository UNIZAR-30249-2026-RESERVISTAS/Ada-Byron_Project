package com.adabyron.application.espacio;

import com.adabyron.domain.espacio.HorarioDisponible;

import java.time.LocalTime;

/**
 * DTO para representar el horario de un espacio.
 * Se usa tanto para respuestas como para solicitudes de cambio de horario.
 */
public record HorarioDTO(
    LocalTime horaApertura,
    LocalTime horaCierre,
    boolean esHorarioEdificio
) {
    /**
     * Crea un HorarioDTO desde un HorarioDisponible de dominio.
     */
    public static HorarioDTO fromDomain(HorarioDisponible horario, boolean esHorarioEdificio) {
        return new HorarioDTO(
            horario.horaApertura(),
            horario.horaCierre(),
            esHorarioEdificio
        );
    }

    /**
     * Convierte a un HorarioDisponible de dominio.
     */
    public HorarioDisponible toDomain() {
        return new HorarioDisponible(horaApertura, horaCierre);
    }
}
