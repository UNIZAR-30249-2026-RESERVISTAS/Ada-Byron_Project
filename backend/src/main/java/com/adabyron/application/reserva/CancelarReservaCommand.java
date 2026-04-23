package com.adabyron.application.reserva;

import java.util.UUID;

public record CancelarReservaCommand(
        UUID idReserva,
        UUID solicitanteId,
        String motivo
) {}