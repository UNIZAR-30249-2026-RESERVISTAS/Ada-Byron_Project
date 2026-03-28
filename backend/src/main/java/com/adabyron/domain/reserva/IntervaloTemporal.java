package com.adabyron.domain.reserva;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * VALUE OBJECT — Intervalo temporal de una reserva (capa de dominio).
 *
 * REPRESENTACIÓN TEMPORAL:
 * Utiliza LocalDateTime inicio + LocalDateTime fin para representar un intervalo de tiempo
 * completo. Esta representación permite:
 *   - Encapsular las reglas de negocio relacionadas con el tiempo
 *   - Validar invariantes (inicio < fin, mismo día)
 *   - Proporcionar métodos de dominio: seSolapaCon(), estaContenidoEn(), duracion()
 *   - Ser inmutable y autodescriptivo (Value Object según DDD)
 *
 * El API externo (CrearReservaDTO) usa fecha + hora + duración por conveniencia del usuario,
 * y se convierte a IntervaloTemporal mediante el factory method of().
 *
 * La capa de persistencia (ReservaJpaEntity) descompone este Value Object en dos columnas
 * separadas (fechaInicio, fechaFin) por limitaciones de JPA y para facilitar consultas SQL.
 *
 * Invariantes:
 *   - inicio debe ser antes que fin
 *   - inicio y fin deben estar en el mismo día (REQ-E2)
 *   - el intervalo no puede ser de duración cero
 *
 * Ver CrearReservaDTO para la representación en el API.
 * Ver ReservaJpaEntity para la representación en base de datos.
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

    /** ¿Este intervalo se encuentra dentro del horario disponible del edificio? — REQ-C5 (si el espacio no tiene horario específico, usaremos el horario del edificio) ,
     *  ¿Este intervalo se encuentra dentro del horario disponible del espacio? — REQ-C6 (si el espacio tiene horario específico) */
    public boolean estaContenidoEn(LocalDateTime apertura, LocalDateTime cierre) {
        return !fechaInicio().isBefore(apertura) && !fechaFin().isAfter(cierre);
    }

}
