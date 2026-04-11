package com.adabyron.infraestructure.persistence.edificio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "edificio_config")
public class EdificioJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @Column(name = "porcentaje_ocupacion_maxima", nullable = false)
    private double porcentajeOcupacionMaxima;

    protected EdificioJpaEntity() {}

    public EdificioJpaEntity(Integer id, double porcentajeOcupacionMaxima) {
        this.id = id;
        this.porcentajeOcupacionMaxima = porcentajeOcupacionMaxima;
    }

    public Integer getId() {
        return id;
    }

    public double getPorcentajeOcupacionMaxima() {
        return porcentajeOcupacionMaxima;
    }

    public void setPorcentajeOcupacionMaxima(double porcentajeOcupacionMaxima) {
        this.porcentajeOcupacionMaxima = porcentajeOcupacionMaxima;
    }
}