package com.adabyron.infraestructure.persistence.espacio;

import com.adabyron.domain.espacio.Asignacion;
import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.espacio.HorarioDisponible;
import com.adabyron.domain.persona.DepartamentoId;
import com.adabyron.domain.persona.PersonaId;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class EspacioConvertersTest {

    @Test
    void toDomain_DeberiaReconstruirHorarioEspecifico() {

        EspacioJpaEntity entity = new EspacioJpaEntity();

        entity.setId("001");
        entity.setCategoria("Laboratorio");
        entity.setNumOcupantes(20);
        entity.setTamanyo(30.0);
        entity.setReservable(true);

        entity.setHoraApertura(LocalTime.of(8,0));
        entity.setHoraCierre(LocalTime.of(20,0));

        entity.setTipoAsignacion("EINA");

        Espacio espacio = EspacioConverters.toDomain(entity);

        assertTrue(espacio.tieneHorarioEspecifico());

        assertEquals(
                LocalTime.of(8,0),
                espacio.getHorarioDisponible().horaApertura()
        );
    }

    @Test
    void toDomain_SinHorario_DeberiaUsarHorarioPorDefecto() {

        EspacioJpaEntity entity = new EspacioJpaEntity();

        entity.setId("001");
        entity.setCategoria("Aula");
        entity.setNumOcupantes(10);
        entity.setTamanyo(20.0);
        entity.setReservable(true);

        entity.setHoraApertura(null);
        entity.setHoraCierre(null);

        entity.setTipoAsignacion("EINA");

        Espacio espacio = EspacioConverters.toDomain(entity);

        assertFalse(espacio.tieneHorarioEspecifico());
    }

    @Test
    void toEntity_DeberiaMapearHorarioEspecifico() {

        HorarioDisponible horario = new HorarioDisponible(
                LocalTime.of(9,0),
                LocalTime.of(18,0)
        );

        Espacio espacio = new Espacio(
                new EspacioId("001"),
                20,
                Categoria.desdeNombre("Laboratorio"),
                50.0,
                true,
                horario,
                Asignacion.eina()
        );

        EspacioJpaEntity entity = EspacioConverters.toEntity(espacio);

        assertEquals(LocalTime.of(9,0), entity.getHoraApertura());
        assertEquals(LocalTime.of(18,0), entity.getHoraCierre());
    }

    @Test
    void toEntity_SinHorarioEspecifico_DeberiaGuardarNull() {

        Espacio espacio = new Espacio(
                new EspacioId("001"),
                20,
                Categoria.desdeNombre("Aula"),
                50.0,
                true,
                null,
                Asignacion.eina()
        );

        EspacioJpaEntity entity = EspacioConverters.toEntity(espacio);

        assertNull(entity.getHoraApertura());
        assertNull(entity.getHoraCierre());
    }

    @Test
    void toDomain_DeberiaReconstruirAsignacionDepartamento() {

        EspacioJpaEntity entity = new EspacioJpaEntity();

        entity.setId("001");
        entity.setCategoria("Despacho");
        entity.setNumOcupantes(5);
        entity.setTamanyo(20.0);
        entity.setReservable(true);

        entity.setTipoAsignacion("DEPARTAMENTO");
        entity.setDepartamentoAsignadoId(2);

        Espacio espacio = EspacioConverters.toDomain(entity);

        assertTrue(espacio.getAsignacion().esDepartamento());

        assertEquals(
                2,
                espacio.getAsignacion()
                        .getDepartamentoId()
                        .valor()
        );
    }

    @Test
    void toDomain_DeberiaReconstruirAsignacionPersonas() {

        UUID uuid = UUID.randomUUID();

        EspacioJpaEntity entity = new EspacioJpaEntity();

        entity.setId("001");
        entity.setCategoria("Despacho");
        entity.setNumOcupantes(5);
        entity.setTamanyo(20.0);
        entity.setReservable(true);

        entity.setTipoAsignacion("PERSONAS");
        entity.setPersonaAsignadaIds(uuid.toString());

        Espacio espacio = EspacioConverters.toDomain(entity);

        assertTrue(espacio.getAsignacion().esPersonas());

        assertEquals(
                1,
                espacio.getAsignacion().getPersonaIds().size()
        );
    }

    @Test
    void toEntity_DeberiaMapearPersonasComoString() {

        PersonaId personaId =
                new PersonaId(UUID.randomUUID());

        Espacio espacio = new Espacio(
                new EspacioId("001"),
                5,
                Categoria.desdeNombre("Despacho"),
                20.0,
                true,
                null,
                Asignacion.personas(Set.of(personaId))
        );

        EspacioJpaEntity entity = EspacioConverters.toEntity(espacio);

        assertEquals(
                personaId.valor().toString(),
                entity.getPersonaAsignadaIds()
        );
    }

    @Test
    void toDomain_DeberiaUsarAsignacionLegacy() {

        EspacioJpaEntity entity = new EspacioJpaEntity();

        entity.setId("001");
        entity.setCategoria("Aula");
        entity.setNumOcupantes(10);
        entity.setTamanyo(20.0);
        entity.setReservable(true);

        entity.setTipoAsignacion(null);

        Espacio espacio = EspacioConverters.toDomain(entity);

        assertTrue(
                espacio.getAsignacion().esEina()
        );
    }

    @Test
    void toDomain_DeberiaLanzarExcepcionSiDepartamentoNull() {

        EspacioJpaEntity entity = new EspacioJpaEntity();

        entity.setId("001");
        entity.setCategoria("Despacho");

        entity.setTipoAsignacion("DEPARTAMENTO");
        entity.setDepartamentoAsignadoId(null);

        assertThrows(
                IllegalStateException.class,
                () -> EspacioConverters.toDomain(entity)
        );
    }

    @Test
    void toDomain_DeberiaLanzarExcepcionSiPersonasVacio() {

        EspacioJpaEntity entity = new EspacioJpaEntity();

        entity.setId("001");
        entity.setCategoria("Despacho");

        entity.setTipoAsignacion("PERSONAS");
        entity.setPersonaAsignadaIds("");

        assertThrows(
                IllegalStateException.class,
                () -> EspacioConverters.toDomain(entity)
        );
    }

    @Test
    void toDomain_DeberiaLanzarExcepcionSiTipoDesconocido() {

        EspacioJpaEntity entity = new EspacioJpaEntity();

        entity.setId("001");
        entity.setCategoria("Aula");

        entity.setTipoAsignacion("OTRO");

        assertThrows(
                IllegalStateException.class,
                () -> EspacioConverters.toDomain(entity)
        );
    }
}