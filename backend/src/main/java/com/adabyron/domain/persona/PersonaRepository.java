package com.adabyron.domain.persona;

import java.util.Optional;
import java.util.List;


/**
 * Puerto de Salida - interfaz que el dominio expone para que el adaptador
 * de infraestructura pueda implementar.
 *
 */
public interface PersonaRepository {

    Optional<Persona> findById(PersonaId id);

    // REQ-B7 - El sistema debe permitir loguearse a un usuario
    Optional<Persona> findByEmail(Email email);

    // Para la UI de gestión de personas (será gestionado por el admin)
    List<Persona> findAll();

    List<Persona> findByRol(Rol rol);

    List<Persona> findByDepartamento(DepartamentoId departamentoId);

    void save(Persona persona);
    void saveAll(List<Persona> personas);
    boolean existsByEmail(Email email);
}
