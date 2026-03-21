package com.adabyron.domain.persona;

import com.adabyron.domain.persona.exception.*;

import java.util.*;

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
public class Persona{

    private UUID id;
    private  String nombre; // INV-6
    private  String email; // INV-5
    private String passwordHash; // Hash de la contraseña
    private Set<Rol> roles; // INV-1, INV-2
    private Integer departamentoId; // INV-3, INV-4

    // Constructor sin argumentos requerido por JPA
    protected Persona() {}

    public Persona(PersonaId id, String nombre, String email, String passwordHash, Set<Rol> roles, DepartamentoId departamentoId) {
        this.id = Objects.requireNonNull(id, "El id no puede ser nulo").valor();
        this.nombre = validarNombre(nombre);
        this.email = validarEmail(email);
        this.passwordHash = Objects.requireNonNull(passwordHash, "El hash de contraseña no puede ser nulo");
        this.roles = new HashSet<>(Objects.requireNonNull(roles, "Los roles no pueden ser nulos"));

        validarEstadoInicial(this.roles, departamentoId);
        this.departamentoId = departamentoId != null ? departamentoId.valor() : null;
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
        this.departamentoId = nuevoDepartamento != null ? nuevoDepartamento.valor() : null;
    }

    /**
     * Añade el rol GERENTE a una persona que ya tiene DOCENTE_INVESTIGADOR.
     * Es la única combinación de dos roles permitida (REQ-B2).
     */
    public void añadirRolGerente() {
        if (tieneRol(Rol.GERENTE))
            return; // idempotente — ya es gerente, no hacemos nada

        if (!tieneRol(Rol.DOCENTE_INVESTIGADOR))
            throw new RolIncompatibleException( Rol.GERENTE, rolPrincipal());

        roles.add(Rol.GERENTE);
    }

    public void quitarRolGerente() {
        if (!tieneRol(Rol.GERENTE))
            return; // idempotente
        roles.remove(Rol.GERENTE);
    }

    // Métodos auxiliares para gestionar los roles
    public boolean tieneRol(Rol rol) {
        return roles.contains(rol);
    }

    /**
     * El rol principal de una persona en caso de tener dos roles es el que no es GERENTE, ya que GERENTE se puede añadir como rol adicional a un DOCENTE_INVESTIGADOR.
     * Si solo tiene un rol, ese es el rol principal. 
     */
    public Rol rolPrincipal() {
        if (roles.size() == 1) {
            return roles.iterator().next();
        }
        return roles.stream()
                .filter(r -> r != Rol.GERENTE)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("La persona debe tener al menos un rol"));
    }


    // Métodos para validaciones inline de los datos de la persona

    private void validarEstadoInicial(Set<Rol> roles, DepartamentoId departamentoId) {
        if (roles.isEmpty()) {
            throw new IllegalArgumentException("La persona debe tener al menos un rol");
        }
        if (roles.size() > 2) {
            throw new IllegalArgumentException("La persona no puede tener más de 2 roles");
        }
        if (roles.size() == 2 &&
            !(roles.contains(Rol.GERENTE) && roles.contains(Rol.DOCENTE_INVESTIGADOR))) {
            throw new IllegalArgumentException("La única combinación válida de 2 roles es GERENTE + DOCENTE_INVESTIGADOR");
        }

        Rol rolPrincipal;
        if (roles.size() == 1 && roles.contains(Rol.GERENTE)) {
            rolPrincipal = Rol.GERENTE;
        } else {
            rolPrincipal = roles.stream()
                    .filter(r -> r != Rol.GERENTE)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("La persona debe tener un rol principal."));
        }

        validarRolDepartamento(rolPrincipal, departamentoId);
    }

    private static String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        String trimmed = nombre.strip();
        if (trimmed.length() < 2)
            throw new IllegalArgumentException("El nombre debe tener al menos 2 caracteres");
        if (trimmed.length() > 100)
            throw new IllegalArgumentException("El nombre no puede superar 100 caracteres");
        return trimmed;
    }

    private static String validarEmail(String email) {
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

    private static void validarRolDepartamento(Rol rol, DepartamentoId departamento) {
        if (rol.requiereDepartamento() && departamento == null)
                throw new DepartamentoRequeridoException(rol);
        if (!rol.requiereDepartamento() && departamento != null)
                throw new DepartamentoNoPermitidoException(rol);
    }

    // Getters
    public UUID getId() { return id; }
    public PersonaId getPersonaId() { return new PersonaId(id); }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Set<Rol> getRoles() { return Collections.unmodifiableSet(roles); }

    public DepartamentoId getDepartamentoId() {
        return departamentoId != null ? new DepartamentoId(departamentoId) : null;
    }
}
