package com.adabyron.application.reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.adabyron.domain.reserva.TipoUsoReserva;

/**
 * DTO para la creación de una reserva — capa de aplicación/API.
 *
 * REPRESENTACIÓN TEMPORAL:
 * Utiliza fecha + horaInicio + duracionMinutos por conveniencia para el usuario del API.
 * Es más intuitivo para el cliente decir "el 25 de marzo, a las 10:00, durante 90 minutos"
 * que tener que calcular la hora de fin manualmente.
 *
 * Este DTO se convierte a IntervaloTemporal (Value Object del dominio) mediante
 * IntervaloTemporal.of(fecha, horaInicio, duracionMinutos) en la capa de servicio.
 *
 * Ver IntervaloTemporal para la representación en el dominio.
 * Ver ReservaJpaEntity para la representación en base de datos.
 */
public record CrearReservaDTO(
    List<String> espacioIds,          // REQ-E1 — uno o varios
    UUID reservadaPorId,              // PersonaId del solicitante
    TipoUsoReserva tipoUso,           // REQ-E1 — docencia, investigación, gestión, otros
    int numeroAsistentes,             //
    LocalDate fecha,                  // Día de la reserva
    LocalTime horaInicio,             // Hora de inicio de la reserva
    int duracionMinutos,              // Duración en minutos (se calcula fechaFin = horaInicio + duracion)
    String detallesAdicionales        // REQ-E3 — campo libre, puede ser null
) {
}