package com.adabyron.domain.edificio;

import java.time.LocalTime;

import com.adabyron.domain.espacio.HorarioDisponible;
/**
 * Clase que representa el edificio Ada Byron.
 * Actualmente, el edificio solo tiene un horario por defecto (REQ-C5).
 */
public class Edificio {

     /**
     * Horario por defecto del edificio Ada Byron (REQ-C5).
     * Horario: 8:00 - 21:00
     */
    private static final HorarioDisponible HORARIO_POR_DEFECTO =
        new HorarioDisponible(LocalTime.of(8, 0), LocalTime.of(21, 0));

    /**
     * Obtiene el horario por defecto del edificio.
     * Los espacios sin horario específico usan este horario.
     */
    public static HorarioDisponible getHorarioPorDefecto() {
        return HORARIO_POR_DEFECTO;
    }
}
