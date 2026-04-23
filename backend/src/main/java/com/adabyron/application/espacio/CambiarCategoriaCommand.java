package com.adabyron.application.espacio;

public record CambiarCategoriaCommand(
        String espacioId,
        CambiarCategoriaDTO dto
) {
}
