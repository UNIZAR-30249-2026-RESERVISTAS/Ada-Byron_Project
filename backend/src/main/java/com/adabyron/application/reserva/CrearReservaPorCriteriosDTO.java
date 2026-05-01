package com.adabyron.application.reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.CategoriaId;
import com.adabyron.domain.reserva.TipoUsoReserva;

public record CrearReservaPorCriteriosDTO(
    UUID reservadaPorId,
    String categoria,
    int numeroAsistentes,
    int numEspacios,
    LocalDate fecha,
    LocalTime horaInicio,
    int duracionMinutos,
    TipoUsoReserva tipoUso,
    String detallesAdicionales
) {}
