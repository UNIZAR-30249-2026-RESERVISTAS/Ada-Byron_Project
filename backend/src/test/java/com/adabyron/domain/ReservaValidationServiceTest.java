package com.adabyron.domain.service;

import com.adabyron.domain.espacio.*;
import com.adabyron.domain.persona.*;
import com.adabyron.domain.reserva.IntervaloTemporal;
import com.adabyron.domain.reserva.Reserva;
import com.adabyron.domain.reserva.exception.ReservaInvalidaException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservaValidacionServiceTest {

    private PersonaRepository personaRepository;

    private ReservaValidacionService service;

    @BeforeEach
    void setUp() {
        personaRepository = mock(PersonaRepository.class);
        service = new ReservaValidacionService(personaRepository);
    }

    @Test
    void gerentePuedeReservarCualquierEspacio() {

        Persona gerente = mock(Persona.class);

        when(gerente.tieneRol(Rol.GERENTE))
                .thenReturn(true);

        Espacio espacio = mock(Espacio.class);

        mockHorarioValido(espacio);

        when(espacio.getNumOcupantes())
                .thenReturn(20);

        when(espacio.getId())
                .thenReturn(new EspacioId("001"));

        service.validar(
                gerente,
                espacio,
                5,
                crearIntervaloValido(),
                1.0,
                List.of(),
                null
        );

        assertTrue(true);
    }

    @Test
    void deberiaFallarSiHorarioFueraDeRango() {

        Persona persona = mock(Persona.class);

        Espacio espacio = mock(Espacio.class);

        HorarioDisponible horario = new HorarioDisponible(
                LocalTime.of(8, 0),
                LocalTime.of(20, 0)
        );

        when(espacio.getHorarioDisponible())
                .thenReturn(horario);

        when(espacio.tieneHorarioEspecifico())
                .thenReturn(true);

        IntervaloTemporal intervalo = mock(IntervaloTemporal.class);

        when(intervalo.fecha())
                .thenReturn(LocalDateTime.now().toLocalDate());

        when(intervalo.estaContenidoEn(any(), any()))
                .thenReturn(false);

        when(intervalo.fechaInicio())
                .thenReturn(LocalDateTime.of(
                        2025,
                        1,
                        1,
                        21,
                        0
                ));

        when(intervalo.fechaFin())
                .thenReturn(LocalDateTime.of(
                        2025,
                        1,
                        1,
                        22,
                        0
                ));

        assertThrows(
                ReservaInvalidaException.class,
                () -> service.validar(
                        persona,
                        espacio,
                        2,
                        intervalo,
                        1.0,
                        List.of(),
                        null
                )
        );
    }

    @Test
    void deberiaFallarSiSuperaAforo() {

        Persona persona = mock(Persona.class);

        when(persona.tieneRol(Rol.GERENTE))
                .thenReturn(true);

        Espacio espacio = mock(Espacio.class);

        mockHorarioValido(espacio);

        when(espacio.getNumOcupantes())
                .thenReturn(10);

        assertThrows(
                ReservaInvalidaException.class,
                () -> service.validar(
                        persona,
                        espacio,
                        20,
                        crearIntervaloValido(),
                        0.5,
                        List.of(),
                        null
                )
        );
    }

    @Test
    void deberiaFallarSiHaySolapamiento() {

        Persona persona = mock(Persona.class);

        when(persona.tieneRol(Rol.GERENTE))
                .thenReturn(true);

        Espacio espacio = mock(Espacio.class);

        mockHorarioValido(espacio);

        when(espacio.getNumOcupantes())
                .thenReturn(50);

        when(espacio.getId())
                .thenReturn(new EspacioId("001"));

        Reserva reserva = mock(Reserva.class);

        when(reserva.estaActiva())
                .thenReturn(true);

        when(reserva.seSolapaCon(any()))
                .thenReturn(true);

        assertThrows(
                ReservaInvalidaException.class,
                () -> service.validar(
                        persona,
                        espacio,
                        5,
                        crearIntervaloValido(),
                        1.0,
                        List.of(reserva),
                        null
                )
        );
    }

    @Test
    void deberiaFallarSiLaboratorioEsDeOtroDepartamento() {

        Persona persona = mock(Persona.class);

        when(persona.tieneRol(Rol.GERENTE))
                .thenReturn(false);

        when(persona.tieneRol(Rol.TECNICO_LABORATORIO))
                .thenReturn(true);

        when(persona.getDepartamentoId())
                .thenReturn(new DepartamentoId(1));

        when(persona.rolPrincipal())
                .thenReturn(Rol.TECNICO_LABORATORIO);

        Espacio espacio = mock(Espacio.class);

        mockHorarioValido(espacio);

        when(espacio.getNumOcupantes())
                .thenReturn(20);

        Categoria categoria = mock(Categoria.class);

        when(categoria.getNombre())
                .thenReturn("Laboratorio");

        when(espacio.getCategoria())
                .thenReturn(categoria);

        assertThrows(
                ReservaInvalidaException.class,
                () -> service.validar(
                        persona,
                        espacio,
                        5,
                        crearIntervaloValido(),
                        1.0,
                        List.of(),
                        new DepartamentoId(2)
                )
        );
    }

    @Test
    void deberiaPermitirDespachoVisitanteMismoDepartamento() {

        Persona reservante = mock(Persona.class);

        when(reservante.tieneRol(Rol.GERENTE))
                .thenReturn(false);

        when(reservante.tieneRol(Rol.INVESTIGADOR_CONTRATADO))
                .thenReturn(true);

        when(reservante.getDepartamentoId())
                .thenReturn(new DepartamentoId(1));

        when(reservante.rolPrincipal())
                .thenReturn(Rol.INVESTIGADOR_CONTRATADO);

        Persona visitante = mock(Persona.class);

        when(visitante.tieneRol(Rol.INVESTIGADOR_VISITANTE))
                .thenReturn(true);

        when(visitante.getDepartamentoId())
                .thenReturn(new DepartamentoId(1));

        UUID visitanteUuid = UUID.randomUUID();

        PersonaId visitanteId = new PersonaId(visitanteUuid);

        when(personaRepository.findById(visitanteUuid))
                .thenReturn(Optional.of(visitante));

        Espacio despacho = mock(Espacio.class);

        mockHorarioValido(despacho);

        when(despacho.getNumOcupantes())
                .thenReturn(20);

        Categoria categoria = mock(Categoria.class);

        when(categoria.getNombre())
                .thenReturn("Despacho");

        when(despacho.getCategoria())
                .thenReturn(categoria);

        Asignacion asignacion = mock(Asignacion.class);

        when(asignacion.esPersonas())
                .thenReturn(true);

        when(asignacion.getPersonaIds())
                .thenReturn(Set.of(visitanteId));

        when(despacho.getAsignacion())
                .thenReturn(asignacion);

        service.validar(
                reservante,
                despacho,
                2,
                crearIntervaloValido(),
                1.0,
                List.of(),
                null
        );

        assertTrue(true);
    }

    // =========================
    // HELPERS
    // =========================

    private void mockHorarioValido(Espacio espacio) {

        HorarioDisponible horario = new HorarioDisponible(
                LocalTime.of(8, 0),
                LocalTime.of(21, 0)
        );

        when(espacio.getHorarioDisponible())
                .thenReturn(horario);

        when(espacio.tieneHorarioEspecifico())
                .thenReturn(false);
    }

    private IntervaloTemporal crearIntervaloValido() {

        IntervaloTemporal intervalo = mock(IntervaloTemporal.class);

        when(intervalo.fecha())
                .thenReturn(LocalDateTime.now().toLocalDate());

        when(intervalo.estaContenidoEn(any(), any()))
                .thenReturn(true);

        when(intervalo.fechaInicio())
                .thenReturn(LocalDateTime.now().plusHours(1));

        when(intervalo.fechaFin())
                .thenReturn(LocalDateTime.now().plusHours(2));

        return intervalo;
    }
}