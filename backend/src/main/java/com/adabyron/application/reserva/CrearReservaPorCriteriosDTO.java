package com.adabyron.application.reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.adabyron.domain.reserva.TipoUsoReserva;

public record CrearReservaPorCriteriosDTO(
    UUID reservadaPorId,
    Integer numeroAsistentes,
    Integer numEspacios,
    Integer capacidadTotal,
    LocalDate fecha,
    LocalTime horaInicio,
    Integer duracionMinutos,
    TipoUsoReserva tipoUso,
    String detallesAdicionales
) {}
