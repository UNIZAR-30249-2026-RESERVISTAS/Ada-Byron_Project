package com.adabyron.application.espacio;

import java.util.UUID;

public record RestablecerHorarioCommand(
        String idEspacio,
        UUID idGerente
) {
}
