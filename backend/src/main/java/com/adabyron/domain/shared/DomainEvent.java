package com.adabyron.domain.shared;

import java.time.Instant;

/**
 * Interfaz base para todos los eventos de dominio.
 *
 * Un evento de dominio representa algo que OCURRIÓ en el dominio
 * y que puede ser relevante para otras partes del sistema.
 *
 * CARACTERÍSTICAS:
 *   - Son inmutables (records en Java).
 *   - Describen hechos en pasado: PersonaCreada, RolCambiado...
 *   - No contienen lógica, solo datos del hecho ocurrido.
 *   - El timestamp se rellena automáticamente al crearse.
 */
public interface DomainEvent {
    Instant ocurredAt();
}
