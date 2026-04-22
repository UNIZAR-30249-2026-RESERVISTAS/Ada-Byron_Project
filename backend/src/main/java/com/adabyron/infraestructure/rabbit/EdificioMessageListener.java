package com.adabyron.infraestructure.rabbit;

import com.adabyron.application.edificio.CambiarPorcentajeOcupacionDTO;
import com.adabyron.application.edificio.EdificioOcupacionDTO;
import com.adabyron.application.edificio.EdificioService;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EdificioMessageListener {
    private final EdificioService edificioService;

    public EdificioMessageListener(EdificioService edificioService) {
        this.edificioService = edificioService;
    }

    @RabbitListener(queues = "edificio.porcentajeOcupacion")
    public EdificioOcupacionDTO onPorcentajeOcupacion(String message){
        return new EdificioOcupacionDTO(edificioService.obtenerPorcentajeOcupacionMaxima());
    }

    @RabbitListener(queues = "edificio.cambiarPorcentajeOcupacion")
    public EdificioOcupacionDTO onCambiarOcupacion(CambiarPorcentajeOcupacionDTO dto) {
        try {
            return new EdificioOcupacionDTO(edificioService.cambiarPorcentajeOcupacionMaxima(dto));
        } catch (IllegalArgumentException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }
}
