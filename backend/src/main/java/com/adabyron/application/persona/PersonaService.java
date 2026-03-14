package com.adabyron.application.persona;

import com.adabyron.domain.persona.*;
import com.adabyron.domain.persona.exception.PersonaNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio para la gestión de personas.
 */
@Service
@Transactional
public class PersonaService {

    private final PersonaRepository personaRepository;

    public PersonaService(PersonaRepository personaRepository) {
        this.personaRepository = personaRepository;
    }

    public Persona crearPersona(CrearPersonaDTO dto) {
        if (personaRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Ya existe una persona con el email: " + dto.email());
        }
        Rol rol = Rol.valueOf(dto.rol());
        DepartamentoId deptId = dto.departamentoId() != null
                ? new DepartamentoId(dto.departamentoId())
                : null;
        Persona persona = PersonaFactory.crearNuevaPersona(dto.nombre(), dto.email(), rol, deptId);
        return personaRepository.save(persona);
    }

    @Transactional(readOnly = true) 
    public List<Persona> listarTodas() {
        return personaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Persona buscarPorId(UUID id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Persona buscarPorEmail(String email) {
        return personaRepository.findByEmail(email)
                .orElseThrow(() -> new PersonaNotFoundException(email));
    }

    public Persona cambiarRol(UUID id, CambiarRolDTO dto) {
        Persona persona = buscarPorId(id);
        Rol nuevoRol = Rol.valueOf(dto.rol());
        DepartamentoId deptId = dto.departamentoId() != null
                ? new DepartamentoId(dto.departamentoId())
                : null;
        persona.cambiarRol(nuevoRol, deptId);
        return personaRepository.save(persona);
    }

    public Persona añadirRolGerente(UUID id) {
        Persona persona = buscarPorId(id);
        persona.añadirRolGerente();
        return personaRepository.save(persona);
    }

    public Persona quitarRolGerente(UUID id) {
        Persona persona = buscarPorId(id);
        persona.quitarRolGerente();
        return personaRepository.save(persona);
    }

    public void eliminar(UUID id) {
        if (!personaRepository.existsById(id)) {
            throw new PersonaNotFoundException(id);
        }
        personaRepository.deleteById(id);
    }
}
