package com.adabyron.infraestructure.persistence.reserva;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Repositorio de persistencia para la entidad Reserva, es necesario extender JpaRepository para aprovechar las funcionalidades de Spring Data JPA
 * Además, se definen métodos personalizados para buscar reservas activas, por espacio, por persona y potencialmente inválidas. Estos métodos personalizados se implementarán automáticamente por Spring Data JPA basándose en el nombre del método y las anotaciones utilizadas.
 */
public interface SpringDataReservaRepository extends JpaRepository<ReservaJpaEntity, UUID> {

    // JpaRepository NO puede deducir esta query por nombre, necesita JPQL explícito
    @Query("SELECT r FROM ReservaJpaEntity r WHERE r.fechaFin > :ahora AND r.estado = 'CONFIRMADA'")
    List<ReservaJpaEntity> findReservasActivas(@Param("ahora") LocalDateTime ahora);

    // Involucra un JOIN con la colección espacioIds, requiere JPQL explícito
    @Query("SELECT r FROM ReservaJpaEntity r JOIN r.espacioIds e WHERE e = :espacioId AND r.estado = 'CONFIRMADA'")
    List<ReservaJpaEntity> findActivasByEspacioId(@Param("espacioId") String espacioId);

    // Spring Data puede deducir este por nombre de método, no necesitamos pasarle @Query
    List<ReservaJpaEntity> findByReservadaPorId(UUID personaId);

    // Involucra dos condiciones, requiere JPQL explícito
    @Query("SELECT r FROM ReservaJpaEntity r WHERE r.estado = 'SOLICITADA' AND r.fechaFin < :ahora")
    List<ReservaJpaEntity> findPotencialmenteInvalidas(@Param("ahora") LocalDateTime ahora);
}
