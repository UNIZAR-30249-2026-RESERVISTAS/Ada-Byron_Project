package com.adabyron.infraestructure.persistence.edificio;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataEdificioRepository extends JpaRepository<EdificioJpaEntity, Integer> {
}
