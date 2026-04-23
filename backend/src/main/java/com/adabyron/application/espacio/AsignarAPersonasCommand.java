package com.adabyron.application.espacio;

public record AsignarAPersonasCommand(
        String espacioId,
        AsignarPersonasDTO dto
) {
}
