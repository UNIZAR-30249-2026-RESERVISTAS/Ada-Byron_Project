package com.adabyron.application.edificio;

import com.adabyron.domain.edificio.EdificioRepository;
import com.adabyron.domain.edificio.PorcentajeOcupacion;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.espacio.EspacioRepository;
import com.adabyron.domain.persona.Persona;
import com.adabyron.domain.persona.PersonaRepository;
import com.adabyron.domain.reserva.EstadoReserva;
import com.adabyron.domain.reserva.Reserva;
import com.adabyron.domain.reserva.ReservaRepository;
import com.adabyron.domain.reserva.ReservaId;
import com.adabyron.infraestructure.mail.MailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EdificioServiceTest {

    @Mock
    private EdificioRepository edificioRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private EspacioRepository espacioRepository;

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private EdificioService edificioService;

    @Test
    void deberiaLanzarExcepcionSiPorcentajeEsNull() {

        CambiarPorcentajeOcupacionDTO dto =
                new CambiarPorcentajeOcupacionDTO(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> edificioService.cambiarPorcentajeOcupacionMaxima(dto)
        );

        assertEquals(
                "El porcentaje de ocupacion es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(reservaRepository);
    }

    @Test
    void deberiaObtenerPorcentajeOcupacionMaxima() {

        PorcentajeOcupacion porcentaje =
                PorcentajeOcupacion.of(0.5);

        when(edificioRepository.obtenerPorcentajeOcupacionMaxima())
                .thenReturn(porcentaje);

        double resultado =
                edificioService.obtenerPorcentajeOcupacionMaxima();

        assertEquals(0.5, resultado);

        verify(edificioRepository)
                .obtenerPorcentajeOcupacionMaxima();
    }

    @Test
    void deberiaCambiarPorcentajeSinInvalidarReservas() {

        CambiarPorcentajeOcupacionDTO dto =
                new CambiarPorcentajeOcupacionDTO(0.8);

        Reserva reserva = mock(Reserva.class);

        when(reservaRepository.findReservasActivas(any(LocalDateTime.class)))
                .thenReturn(List.of(reserva));

        when(reserva.getEstado())
                .thenReturn(EstadoReserva.SOLICITADA);

        double resultado =
                edificioService.cambiarPorcentajeOcupacionMaxima(dto);

        assertEquals(0.8, resultado);

        verify(edificioRepository)
                .guardarPorcentajeOcupacionMaxima(any());

        verify(reservaRepository, never())
                .save(any());
    }

    @Test
    void deberiaMarcarReservaComoInvalidaSiSuperaAforo() {

        CambiarPorcentajeOcupacionDTO dto =
                new CambiarPorcentajeOcupacionDTO(0.5);

        Reserva reserva = mock(Reserva.class);
        Espacio espacio = mock(Espacio.class);
        Persona persona = mock(Persona.class);

        UUID personaId = UUID.randomUUID();
        ReservaId reservaId = mock(ReservaId.class);
        EspacioId espacioId = mock(EspacioId.class);

        when(reservaRepository.findReservasActivas(any(LocalDateTime.class)))
                .thenReturn(List.of(reserva));

        when(reserva.getEstado())
                .thenReturn(EstadoReserva.CONFIRMADA);

        when(reserva.getId())
                .thenReturn(reservaId);

        when(reservaId.toString())
                .thenReturn(UUID.randomUUID().toString());

        when(reserva.getEspacioIds())
                .thenReturn(List.of(espacioId));

        when(espacioRepository.findById(espacioId))
                .thenReturn(Optional.of(espacio));

        when(espacio.getNumOcupantes())
                .thenReturn(10);

        when(reserva.getNumeroAsistentes())
                .thenReturn(8);

        when(reserva.getReservadaPorIdRaw())
                .thenReturn(personaId);

        when(personaRepository.findById(personaId))
                .thenReturn(Optional.of(persona));

        edificioService.cambiarPorcentajeOcupacionMaxima(dto);

        verify(reserva)
                .marcarComoPotencialmenteInvalida(anyString());

        verify(reservaRepository)
                .save(reserva);
    }

    @Test
    void deberiaLanzarExcepcionSiEspacioNoExiste() {

        CambiarPorcentajeOcupacionDTO dto =
                new CambiarPorcentajeOcupacionDTO(0.5);

        Reserva reserva = mock(Reserva.class);
        EspacioId espacioId = mock(EspacioId.class);

        when(reservaRepository.findReservasActivas(any(LocalDateTime.class)))
                .thenReturn(List.of(reserva));

        when(reserva.getEstado())
                .thenReturn(EstadoReserva.CONFIRMADA);

        when(reserva.getEspacioIds())
                .thenReturn(List.of(espacioId));

        when(espacioRepository.findById(espacioId))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalStateException.class,
                () -> edificioService.cambiarPorcentajeOcupacionMaxima(dto)
        );
    }
}