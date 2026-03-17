    package com.adabyron.domain.persona;

    import java.util.List;
    import java.util.Optional;
    import java.util.UUID;

    /**
     * Puerto de Salida - interfaz que el dominio expone para que el adaptador
     * de infraestructura pueda implementar.
     */
    public interface PersonaRepository {

        Persona save(Persona persona);

        Optional<Persona> findById(UUID id);

        Optional<Persona> findByEmail(String email);

        List<Persona> findAll();

        List<Persona> findByRol(Rol rol);

        List<Persona> findByDepartamentoId(Integer departamentoId);

        boolean existsByEmail(String email);

        boolean existsById(UUID id);

        void deleteById(UUID id);
    }
