package com.adabyron.infraestructure.rabbit;

import com.adabyron.application.reserva.*;
import com.adabyron.domain.reserva.exception.OperacionNoAutorizadaException;
import com.adabyron.domain.reserva.exception.ReservaNotFoundException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.adabyron.domain.reserva.Reserva;

import java.util.List;
import java.util.UUID;

@Component
public class ReservaMessageListener {

    private final ReservaService reservaService;

    public ReservaMessageListener(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @RabbitListener(queues = "reserva.crear")
    public ReservaDTO onCrearReserva(CrearReservaDTO dto) {
        try {
            Reserva reserva = reservaService.crearReserva(dto);
            return ReservaDTO.fromEntity(reserva);

        } catch (IllegalArgumentException ex) {
            // Solo capturamos los errores previos (Persona o Espacio no existen)
            // Esto le dice a RabbitMQ: "El usuario ha mandado un ID falso, descarta el mensaje para siempre".
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }

    }

    @RabbitListener(queues = "reserva.crear.criterios")
    public ReservaDTO onCrearReservaPorCriterios(CrearReservaPorCriteriosDTO dto) {
        try {
            System.out.println("Datos que llegan por criterios" + dto);
            Reserva reserva = reservaService.crearReservaCriterios(dto);
            return ReservaDTO.fromEntity(reserva);

        } catch (IllegalArgumentException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "reserva.listar.activas")
    public List<ReservaDTO> onListarActivas(String mensaje) {
        return reservaService.listarReservasActivas().stream()
                .map(ReservaDTO::fromEntity)
                .toList();
    }

    @RabbitListener(queues = "reserva.buscar.porId")
    public ReservaDTO onBuscarReserva(UUID id) {
        try {
            Reserva reserva = reservaService.buscarPorId(id);

            return ReservaDTO.fromEntity(reserva);
        } catch (IllegalArgumentException | ReservaNotFoundException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "reserva.listar.porPersona")
    public List<ReservaDTO> onListarPorPersona(UUID personaId) {
        return reservaService.listarPorPersona(personaId).stream()
                            .map(ReservaDTO::fromEntity)
                            .toList();
    }

    @RabbitListener(queues = "reserva.listar.activas.porPersona")
    public List<ReservaDTO> onListarActivasPorPersona(UUID personaId) {
        return reservaService.listarActivasPorPersona(personaId).stream()
                .map(ReservaDTO::fromEntity)
                .toList();
    }

    @RabbitListener(queues = "reserva.listar.potencialmenteInvalidas")
    public List<ReservaDTO> onListarPotencialmenteInvalidas(String message) {
        return reservaService.listarPotencialmenteInvalidas().stream()
                .map(ReservaDTO::fromEntity)
                .toList();
    }

    @RabbitListener(queues = "reserva.cancelar")
    public String onCancelarReserva(CancelarReservaCommand comando) {
        try {
            reservaService.cancelarReserva(
                    comando.idReserva(),
                    comando.solicitanteId(),
                    comando.motivo()
            );

            return "OK";
        } catch (IllegalArgumentException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }

    }

    @RabbitListener(queues = "reserva.eliminar")
    public String onEliminarReserva(CancelarReservaCommand comando) {
        try {
            reservaService.eliminarReserva(
                    comando.idReserva(),
                    comando.solicitanteId()
            );

            return "OK";
        } catch (IllegalArgumentException | ReservaNotFoundException | OperacionNoAutorizadaException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "reserva.revalidar")
    public ReservaDTO onRevalidarReserva(RevalidarReservaCommand comando) {
        try {
            Reserva reserva = reservaService.revalidarReserva(
                    comando.idReserva(),
                    comando.gerenteId()
            );
            return ReservaDTO.fromEntity(reserva);

        } catch (IllegalArgumentException | ReservaNotFoundException | OperacionNoAutorizadaException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }
}