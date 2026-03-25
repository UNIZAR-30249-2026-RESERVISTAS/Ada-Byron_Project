package com.adabyron.domain.edificio;

import com.adabyron.domain.espacio.HorarioDisponible;
import java.time.LocalTime;

/**
 * Horario del edificio Ada Byron.
 *
 * REQ-C5: Define el horario por defecto para todos los espacios del edificio.
 * Los espacios heredan este horario a menos que tengan un horario específico configurado.
 *
 * Horario del edificio Ada Byron: 8:00 - 21:00
 */
public class HorarioEdificio {

    /**
     * Horario por defecto del edificio Ada Byron.
     * Este es el horario que se usa cuando un espacio no tiene horario específico (REQ-C5).
     */
    public static final HorarioDisponible HORARIO_ADA_BYRON =
        new HorarioDisponible(LocalTime.of(8, 0), LocalTime.of(21, 0));

    /**
     * Obtiene el horario del edificio Ada Byron.
     * Los espacios sin horario específico usan este horario por defecto.
     */
    public static HorarioDisponible obtenerHorarioEdificio() {
        return HORARIO_ADA_BYRON;
    }
}
