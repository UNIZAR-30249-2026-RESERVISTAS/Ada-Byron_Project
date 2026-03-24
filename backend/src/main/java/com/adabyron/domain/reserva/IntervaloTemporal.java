package com.adabyron.domain.reserva;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * VALUE OBJECT — Intervalo temporal de una reserva.
 *
 * Invariantes:
 *   - inicio debe ser antes que fin
 *   - inicio y fin deben estar en el mismo día (REQ-E2)
 *   - el intervalo no puede ser de duración cero
 */
public record IntervaloTemporal(LocalDateTime inicio, LocalDateTime fin) {

    public IntervaloTemporal {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin no pueden ser nulas");
        }
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior a la fecha de fin");
        }
        // REQ-E2: la reserva no puede empezar un día y terminar en otro
        if (inicio.toLocalDate().isAfter(fin.toLocalDate())) {
            throw new IllegalArgumentException("Las fechas deben estar en el mismo día");
        }
    }

    /** Factory conveniente a partir de fecha, hora inicio y duración en minutos */
    public static IntervaloTemporal of(LocalDate fecha, LocalTime horaInicio, int duracionMinutos) {
        LocalDateTime inicio = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin    = inicio.plusMinutes(duracionMinutos);
        return new IntervaloTemporal(inicio, fin);
    }

    /** Comprueba si este intervalo se solapa con otro (total o parcialmente) — REQ-F7 */
    public boolean seSolapaCon(IntervaloTemporal otro) {
        return inicio.isBefore(otro.fin) && fin.isAfter(otro.inicio);
    }

    public Duration duracion() {
        return Duration.between(inicio, fin);
    }

    public LocalDate fecha() {
        return inicio.toLocalDate();
    }

    public LocalDateTime fechaInicio() {
        return inicio;
    }

    public LocalDateTime fechaFin() {
        return fin;
    }

    /** ¿Este intervalo se encuentra dentro del horario disponible del espacio? — REQ-C5, C6 */
    public boolean estaContenidoEn(LocalDateTime apertura, LocalDateTime cierre) {
        return !fechaInicio().isBefore(apertura) && !fechaFin().isAfter(cierre);
    }

}
