package com.adabyron.infraestructure.persistence.espacio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


public interface SpringDataEspacioRepository extends JpaRepository<EspacioJpaEntity, String> {

    List<EspacioJpaEntity> findByCategoria(String categoria);
    List<EspacioJpaEntity> findByNumOcupantes(int numOcupantes);

    @Query("""
        SELECT e FROM EspacioJpaEntity e
                WHERE NOT EXISTS (
                    SELECT 1 FROM ReservaJpaEntity r
                    JOIN r.espacioIds id
                    WHERE id = e.id
                    AND r.estado IN ('CONFIRMADA', 'POTENCIALMENTE_INVALIDA')
                    AND r.fechaInicio < :fin
                    AND r.fechaFin > :inicio
                )
    """)
    List<EspacioJpaEntity> findDisponibles(
        @Param("inicio") LocalDateTime inicio,
        @Param("fin") LocalDateTime fin
    );

    @Query("SELECT e FROM EspacioJpaEntity e WHERE e.categoria = :categoria AND NOT EXISTS (" +
            "SELECT 1 FROM ReservaJpaEntity r JOIN r.espacioIds re " +
            "WHERE re = e.id AND r.estado IN ('CONFIRMADA', 'POTENCIALMENTE_INVALIDA') " +
            "AND r.fechaInicio < :fin AND r.fechaFin > :inicio)")
    List<EspacioJpaEntity> findDisponiblesByCategoria(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin,
            @Param("categoria") String categoria);
}
