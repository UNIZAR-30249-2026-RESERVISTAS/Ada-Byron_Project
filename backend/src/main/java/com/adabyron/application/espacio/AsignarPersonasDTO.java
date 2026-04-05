package com.adabyron.application.espacio;

import java.util.Set;
import java.util.UUID;

public record AsignarPersonasDTO(
    Set<UUID> personaIds
) {
    
}
