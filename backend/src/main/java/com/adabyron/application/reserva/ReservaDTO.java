package com.adabyron.application.reserva;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.adabyron.domain.reserva.EstadoReserva;
import com.adabyron.domain.reserva.Reserva;
import com.adabyron.domain.reserva.TipoUsoReserva;

/**
 * DTO de salida para exponer los datos de una Reserva a través de la API.
 */
public record ReservaDTO (
    UUID id,
    List<String> espacioIds,
    UUID reservadaPorId,
    TipoUsoReserva tipoUso,
    int numeroAsistentes,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    String detallesAdicionales,
    EstadoReserva estado,
    String motivoRechazoOCancelacion
){
    public static ReservaDTO fromEntity(Reserva reserva) {
        return new ReservaDTO(
            reserva.getIdRaw(),
            reserva.getEspacioIds().stream()
                   .map(e -> e.id())
                   .toList(),
            reserva.getReservadaPorIdRaw(),
            reserva.getTipoUso(),
            reserva.getNumeroAsistentes(),
            reserva.getIntervalo().fechaInicio(),
            reserva.getIntervalo().fechaFin(),
            reserva.getDetallesAdicionales(),
            reserva.getEstado(),
            reserva.getMotivoRechazoOCancelacion()
        );
    }
    
}
