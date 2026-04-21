package com.adabyron.infraestructure.rabbit;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class RabbitMQConfig {
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                         MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }

    @Bean
    public Queue queueCrearReserva() {
        return new Queue("reserva.crear");
    }

    @Bean
    public Queue queueEliminarReserva() {
        return new Queue("reserva.eliminar");
    }

    @Bean
    public Queue queueCancelarReserva() {
        return new Queue("reserva.cancelar");
    }

    @Bean
    public Queue queueListarReservasActivas() {
        return new Queue("reserva.listar.activas");
    }

    @Bean
    public Queue queueBuscarReservaPorId() {
        return new Queue("reserva.buscar.porId");
    }

    @Bean
    public Queue queueListarReservaPorPersona() {
        return new Queue("reserva.listar.porPersona");
    }

    @Bean
    public Queue queueListarReservasActivasPorPersona() {
        return new Queue("reserva.listar.activas.porPersona");
    }

    @Bean
    public Queue queueListarReservasPotencialmenteInvalidas() {
        return new Queue("reserva.listar.potencialmenteInvalidas");
    }

    @Bean
    public Queue queueRevalidarReserva() {
        return new Queue("reserva.revalidar");
    }
}
