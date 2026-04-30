package com.adabyron.application.espacio;

public record CambiarEstadoCommand(
        String espacioId,
        CambiarReservableDTO dto
) {
}
