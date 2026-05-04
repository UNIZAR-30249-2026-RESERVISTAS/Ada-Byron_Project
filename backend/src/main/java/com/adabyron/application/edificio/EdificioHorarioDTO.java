package com.adabyron.application.edificio;

import java.time.LocalTime;

public record EdificioHorarioDTO(
        LocalTime apertura,
        LocalTime cierre
) {
}
