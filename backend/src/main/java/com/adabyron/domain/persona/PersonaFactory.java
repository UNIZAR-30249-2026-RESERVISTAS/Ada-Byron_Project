package com.adabyron.domain.persona;

import java.util.Set;

public class PersonaFactory {


    /**
     * Método de fábrica para crear una nueva persona.
     * Este método se encarga de validar los datos de entrada, asignar un ID único
     * y garantizar que se asigna al menos un rol y un departamento al crear la persona, ya que son requisitos obligatorios.
     */
    public static Persona crearNuevaPersona(String nombre, String email, String passwordHash, Rol rol, DepartamentoId departamentoId) {
        return new Persona(PersonaId.generarNuevoId(), nombre, email, passwordHash, Set.of(rol), departamentoId);
    }

    /**
     * Método de fábrica para reconstruir una persona a partir de sus datos almacenados en la base de datos (rehidrata la persona).
     */
   public static Persona reconstruirPersona(PersonaId id, String nombre, String email, String passwordHash, Set<Rol> roles, DepartamentoId departamentoId) {
        return new Persona(id, nombre, email, passwordHash, roles, departamentoId);
   }
}
