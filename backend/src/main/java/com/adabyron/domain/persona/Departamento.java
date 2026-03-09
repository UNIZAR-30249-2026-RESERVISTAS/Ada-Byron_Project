package com.adabyron.domain.persona;


/** Entidad - Departamento Universitario (REQ-B3)
 *
 * El departamento es una entidad inmutable, por lo que no vamos a exponer
 * los métodos setters. Los dos departamentos son creados al iniciar
 * el sistema, y nunca son modificados.
 */
public class Departamento {

    private final DepartamentoId id;
    private final String nombre;
    private final String codigoSIGEUZ;

    public Departamento(DepartamentoId id, String nombre, String codigoSIGEUZ) {
        this.id = id;
        this.nombre = nombre;
        this.codigoSIGEUZ = codigoSIGEUZ;
    }

    /**
     * Creamos las dos únicas instancias que pueden existir de Departamento, y las exponemos como constantes públicas.
     * Son inmutables e idénticas durante toda la vida del sistema.
     */
    public static final Departamento INFORMATICA_INGENIERIA_SISTEMAS = new Departamento(DepartamentoId.INFORMATICA_INGENIERIA_SISTEMAS, "Informática e Ingeniería de Sistemas", "IIS");

    public static final Departamento INGENIERIA_ELECTRONICA_COMUNICACIONES = new Departamento(DepartamentoId.INGENIERIA_ELECTRONICA_COMUNICACIONES, "Ingeniería Electrónica y Comunicaciones", "IEC");


    public DepartamentoId getId() { return id;}
    public String getNombre() { return nombre; }
    public String getCodigoSIGEUZ() { return codigoSIGEUZ; }

    /**
     * Las entidades se comparan por su identidad (ID), no por su valor. 
     * Por eso, el método equals solo compara los IDs de los departamentos, no sus nombres o códigos.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Departamento d)) return false;
        return this.id.equals(d.id);
    } 

    @Override
    public String toString() { return nombre; }

}
