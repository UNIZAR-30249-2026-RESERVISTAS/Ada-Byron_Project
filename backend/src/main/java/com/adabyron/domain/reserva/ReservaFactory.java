package com.adabyron.domain.reserva;

import java.util.List;
import com.adabyron.domain.espacio.*;
import com.adabyron.domain.persona.PersonaId;


/**
 * FACTORY — Reserva
 *
 * Seguimos el mismo patrón que enPersonaFactory, centralizamos la creación
 * e hidratación de instancias de Reserva fuera del Agregado Raíz.
 *
 * Dos responsabilidades:
 *   - crearNuevaReserva: crea una reserva nueva en estado SOLICITADA
 *   - reconstruirReserva: rehidrata una reserva desde la base de datos
 */
public class ReservaFactory {
    /**
     * Crea una nueva Reserva en estado SOLICITADA.
     *
     * La validación de las reglas de negocio (F1-F8) debe haberse
     * ejecutado en ReservaValidacionService ANTES de llamar a este método.
     *
     * @param espacioIds          Lista de espacios reservados (al menos uno)
     * @param reservadaPorId      PersonaId del solicitante
     * @param tipoUsoReserva      Tipo de uso (docencia, investigación, gestión, otros)
     * @param numeroAsistentes    Número de personas que asistirán
     * @param intervalo           Intervalo temporal de la reserva
     * @param detallesAdicionales Texto libre con explicaciones adicionales (REQ-E3)
     */
    public static Reserva crearNuevaReserva(List<EspacioId> espacioIds,
                                             PersonaId reservadaPorId,
                                             TipoUsoReserva tipoUso,
                                             int numeroAsistentes,
                                             IntervaloTemporal intervalo,
                                             String detallesAdicionales) {
        return new Reserva(
            ReservaId.generar(),
            espacioIds,
            reservadaPorId,
            tipoUso,
            numeroAsistentes,
            intervalo,
            detallesAdicionales
        );
    }

    /**
     * Reconstruye una Reserva a partir de sus datos almacenados en la BD.
     * No disparamos validaciones, confiamos en que la BD contiene datos válidos.
     */
    public static Reserva reconstruirReserva(ReservaId id,
                                              List<EspacioId> espacioIds,
                                              PersonaId reservadaPorId,
                                              TipoUsoReserva tipoUso,
                                              int numeroAsistentes,
                                              IntervaloTemporal intervalo,
                                              String detallesAdicionales,
                                              EstadoReserva estado,
                                              String motivoRechazoOCancelacion) {
        return new Reserva(
            id,
            espacioIds,
            reservadaPorId,
            tipoUso,
            numeroAsistentes,
            intervalo,
            detallesAdicionales,
            estado,
            motivoRechazoOCancelacion
        );
    }
}