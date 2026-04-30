package com.adabyron.application.persona;

import java.util.UUID;

public record CambiarRolCommand(
        UUID personaId,
        CambiarRolDTO dto
) {}