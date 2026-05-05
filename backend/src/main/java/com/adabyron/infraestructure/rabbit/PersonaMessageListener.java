package com.adabyron.infraestructure.rabbit;

import com.adabyron.application.persona.*;
import com.adabyron.domain.persona.Persona;
import com.adabyron.domain.persona.exception.DepartamentoRequeridoException;
import com.adabyron.domain.persona.exception.PersonaNotFoundException;
import com.adabyron.domain.persona.exception.RolIncompatibleException;
import com.adabyron.domain.persona.exception.RolInmutable;
import com.adabyron.domain.reserva.exception.OperacionNoAutorizadaException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class PersonaMessageListener {
    private final PersonaService personaService;

    public PersonaMessageListener(PersonaService personaService) {
        this.personaService = personaService;
    }

    @RabbitListener(queues = "persona.crear")
    public PersonaDTO onCrearPersona(CrearPersonaDTO dto){
        try {
            Persona persona =  personaService.crearPersona(dto);
            return PersonaDTO.fromEntity(persona);
        } catch (IllegalArgumentException | DepartamentoRequeridoException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "persona.listar")
    public List<PersonaDTO> onListarPersonas(String mensaje){
        return personaService.listarTodas().stream()
                .map(PersonaDTO::fromEntity)
                .toList();
    }

    @RabbitListener(queues = "persona.buscar.porId")
    public PersonaDTO onBuscarPersonaPorId(UUID id){
        try{
            Persona persona = personaService.buscarPorId(id);
            return PersonaDTO.fromEntity(persona);
        } catch(IllegalArgumentException | PersonaNotFoundException ex){
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "persona.buscar.porEmail")
    public PersonaDTO onBuscarPersonaPorEmail(String email){
        try{
            Persona persona = personaService.buscarPorEmail(email);
            return PersonaDTO.fromEntity(persona);
        } catch(IllegalArgumentException | PersonaNotFoundException ex){
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "persona.cambiarRol")
    public PersonaDTO onCambiarRolPersona(CambiarRolCommand comando) {
        try {
            Persona persona = personaService.cambiarRol(
                    comando.personaId(),
                    comando.dto()
            );
            return PersonaDTO.fromEntity(persona);

        } catch (IllegalArgumentException | OperacionNoAutorizadaException | PersonaNotFoundException | RolInmutable |
                 RolIncompatibleException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "persona.añadirGerente")
    public PersonaDTO onAñadirGerente(UUID id){
        try{
            Persona persona = personaService.añadirRolGerente(id);
            return PersonaDTO.fromEntity(persona);
        } catch(IllegalArgumentException | PersonaNotFoundException | OperacionNoAutorizadaException | IllegalStateException ex){
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "persona.quitarGerente")
    public PersonaDTO onQuitarGerente(UUID id){
        try{
            Persona persona = personaService.quitarRolGerente(id);
            return PersonaDTO.fromEntity(persona);
        } catch(IllegalArgumentException | PersonaNotFoundException | OperacionNoAutorizadaException ex){
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "persona.cambiarDepartamento")
    public PersonaDTO onCambiarDepartamento(CambiarDepartamentoCommand comando) {
        try {
            Persona persona = personaService.cambiarDepartamento(
                    comando.personaId(),
                    comando.dto()
            );
            return PersonaDTO.fromEntity(persona);

        } catch (IllegalArgumentException | PersonaNotFoundException | OperacionNoAutorizadaException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

    @RabbitListener(queues = "persona.eliminar")
    public String onEliminarPersona(UUID id){
        try {
            personaService.eliminar(id);
            return "OK";
        } catch (IllegalArgumentException | PersonaNotFoundException | OperacionNoAutorizadaException ex) {
            throw new AmqpRejectAndDontRequeueException(ex.getMessage(), ex);
        }
    }

}
