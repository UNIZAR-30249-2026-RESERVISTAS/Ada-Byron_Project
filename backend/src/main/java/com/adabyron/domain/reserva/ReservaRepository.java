package com.adabyron.domain.reserva;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;


public interface ReservaRepository {
    Reserva save(Reserva reserva);

    Optional<Reserva> findById(UUID id);

    // REQ-H1 Gerente Consultan todas las reservas cuya hora de finalización es posterior al momento actual, y su estado es CONFIRMADA O POTENCIALMENTE_INVÁLIDA.
    List<Reserva> findReservasActivas(LocalDateTime ahora);

    // REQ-F7 Para validar solapamientos
    List<Reserva> findActivasByEspacioId(String espacioId);

    // Para obtener el historial completo de reservas de una persona, independientemente de su estado actual.
    List<Reserva> findByReservadaPorId(UUID personaId);

    // O4 - El gerente puede listar las reservas potencialmente inválidas para decidir si las confirma o cancela
    List<Reserva> findPotencialmenteInvalidas(LocalDateTime ahora);

    void deleteById(UUID id);
}
