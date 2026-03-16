package com.adabyron.infraestructure.persistence.espacio;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface SpringDataEspacioRepository extends JpaRepository<EspacioJpaEntity, String> {

    List<EspacioJpaEntity> findByCategoria(String categoria);
    List<EspacioJpaEntity> findByNumOcupantes(int numOcupantes);
}
