package com.adabyron.infraestructure.persistence.espacio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "espacios")
@Setter @Getter
public class EspacioJpaEntity {
    @Id
    @Column(name = "espacioId", updatable = false, nullable = false)
    private String id;

    @Column(name = "ocupantes", updatable = false, nullable = false)
    private int numOcupantes;

    @Setter
    @Column(name = "categoria", updatable = true, nullable = false)
    private String categoria;

    @Column(name = "reservable", updatable = true, nullable = false)
    private boolean reservable;

    @Column(name = "tamaño", updatable = false, nullable = false)
    private double tamanyo;
}
