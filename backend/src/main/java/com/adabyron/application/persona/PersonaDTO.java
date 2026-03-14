package com.adabyron.application.persona;

import com.adabyron.domain.persona.Persona;
import com.adabyron.domain.persona.Rol;

import java.util.Set;
import java.util.stream.Collectors;

public record PersonaDTO(
    String id,
    String nombre,
    String email,
    Set<String> roles,
    Integer departamentoId
) {
    public static PersonaDTO fromEntity(Persona persona) {
        return new PersonaDTO(
            persona.getId().toString(),
            persona.getNombre(),
            persona.getEmail(),
            persona.getRoles().stream().map(Rol::name).collect(Collectors.toSet()),
            persona.getDepartamentoId() != null ? persona.getDepartamentoId().valor() : null
        );
    }
}
