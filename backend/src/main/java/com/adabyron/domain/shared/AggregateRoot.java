package com.adabyron.domain.shared;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AggregateRoot {

    // Lista mutable internamente, pero expuesta solo como inmutable
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    /**
     * Registra un evento ocurrido dentro del agregado.
     * Llamado desde los comandos del Aggregate Root.
     */
    protected void registerEvent(DomainEvent event) {
        if (event == null)
            throw new IllegalArgumentException("El evento no puede ser nulo");
        domainEvents.add(event);
    }

    /**
     * Devuelve los eventos acumulados como lista inmutable.
     * El Application Service los lee para despacharlos.
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Limpia la lista tras el despacho.
     * Llamado por el Application Service después de publicar los eventos.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }

    /**
     * Indica si hay eventos pendientes de despachar.
     * Útil para tests y para el Application Service.
     */
    public boolean hasEvents() {
        return !domainEvents.isEmpty();
    }
}
