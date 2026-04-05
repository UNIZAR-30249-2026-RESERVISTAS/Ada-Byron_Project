package com.adabyron.application.espacio;

import com.adabyron.domain.espacio.Asignacion;
import com.adabyron.domain.persona.PersonaId;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record AsignacionDTO(
    String tipo,
    Integer departamentoId,
    Set<UUID> personaIds
) {
    public static AsignacionDTO fromDomain(Asignacion asignacion) {
        return new AsignacionDTO(
            asignacion.tipo().name(),
            asignacion.getDepartamentoId() != null ? asignacion.getDepartamentoId().valor() : null,
            asignacion.getPersonaIds().stream().map(PersonaId::valor).collect(Collectors.toSet())
        );
    }
}
