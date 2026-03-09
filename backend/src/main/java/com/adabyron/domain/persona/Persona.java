package com.adabyron.domain.persona;

import com.adabyron.domain.persona.exception.*;
import com.adabyron.domain.shared.AggregateRoot;

import java.util.HashSet;
import java.util.Set;

/**
 * AGGREGATE ROOT - Persona
 *
 *
 * Invariantes que esta clase debe garantizar siempre:
 *  INV-1: Una persona tiene al menos 1 rol y como máximo 2.
 *  INV-2: Si tiene 2 roles, deben ser GERENTE + DOCENTE_INVESTIGADOR.
 *  INV-3: Si algún rol requiere departamento, debe tener departamentoId.
 *  INV-4: Si ningún rol requiere departamento, departamentoId es null.
 *  INV-5: email nunca es nulo ni vacío.
 *  INV-6: nombre nunca es nulo ni vacío.
 */
public class Persona extends AggregateRoot {
    // ID de la persona, generado automáticamente al crear una nueva persona.
    private final PersonaId id;

    // Datos de una persona, serán String con validación inline en el constructor.
    private  String nombre; // INV-6
    private  String email; // INV-5

    // Roles y adscripción del departamento
    private Set<Rol> roles; // INV-1, INV-2
    private DepartamentoId departamentoId; // INV-3, INV-4

    private Persona(PersonaId id, String nombre, String email) {
        this.id = id;
        this.nombre = validarNombre(nombre);
        this.email = validarEmail(email);
        this.roles = new HashSet<>();
    }
    /**
     * Método de fábrica para crear una nueva persona.
     * Este método se encarga de validar los datos de entrada, asignar un ID único
     * y garantizar que se asigna al menos un rol y un departamento al crear la persona, ya que son requisitos obligatorios.
     */
    public static Persona crearNuevaPersona(String nombre, String email, Rol rol, DepartamentoId departamentoId) {
        Persona p = new Persona(PersonaId.generarNuevoId(), nombre, email);
        p.roles.add(rol); // Asignamos el rol al crear la persona, ya que es obligatorio tener al menos un rol.
        p.validarRolDepartamento(rol, departamentoId); // Validamos que el rol sea compatible con el departamento.
        p.departamentoId = departamentoId; // Asignamos el departamento al crear la persona, ya que es obligatorio tener un departamento.
        return p;
    }

    /**
     * Método de fábrica para reconstruir una persona a partir de sus datos almacenados en la base de datos.
     */
    public static Persona reconstruirPersona(PersonaId id, String nombre, String email, Set<Rol> roles, DepartamentoId departamentoId) {
        Persona p = new Persona(id, nombre, email);
        p.roles =new HashSet<>(roles); // Asignamos los roles al reconstruir la persona, ya que es obligatorio tener al menos un rol.
        p.departamentoId = departamentoId;
        return p;
    }

    // Métodos para gestionar los roles de la persona, garantizando las invariantes INV-1 e INV-2

    /**
     * Método para cambiar el rol de la persona
     * Este método se encarga de validar que el nuevo rol sea compatible con el departamento asignado,
     * y de garantizar que si la persona tenía el rol de GERENTE y el nuevo rol no es compatible con GERENTE,
     * se retire el rol de GERENTE para mantener la coherencia de los roles.
     */
    public void cambiarRol (Rol nuevoRol, DepartamentoId nuevoDepartamento) {
        Rol rolAnterior = rolPrincipal();

        // Validamos la coherencia del nuevo rol con el departamento (INV-3, INV-4)
        validarRolDepartamento(nuevoRol, nuevoDepartamento);

        // Si tenía GERENTE y el nuevo rol no es compatible, retiramos GERENTE
        if (tieneRol(Rol.GERENTE) && !nuevoRol.esCompatibleCon(Rol.GERENTE)) {
            roles.remove(Rol.GERENTE);
        }

        roles.remove(rolAnterior);
        roles.add(nuevoRol);
        this.departamentoId = nuevoDepartamento;

        // registerEvent(new RolCambiado(this.id, rolAnterior, nuevoRol));
    }

    /**
     * Añadir el rol de GERENTE a una persona que ya tiene el rol de DOCENTE_INVESTIGADOR
     */
    public void añadirRolGerente() {
        if (!tieneRol(Rol.DOCENTE_INVESTIGADOR))
            throw new RolIncompatibleException(Rol.GERENTE, rolPrincipal());
        if (tieneRol(Rol.GERENTE))
            return; // idempotente
        roles.add(Rol.GERENTE);
        //registerEvent(new GerenciaAsignada(this.id));
    }

    public void quitarRolGerente() {
        if (!tieneRol(Rol.GERENTE))
            return; // idempotente
        roles.remove(Rol.GERENTE);
        // registerEvent(new GerenciaRetirada(this.id));
    }

    // Métodos auxiliares para gestionar los roles
    public boolean tieneRol(Rol rol) {
        return roles.contains(rol);
    }

    /**
     * El rol principal de una persona es siempre el que no es GERENTE, ya que GERENTE es un rol adicional que puede coexistir con DOCENTE_INVESTIGADOR.
     * INV-1 garantiza que siempre existe exactamente un rol principal
     */
    public Rol rolPrincipal() {
        return roles.stream()
                .filter(r -> r != Rol.GERENTE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("La persona debe tener al menos un rol"));
    }


    // **************************
    // Métodos para validaciones inline de los datos de la persona
    // **************************

    /**
     * Método encargado de validar el nombre de la persona. INV-6
     * El nombre no puede estar vacío, ni ser nulo, ni tener menos de 2 caracteres, ni más de 100 caracteres.
     */
    public String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        String trimmed = nombre.strip();
        if (trimmed.length() < 2)
            throw new IllegalArgumentException("El nombre debe tener al menos 2 caracteres");
        if (trimmed.length() > 100)
            throw new IllegalArgumentException("El nombre no puede superar 100 caracteres");
        return trimmed;
    }

    /**
     * Método encargado de validar el email de la persona. INV-5
     * El email no puede estar vacío, ni ser nulo, ni tener menos de 5 caracteres,
     * ni más de 100 caracteres, y debe tener un formato válido.
     */
    public String validarEmail(String email) {
        if (email == null || email.isBlank())
            throw new IllegalArgumentException("El email no puede estar vacío");
        String trimmed = email.strip();
        if (trimmed.length() < 5)
            throw new IllegalArgumentException("El email debe tener al menos 5 caracteres");
        if (trimmed.length() > 100)
            throw new IllegalArgumentException("El email no puede superar 100 caracteres");
        String normalizado = email.strip().toLowerCase();
        if (!normalizado.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-z]{2,}$"))
            throw new IllegalArgumentException("Email con formato inválido: " + normalizado);
        return normalizado;
    }

    /**
     * Método encargado de validar que el rol asignado a la persona sea compatible con el departamento asignado.
     * INV-3 e INV-4
     */
    public void validarRolDepartamento(Rol rol, DepartamentoId departamento) {
        if (rol.requiereDepartamento() && departamento == null)
                throw new DepartamentoRequeridoException(rol); // Por implementar la excepción
        if (!rol.requiereDepartamento() && departamento != null)
                throw new DepartamentoNoPermitidoException(rol); // Por implementar la excepción
    }

    // **************************
    // Getters y setters necesarios para la persistencia y la gestión de personas
    // **************************

    // De momento vacío para no poner sentido
}
