package com.adabyron.infraestructure.persistence.persona;

import com.adabyron.domain.persona.Rol;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de persistencia para la entidad Persona, es necesario extender JpaRepository para aprovechar las funcionalidades de Spring Data JPA
 * Además, se definen métodos personalizados para buscar por email, rol y departamento, así como para verificar la existencia de una persona por
 * email o ID. Estos métodos personalizados se implementarán automáticamente por Spring Data JPA basándose en el nombre del método y las anotaciones utilizadas.
 * NOTA: No es necesario implementar esta interfaz, Spring Data JPA se encargará de generar la implementación en tiempo de ejecución.
 */
public interface SpringDataPersonaRepository extends JpaRepository<PersonaJpaEntity, UUID> {

    Optional<PersonaJpaEntity> findByEmail(String email);

    // Método personalizado para buscar personas por rol, es necesario usar @Query para realizar la consulta correcta debido a la relación de roles como colección
    @Query("SELECT p FROM PersonaJpaEntity p JOIN p.roles r WHERE r = :rol")
    List<PersonaJpaEntity> findByRol(@Param("rol") Rol rol);

    List<PersonaJpaEntity> findByDepartamentoId(Integer departamentoId);

    boolean existsByEmail(String email);
}
