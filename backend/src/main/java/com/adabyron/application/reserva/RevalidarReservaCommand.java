package com.adabyron.application.reserva;

import java.util.UUID;

public record RevalidarReservaCommand(
        UUID idReserva,
        UUID gerenteId
) {}