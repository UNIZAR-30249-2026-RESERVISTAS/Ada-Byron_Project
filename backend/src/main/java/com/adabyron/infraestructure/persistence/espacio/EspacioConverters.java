package com.adabyron.infraestructure.persistence.espacio;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.UUID;

import com.adabyron.domain.espacio.Asignacion;
import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.espacio.HorarioDisponible;
import com.adabyron.domain.persona.DepartamentoId;
import com.adabyron.domain.persona.PersonaId;

public class EspacioConverters {

    /**
     * Convierte una entidad JPA a objeto de dominio.
     * Si las horas de apertura/cierre son null, el horarioEspecifico será null
     * y el espacio usará el horario del edificio por defecto (REQ-C5).
     */
    public static Espacio toDomain(EspacioJpaEntity entity) {
        // Reconstruimos HorarioDisponible solo si ambas horas están presentes
        HorarioDisponible horarioEspecifico = null;
        if (entity.getHoraApertura() != null && entity.getHoraCierre() != null) {
            horarioEspecifico = new HorarioDisponible(
                entity.getHoraApertura(),
                entity.getHoraCierre()
            );
        }

        Categoria categoria = Categoria.desdeNombre(entity.getCategoria());
        Asignacion asignacion = reconstruirAsignacion(entity, categoria);

        return new Espacio(
            new EspacioId(entity.getId()),
            entity.getNumOcupantes(),
            categoria,
            entity.getTamanyo(),
            entity.isReservable(),
            horarioEspecifico,
            asignacion
        );
    }

    /**
     * Convierte un objeto de dominio a entidad JPA.
     * Si el espacio no tiene horario específico (usa el del edificio),
     * las columnas hora_apertura y hora_cierre serán null.
     */
    public static EspacioJpaEntity toEntity(Espacio domain) {
        EspacioJpaEntity entity = new EspacioJpaEntity();
        entity.setId(domain.getId().id());
        entity.setCategoria(domain.getCategoria().getNombre());
        entity.setNumOcupantes(domain.getNumOcupantes());
        entity.setTamanyo(domain.getTamanyo());
        entity.setReservable(domain.isReservable());

        // Mapear horario específico si existe
        if (domain.tieneHorarioEspecifico()) {
            HorarioDisponible horario = domain.getHorarioDisponible();
            entity.setHoraApertura(horario.horaApertura());
            entity.setHoraCierre(horario.horaCierre());
        } else {
            entity.setHoraApertura(null);
            entity.setHoraCierre(null);
        }

        Asignacion asignacion = domain.getAsignacion();
        entity.setTipoAsignacion(asignacion.tipo().name());
        entity.setDepartamentoAsignadoId(
            asignacion.getDepartamentoId() != null ? asignacion.getDepartamentoId().valor() : null
        );

        if (asignacion.esPersonas()) {
            String personasAsig = asignacion.getPersonaIds().stream()
                .map(pid -> pid.valor().toString())
                .sorted()
                .collect(Collectors.joining(","));
            entity.setPersonaAsignadaIds(personasAsig);
        } else {
            entity.setPersonaAsignadaIds(null);
        }

        return entity;
    }

    /**
     * Reconstruye la asignación a partir de los campos de la entidad JPA.
     */
    private static Asignacion reconstruirAsignacion(EspacioJpaEntity entity, Categoria categoria) {
        String tipo = entity.getTipoAsignacion();

        // Si el tipo de asignación no está presente (datos antiguos), usamos la lógica de la fucnion auxiliar legacy basada en la categoría.
        if (tipo == null || tipo.isBlank()) {
            return asignacionLegacy(categoria);
        }

        return switch (tipo) {
            case "EINA" -> Asignacion.eina();
            case "DEPARTAMENTO" -> {
                Integer depto = entity.getDepartamentoAsignadoId();
                if (depto == null) {
                    throw new IllegalStateException("DEPARTAMENTO sin departamento_asignado_id en espacio " + entity.getId());
                }
                yield Asignacion.departamento(new DepartamentoId(depto));
            }
            case "PERSONAS" -> {
                String personasAsig = entity.getPersonaAsignadaIds();
                if (personasAsig == null || personasAsig.isBlank()) {
                    throw new IllegalStateException("PERSONAS sin persona_asignada_ids en espacio " + entity.getId());
                }
                Set<PersonaId> personas = Arrays.stream(personasAsig.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .map(UUID::fromString)
                    .map(PersonaId::new)
                    .collect(Collectors.toSet());

                if (personas.isEmpty()) {
                    throw new IllegalStateException("PERSONAS vacío en espacio " + entity.getId());
                }
                yield Asignacion.personas(personas);
            }
            default -> throw new IllegalStateException("tipo_asignacion desconocido: " + tipo + " en espacio " + entity.getId());
        };
    }

    /**
     * Este metodo los usamos de momento de manera temporal, puesto que inicialmente no teníamos el campo tipo_asignacion en la base de datos,
     *  y por tanto no podíamos distinguir entre los distintos tipos de asignación. 
     * Por eso, mientras no hayamos migrado todos los datos a la nueva estructura, este método asigna una asignación por defecto basada en la categoría del espacio.
     */
    private static Asignacion asignacionLegacy(Categoria categoria) {
        return switch (categoria.getId().valor()) {
            case 1, 2, 3, 5 -> Asignacion.eina();
            case 4 -> Asignacion.departamento(DepartamentoId.INFORMATICA_INGENIERIA_SISTEMAS);
            default -> throw new IllegalStateException("Categoría no soportada");
        };
    }
}
