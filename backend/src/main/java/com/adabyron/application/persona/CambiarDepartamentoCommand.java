package com.adabyron.application.persona;

import java.util.UUID;

public record CambiarDepartamentoCommand(
        UUID personaId,
        CambiarDepartamentoDTO dto
) {
}
