package com.adabyron.domain.persona;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * Puerto de Salida - interfaz que el dominio expone para que el adaptador
 * de infraestructura pueda implementar.
 *
 */
public interface PersonaRepository extends JpaRepository<Persona, PersonaId> {

    // Optional<Persona> findById(PersonaId id);

    // REQ-B7 - El sistema debe permitir loguearse a un usuario
    // No es necesario implementar este método, el JpaRepository ya lo implementa.
    // Eso si, debo de declararlo para que el adaptador de infraestructura lo pueda usar, ya que el JpaRepository no lo expone.
    //Optional<Persona> findByEmail(Email email);
    void findByEmail(Email email);
    void findById(Persona id);

    // Para la UI de gestión de personas (será gestionado por el admin) En principio no es necesario
    // Ya lo implementa el JpaRepository.
    List<Persona> findAll();

    List<Persona> findByRol(Rol rol);

    List<Persona> findByDepartamento(DepartamentoId departamentoId);

    boolean existsByEmail(Email email);


    // No es necesario implementar estos métodos, el JpaRepository ya los implementa.
    //void save(Persona persona);
    //void saveAll(List<Persona> personas);
}
