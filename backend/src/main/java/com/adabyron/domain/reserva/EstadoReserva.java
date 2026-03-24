package com.adabyron.domain.reserva;


/**
 * ENUM — Estados del ciclo de vida de una Reserva.
 *
 * Autómata finito (del documento de requisitos):
 *
 *   SOLICITADA ──validacionOK──► CONFIRMADA ──cambioCondiciones──► POTENCIALMENTE_INVALIDA
 *   SOLICITADA ──validacionNoOK─► RECHAZADA                               │
 *   CONFIRMADA ──eliminarReserva(gerente)──► CANCELADA                    │
 *   POTENCIALMENTE_INVALIDA ──validarGerente──► CONFIRMADA                │
 *   POTENCIALMENTE_INVALIDA ──eliminarReserva──► CANCELADA ◄──────────────┘
 *
 * O4: Las reservas inválidas no se borran automáticamente, pasan a POTENCIALMENTE_INVALIDA.
 */
public enum EstadoReserva {
    SOLICITADA,
    CONFIRMADA,
    RECHAZADA,
    POTENCIALMENTE_INVALIDA,
    CANCELADA;

    public boolean estaActiva() {
        return this == CONFIRMADA || this == POTENCIALMENTE_INVALIDA;
    }

    public boolean esModificable() {
        return this == CONFIRMADA || this == POTENCIALMENTE_INVALIDA;
    }
}
