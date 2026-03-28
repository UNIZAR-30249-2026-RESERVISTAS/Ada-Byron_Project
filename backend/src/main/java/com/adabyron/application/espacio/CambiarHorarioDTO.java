package com.adabyron.application.espacio;


import java.time.LocalTime;

/**
 * DTO para solicitar el cambio de horario de un espacio.
 * REQ-C6: Solo los gerentes pueden cambiar el horario de un espacio.
 */
public record CambiarHorarioDTO(
    LocalTime horaApertura,
    LocalTime horaCierre
) {
}
