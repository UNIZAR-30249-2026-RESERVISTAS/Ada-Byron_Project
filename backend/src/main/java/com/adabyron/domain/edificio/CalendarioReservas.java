package com.adabyron.domain.edificio;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

public record CalendarioReservas(
    Set<DayOfWeek> diasSemanaNoReservables,
    Set<LocalDate> festivos
    ) {

    public CalendarioReservas {
        diasSemanaNoReservables = diasSemanaNoReservables == null ? Set.of() : Set.copyOf(diasSemanaNoReservables);
        festivos = festivos == null ? Set.of() : Set.copyOf(festivos);
    }

    public static CalendarioReservas porDefecto() {
        return new CalendarioReservas(Set.of(DayOfWeek.SUNDAY), Set.of());
    }

    public boolean esReservable(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        return !diasSemanaNoReservables.contains(fecha.getDayOfWeek())
        && !festivos.contains(fecha);
    }
}
