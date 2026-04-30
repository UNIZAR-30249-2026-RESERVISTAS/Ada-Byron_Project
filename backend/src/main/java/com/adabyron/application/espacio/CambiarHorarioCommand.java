package com.adabyron.application.espacio;

import com.adabyron.domain.espacio.HorarioDisponible;

import java.util.UUID;

public record CambiarHorarioCommand(
        String idEspacio,
        HorarioDisponible nuevoHorario,
        UUID idGerente
) {
}
