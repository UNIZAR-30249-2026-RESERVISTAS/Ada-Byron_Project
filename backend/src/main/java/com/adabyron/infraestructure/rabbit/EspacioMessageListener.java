package com.adabyron.infraestructure.rabbit;

import com.adabyron.application.espacio.*;
import com.adabyron.domain.edificio.Edificio;
import com.adabyron.domain.espacio.Asignacion;
import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.exception.EspacioNotFoundException;
import com.adabyron.domain.espacio.exception.HorarioInvalidoException;
import com.adabyron.domain.reserva.exception.OperacionNoAutorizadaException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class EspacioMessageListener {

    private final EspacioService espacioService;

    public EspacioMessageListener(EspacioService espacioService) {
        this.espacioService = espacioService;
    }

    @RabbitListener(queues = "espacio.buscar.porId")
    public EspacioDTO onBuscarEspacio(String id) {
        try {
            Espacio espacio = espacioService.obtenerDetalles(id);

            return EspacioDTO.fromEntity(espacio);
        } catch (IllegalArgumentException | EspacioNotFoundException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "espacio.cambiarCategoria")
    public EspacioDTO onCambiarCategoria(CambiarCategoriaCommand comando) {
        try {
            Espacio espacio = espacioService.cambiarCategoria(
                    comando.espacioId(),
                    Categoria.desdeNombre(comando.dto().categoria())
            );
            return EspacioDTO.fromEntity(espacio);

        } catch (IllegalArgumentException | OperacionNoAutorizadaException | EspacioNotFoundException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "espacio.cambiarEstado")
    public EspacioDTO onCambiarEstado(CambiarEstadoCommand comando) {
        try {
            Espacio espacio = espacioService.cambiarReservable(
                    comando.espacioId(),
                    comando.dto().reservable()
            );
            return EspacioDTO.fromEntity(espacio);

        } catch (IllegalArgumentException | OperacionNoAutorizadaException | EspacioNotFoundException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "espacio.obtenerHorario")
    public HorarioDTO onObtenerHorario(String id) {
        try {
            Espacio espacio = espacioService.obtenerDetalles(id);

            return HorarioDTO.fromDomain(espacio.getHorarioDisponible(), !espacio.tieneHorarioEspecifico());
        } catch (IllegalArgumentException | EspacioNotFoundException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "espacio.cambiarHorario")
    public EspacioDTO onCambiarHorario(CambiarHorarioCommand comando) {
        try {
            Espacio espacio = espacioService.cambiarHorario(
                    comando.idEspacio(),
                    comando.nuevoHorario(),
                    comando.idGerente()
            );
            return EspacioDTO.fromEntity(espacio);

        } catch (IllegalArgumentException | HorarioInvalidoException | OperacionNoAutorizadaException | EspacioNotFoundException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "espacio.restablecerHorario")
    public EspacioDTO onRestablecerHorario(RestablecerHorarioCommand comando) {
        try {
            Espacio espacio = espacioService.restablecerHorario(
                    comando.idEspacio(),
                    comando.idGerente()
            );
            return EspacioDTO.fromEntity(espacio);

        } catch (IllegalArgumentException | OperacionNoAutorizadaException | EspacioNotFoundException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "espacio.obtenerAsignacion")
    public AsignacionDTO onObtenerAsignacion(String id) {
        try {
            Asignacion asignacion = espacioService.obtenerAsignacion(id);

            return AsignacionDTO.fromDomain(asignacion);
        } catch (IllegalArgumentException | EspacioNotFoundException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "espacio.asignarAEina")
    public EspacioDTO onAsignarAEina(String id) {
        try {
            Espacio espacio = espacioService.asignarAEina(id);

            return EspacioDTO.fromEntity(espacio);
        } catch (IllegalArgumentException | OperacionNoAutorizadaException | EspacioNotFoundException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "espacio.asignarADepartamento")
    public EspacioDTO onAsignarADepartamento(AsignarADepartamentoCommand command) {
        try {
            Espacio espacio = espacioService.asignarADepartamento(command.espacioId(), command.dto().departamentoId());

            return EspacioDTO.fromEntity(espacio);
        } catch (IllegalArgumentException | OperacionNoAutorizadaException | EspacioNotFoundException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "espacio.asignarAPersonas")
    public EspacioDTO onAsignarAPersonas(AsignarAPersonasCommand command) {
        try {
            Espacio espacio = espacioService.asignarAPersonas(command.espacioId(), command.dto().personaIds());

            return EspacioDTO.fromEntity(espacio);
        } catch (IllegalArgumentException | OperacionNoAutorizadaException | EspacioNotFoundException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "espacio.filtrarPorAforo")
    public List<String> onFiltrarPorAforo(Integer personasBuscadas) {

        // El Listener obtiene el porcentaje actual directamente del dominio
        double porcentajeActual = Edificio.getPorcentajeOcupacionMaxima();

        int ocupantesNecesarios = (int) Math.ceil(personasBuscadas / porcentajeActual);

        // Hace la llamada al servicio que ejecuta la query en la base de datos
        // y devuelve directamente la lista de IDs (RabbitMQ se encarga de serializarla)
        return espacioService.obtenerIdsPorAforo(ocupantesNecesarios);
    }
}
