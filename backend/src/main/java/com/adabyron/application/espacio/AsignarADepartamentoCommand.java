package com.adabyron.application.espacio;

public record AsignarADepartamentoCommand(
        String espacioId,
        AsignarDepartamentoDTO dto
) {
}
