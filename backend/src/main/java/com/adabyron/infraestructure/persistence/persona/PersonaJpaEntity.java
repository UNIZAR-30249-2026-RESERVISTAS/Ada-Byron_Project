package com.adabyron.infraestructure.persistence.persona;

import com.adabyron.domain.persona.Rol;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "personas")
public class PersonaJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "persona_roles", joinColumns = @JoinColumn(name = "persona_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private Set<Rol> roles;

    @Column(name = "departamento_id")
    private Integer departamentoId;

    // Constructor sin args (JPA)
    protected PersonaJpaEntity() {}

    // Constructor con parámetros
    public PersonaJpaEntity(UUID id, String nombre, String email, String passwordHash, Set<Rol> roles, Integer departamentoId) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.passwordHash = passwordHash;
        this.roles = roles;
        this.departamentoId = departamentoId;
    }

    // Solo Getters
    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Set<Rol> getRoles() { return roles; }
    public Integer getDepartamentoId() { return departamentoId; }
}
