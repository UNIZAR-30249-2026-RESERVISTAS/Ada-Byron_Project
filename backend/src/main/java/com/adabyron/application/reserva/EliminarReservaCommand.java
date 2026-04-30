package com.adabyron.application.reserva;

import java.util.UUID;

public record EliminarReservaCommand(
        UUID idReserva,
        UUID solicitanteId
) {}