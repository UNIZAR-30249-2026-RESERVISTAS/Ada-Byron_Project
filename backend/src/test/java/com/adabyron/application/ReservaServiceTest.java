package com.adabyron.application.reserva;

import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.espacio.EspacioRepository;
import com.adabyron.domain.persona.Persona;
import com.adabyron.domain.persona.PersonaId;
import com.adabyron.domain.persona.PersonaRepository;
import com.adabyron.domain.persona.Rol;
import com.adabyron.domain.reserva.*;
import com.adabyron.domain.service.ReservaValidacionService;
import com.adabyron.infraestructure.mail.MailService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private EspacioRepository espacioRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private ReservaValidacionService validacionService;

    @Mock
    private MailService servicioEmail;

    @InjectMocks
    private ReservaService reservaService;

    @Test
    void deberiaLanzarExcepcionSiPersonaNoExiste() {

        UUID personaId = UUID.randomUUID();

        CrearReservaDTO dto = mock(CrearReservaDTO.class);

        when(dto.reservadaPorId())
                .thenReturn(personaId);

        when(personaRepository.findById(personaId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.crearReserva(dto)
        );
    }

    @Test
    void deberiaLanzarExcepcionSiEspacioNoExiste() {

        UUID personaUuid = UUID.randomUUID();

        Persona persona = mock(Persona.class);

        when(personaRepository.findById(personaUuid))
                .thenReturn(Optional.of(persona));

        when(espacioRepository.findById(any(EspacioId.class)))
                .thenReturn(Optional.empty());

        CrearReservaDTO dto = new CrearReservaDTO(
                List.of("001"),
                personaUuid,
                TipoUsoReserva.DOCENCIA,
                5,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                60,
                "Detalles"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> reservaService.crearReserva(dto)
        );
    }

    @Test
    void deberiaCrearReservaConfirmada() {

        UUID personaUuid = UUID.randomUUID();

        Persona persona = mock(Persona.class);
        PersonaId personaId = mock(PersonaId.class);

        Espacio espacio = mock(Espacio.class);
        EspacioId espacioId = mock(EspacioId.class);

        Reserva reserva = mock(Reserva.class);

        when(personaRepository.findById(personaUuid))
                .thenReturn(Optional.of(persona));

        when(persona.getPersonaId())
                .thenReturn(personaId);

        when(espacioRepository.findById(any()))
                .thenReturn(Optional.of(espacio));

        when(espacio.getId())
                .thenReturn(espacioId);

        when(reservaRepository.findActivasByEspacioId(any()))
                .thenReturn(List.of());

        CrearReservaDTO dto = new CrearReservaDTO(
                List.of("001"),
                personaUuid,
                TipoUsoReserva.DOCENCIA,
                5,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                60,
                "Detalles"
        );


        try (MockedStatic<ReservaFactory> mockedFactory =
                     mockStatic(ReservaFactory.class)) {

            mockedFactory.when(() ->
                            ReservaFactory.crearNuevaReserva(
                                    any(),
                                    any(),
                                    any(),
                                    anyInt(),
                                    any(),
                                    any()))
                    .thenReturn(reserva);

            when(reservaRepository.save(reserva))
                    .thenReturn(reserva);

            Reserva resultado = reservaService.crearReserva(dto);

            verify(reserva).confirmar();

            verify(reservaRepository)
                    .save(reserva);

            assertEquals(reserva, resultado);
        }
    }

    @Test
    void deberiaRechazarReservaSiValidacionFalla() {

        UUID personaUuid = UUID.randomUUID();

        Persona persona = mock(Persona.class);
        PersonaId personaId = mock(PersonaId.class);

        Espacio espacio = mock(Espacio.class);

        EspacioId espacioId = mock(EspacioId.class);

        when(espacio.getId())
                .thenReturn(espacioId);

        when(espacioId.id())
                .thenReturn("001");

        Reserva reserva = mock(Reserva.class);

        when(personaRepository.findById(personaUuid))
                .thenReturn(Optional.of(persona));

        when(persona.getPersonaId())
                .thenReturn(personaId);

        when(espacioRepository.findById(any()))
                .thenReturn(Optional.of(espacio));

        CrearReservaDTO dto = new CrearReservaDTO(
                List.of("001"),
                personaUuid,
                TipoUsoReserva.DOCENCIA,
                5,
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                60,
                "Detalles"
        );


        doThrow(new IllegalStateException("Aforo excedido"))
                .when(validacionService)
                .validar(any(), any(), anyInt(), any(), anyDouble(), any(), any());

        try (MockedStatic<ReservaFactory> mockedFactory =
                     mockStatic(ReservaFactory.class)) {

            mockedFactory.when(() ->
                            ReservaFactory.crearNuevaReserva(
                                    any(),
                                    any(),
                                    any(),
                                    anyInt(),
                                    any(),
                                    any()))
                    .thenReturn(reserva);

            when(reservaRepository.save(reserva))
                    .thenReturn(reserva);

            reservaService.crearReserva(dto);

            verify(reserva)
                    .rechazar("Aforo excedido");

            verify(reservaRepository)
                    .save(reserva);
        }
    }

    @Test
    void deberiaCancelarReservaYEnviarCorreo() {

        UUID reservaId = UUID.randomUUID();
        UUID personaUuid = UUID.randomUUID();

        Persona persona = mock(Persona.class);
        PersonaId personaId = mock(PersonaId.class);

        Reserva reserva = mock(Reserva.class);

        when(personaRepository.findById(personaUuid))
                .thenReturn(Optional.of(persona));

        when(reservaRepository.findById(reservaId))
                .thenReturn(Optional.of(reserva));

        when(persona.getRoles())
                .thenReturn(Set.of(Rol.GERENTE));

        when(persona.getPersonaId())
                .thenReturn(personaId);

        when(persona.getEmail())
                .thenReturn("test@test.com");

        when(reservaRepository.save(reserva))
                .thenReturn(reserva);

        Reserva resultado = reservaService.cancelarReserva(
                reservaId,
                personaUuid,
                "Motivo"
        );

        verify(reserva)
                .cancelar(any(), any(), eq("Motivo"));

        verify(servicioEmail)
                .enviarCorreoCancelacion(anyString(), anyString());

        verify(reservaRepository)
                .save(reserva);

        assertEquals(reserva, resultado);
    }

    @Test
    void deberiaLanzarExcepcionSiNoTienePermisosParaEliminar() {

        UUID reservaId = UUID.randomUUID();
        UUID personaUuid = UUID.randomUUID();

        Persona persona = mock(Persona.class);
        PersonaId personaId = mock(PersonaId.class);

        Reserva reserva = mock(Reserva.class);
        PersonaId creadorId = mock(PersonaId.class);

        when(personaRepository.findById(personaUuid))
                .thenReturn(Optional.of(persona));

        when(reservaRepository.findById(reservaId))
                .thenReturn(Optional.of(reserva));

        when(persona.getRoles())
                .thenReturn(Set.of(Rol.ESTUDIANTE));

        when(persona.getPersonaId())
                .thenReturn(personaId);

        when(reserva.getReservadaPorId())
                .thenReturn(creadorId);

        assertThrows(
                RuntimeException.class,
                () -> reservaService.eliminarReserva(
                        reservaId,
                        personaUuid)
        );
    }
}