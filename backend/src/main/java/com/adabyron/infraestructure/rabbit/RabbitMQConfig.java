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

    //Colas para el controlador de Reserva
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

    //Colas para el controlador de Persona
    @Bean
    public Queue queueCrearPersona() {
        return new Queue("persona.crear");
    }

    @Bean
    public Queue queueListarPersonas() { return new Queue("persona.listar"); }

    @Bean
    public Queue queueBuscarPersonaPorId() { return new Queue("persona.buscar.porId"); }

    @Bean
    public Queue queueBuscarPersonaPorEmail() { return new Queue("persona.buscar.porEmail"); }

    @Bean
    public Queue queueCambiarRolPersona() { return new Queue("persona.cambiarRol"); }

    @Bean
    public Queue queueAñadirGerente() { return new Queue("persona.añadirGerente"); }

    @Bean
    public Queue queueQuitarGerente() { return new Queue("persona.quitarGerente"); }

    @Bean
    public Queue queueCambiarDepartamento() { return new Queue("persona.cambiarDepartamento"); }

    @Bean
    public Queue queueEliminarPersona() { return new Queue("persona.eliminar"); }

    //Colas para el controlador de Departamento
    @Bean
    public Queue queueListarDepartamentos() { return new Queue("departamento.listar"); }

    @Bean
    public Queue queueBuscarDepartamentoPorId() { return new Queue("departamento.buscar.porId"); }

    //Colas para el controlador de Edificio
    @Bean
    public Queue queuePorcentajeOcupacionEdifico(){ return new Queue("edificio.porcentajeOcupacion"); }

    @Bean
    public Queue queueCambiarPorcentajeOcupacionEdificio(){ return new Queue("edificio.cambiarPorcentajeOcupacion"); }

    //Colas para el controlador de Espacio
    @Bean
    public Queue queueBuscarEspacioPorId() { return new Queue("espacio.buscar.porId"); }

    @Bean
    public Queue queueCambiarCategoria() { return new Queue("espacio.cambiarCategoria"); }

    @Bean
    public Queue queueCambiarEstado() { return new Queue("espacio.cambiarEstado"); }

    @Bean
    public Queue queueObtenerHorario() { return new Queue("espacio.obtenerHorario"); }

    @Bean
    public Queue queueCambiarHorario() { return new Queue("espacio.cambiarHorario"); }

    @Bean
    public Queue queueRestablecerHorario() { return new Queue("espacio.restablecerHorario"); }

    @Bean
    public Queue queueObtenerAsignacion() { return new Queue("espacio.obtenerAsignacion"); }

    @Bean
    public Queue queueAsignarAEina() { return new Queue("espacio.asignarAEina"); }

    @Bean
    public Queue queueAsignarADepartamento() { return new Queue("espacio.asignarADepartamento"); }

    @Bean
    public Queue queueAsignarAPersonas() { return new Queue("espacio.asignarAPersonas"); }





}
