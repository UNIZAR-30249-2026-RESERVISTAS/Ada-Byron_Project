// backend/src/main/java/com/adabyron/domain/espacio/Asignacion.java
package com.adabyron.domain.espacio;

import com.adabyron.domain.persona.DepartamentoId;
import com.adabyron.domain.persona.PersonaId;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Representa la asignación de un espacio a la EINA, a un departamento o a personas específicas.
 * - Si el espacio está asignado a la EINA, puede ser reservado por cualquier persona
 * - Si el espacio está asignado a un departamento, solo las personas de ese departamento pueden reservarlo
 * - Si el espacio está asignado a personas específicas, solo esas personas pueden reservarlo
 */
public record Asignacion(Tipo tipo, Integer departamentoId, Set<UUID> personaIds) {

    public enum Tipo { EINA, DEPARTAMENTO, PERSONAS }

    public Asignacion {
        Objects.requireNonNull(tipo, "El tipo de asignación es obligatorio");
        personaIds = personaIds == null ? Set.of() : Set.copyOf(personaIds);

        switch (tipo) {
            case EINA -> {
                if (departamentoId != null || !personaIds.isEmpty()) {
                    throw new IllegalArgumentException("Asignación EINA no admite departamento ni personas");
                }
            }
            case DEPARTAMENTO -> {
                if (departamentoId == null || !personaIds.isEmpty()) {
                    throw new IllegalArgumentException("Asignación DEPARTAMENTO requiere departamento y no admite personas");
                }
            }
            case PERSONAS -> {
                if (departamentoId != null || personaIds.isEmpty()) {
                    throw new IllegalArgumentException("Asignación PERSONAS requiere al menos una persona y no admite departamento");
                }
            }
        }
    }

    public static Asignacion eina() {
        return new Asignacion(Tipo.EINA, null, Set.of());
    }

    public static Asignacion departamento(DepartamentoId departamentoId) {
        Objects.requireNonNull(departamentoId, "Departamento obligatorio");
        return new Asignacion(Tipo.DEPARTAMENTO, departamentoId.valor(), Set.of());
    }

    public static Asignacion personas(Set<PersonaId> personas) {
        if (personas == null || personas.isEmpty()) {
            throw new IllegalArgumentException("Debe haber al menos una persona");
        }
        Set<UUID> ids = personas.stream().map(PersonaId::valor).collect(Collectors.toUnmodifiableSet());
        return new Asignacion(Tipo.PERSONAS, null, ids);
    }

    public boolean esEina() { return tipo == Tipo.EINA; }
    public boolean esDepartamento() { return tipo == Tipo.DEPARTAMENTO; }
    public boolean esPersonas() { return tipo == Tipo.PERSONAS; }

    public DepartamentoId getDepartamentoId() {
        return departamentoId == null ? null : new DepartamentoId(departamentoId);
    }

    public Set<PersonaId> getPersonaIds() {
        return personaIds.stream().map(PersonaId::new).collect(Collectors.toUnmodifiableSet());
    }
}