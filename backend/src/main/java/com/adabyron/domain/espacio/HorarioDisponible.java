package com.adabyron.domain.espacio;

import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * VALUE OBJECT - Horario disponible para reservas de un espacio.
 *
 * Define el rango horario en el que un espacio puede ser reservado.
 * REQ-C5: Por defecto, los espacios tienen el mismo horario que el edificio Ada Byron.
 * REQ-C6: Los gerentes pueden cambiar el horario de un espacio (dentro del horario del edificio).
 *
 * Invariantes:
 * - horaApertura debe ser anterior a horaCierre
 * - El horario debe estar en formato 24 horas
 */
public record HorarioDisponible(LocalTime horaApertura, LocalTime horaCierre) {

    public HorarioDisponible {
        if (horaApertura == null || horaCierre == null) {
            throw new IllegalArgumentException("Las horas de apertura y cierre no pueden ser nulas");
        }
        if (horaApertura.isAfter(horaCierre) || horaApertura.equals(horaCierre)) {
            throw new IllegalArgumentException(
                "La hora de apertura debe ser anterior a la hora de cierre");
        }
    }

    /**
     * Convierte el horario a LocalDateTime para un día específico.
     * Útil para validar si un IntervaloTemporal está dentro del horario.
     */
    public LocalDateTime aperturaEn(LocalDate fecha) {
        return LocalDateTime.of(fecha, horaApertura);
    }

    public LocalDateTime cierreEn(LocalDate fecha) {
        return LocalDateTime.of(fecha, horaCierre);
    }

    /**
     * Verifica si este horario está completamente contenido en otro horario.
     * Necesario para REQ-C6: validar que el horario del espacio está dentro del horario del edificio.
     */
    public boolean estaContenidoEn(HorarioDisponible otro) {
        return !this.horaApertura.isBefore(otro.horaApertura)
            && !this.horaCierre.isAfter(otro.horaCierre);
    }

    @Override
    public String toString() {
        return horaApertura + " - " + horaCierre;
    }
}
