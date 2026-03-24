package com.adabyron.application.reserva;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import com.adabyron.domain.reserva.TipoUsoReserva;

public record CrearReservaDTO(
    List<String> espacioIds,          // REQ-E1 — uno o varios 
    UUID reservadaPorId,              // PersonaId del solicitante
    TipoUsoReserva tipoUso,           // REQ-E1 — docencia, investigación, gestión, otros
    int numeroAsistentes,             // 
    LocalDate fecha,                  // 
    LocalTime horaInicio,             // 
    int duracionMinutos,              // 
    String detallesAdicionales        // REQ-E3 — campo libre, puede ser null
) {
}