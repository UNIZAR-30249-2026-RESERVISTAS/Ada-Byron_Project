package com.adabyron.infraestructure.persistence.persona;

import com.adabyron.domain.persona.DepartamentoId;
import com.adabyron.domain.persona.Persona;
import com.adabyron.domain.persona.PersonaId;
import com.adabyron.domain.persona.PersonaRepository;
import com.adabyron.domain.persona.Rol;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PersonaRepositoryJpa implements PersonaRepository {

    private final SpringDataPersonaRepository jpa;

    public PersonaRepositoryJpa(SpringDataPersonaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Persona save(Persona persona) {
        PersonaJpaEntity entity = toEntity(persona); 
        jpa.save(entity); 
        return persona;
    }

    @Override
    public Optional<Persona> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Persona> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public List<Persona> findAll() {
        return jpa.findAll().stream()
                  .map(this::toDomain)
                  .toList();
    }

    @Override
    public List<Persona> findByRol(Rol rol) {
        return jpa.findByRol(rol).stream()
                  .map(this::toDomain)
                  .toList();
    }

    @Override
    public List<Persona> findByDepartamentoId(Integer departamentoId) {
        return jpa.findByDepartamentoId(departamentoId).stream()
                  .map(this::toDomain)
                  .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpa.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    // Mappers
    private PersonaJpaEntity toEntity(Persona persona) {
        return new PersonaJpaEntity(
            persona.getId(),
            persona.getNombre(),
            persona.getEmail(),
            persona.getRoles(),
            persona.getDepartamentoId() != null 
                ? persona.getDepartamentoId().valor() 
                : null
        );
    }

    private Persona toDomain(PersonaJpaEntity entity) {
        return new Persona(
            new PersonaId(entity.getId()),
            entity.getNombre(),
            entity.getEmail(),
            entity.getRoles(),
            entity.getDepartamentoId() != null 
                ? new DepartamentoId(entity.getDepartamentoId()) 
                : null
        );
    }
}
