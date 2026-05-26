package com.adabyron.application.espacio;

import com.adabyron.domain.espacio.Asignacion;
import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.espacio.EspacioRepository;
import com.adabyron.domain.espacio.HorarioDisponible;
import com.adabyron.domain.espacio.exception.EspacioNotFoundException;
import com.adabyron.domain.persona.Persona;
import com.adabyron.domain.persona.PersonaId;
import com.adabyron.domain.persona.PersonaRepository;
import com.adabyron.domain.persona.Rol;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class EspacioServiceTest {

    @Mock
    private EspacioRepository espacioRepository;

    @Mock
    private PersonaRepository personaRepository;

    @InjectMocks
    private EspacioService espacioService;

    @Test
    void deberiaObtenerDetalles() {

        Espacio espacio = mock(Espacio.class);

        when(espacioRepository.findById(any(EspacioId.class)))
                .thenReturn(Optional.of(espacio));

        Espacio resultado = espacioService.obtenerDetalles("001");

        assertEquals(espacio, resultado);
    }

    @Test
    void deberiaLanzarExcepcionSiEspacioNoExiste() {

        when(espacioRepository.findById(any(EspacioId.class)))
                .thenReturn(Optional.empty());

        assertThrows(
                EspacioNotFoundException.class,
                () -> espacioService.obtenerDetalles("001")
        );
    }

    @Test
    void deberiaCambiarCategoria() {

        Espacio espacio = mock(Espacio.class);
        Categoria categoria = mock(Categoria.class);

        when(espacioRepository.findById(any(EspacioId.class)))
                .thenReturn(Optional.of(espacio));

        when(espacioRepository.save(espacio))
                .thenReturn(espacio);

        Espacio resultado = espacioService.cambiarCategoria("001", categoria);

        verify(espacio).cambiarCategoria(categoria);

        verify(espacioRepository).save(espacio);

        assertEquals(espacio, resultado);
    }

    @Test
    void deberiaCambiarReservable() {

        Espacio espacio = mock(Espacio.class);

        when(espacioRepository.findById(any(EspacioId.class)))
                .thenReturn(Optional.of(espacio));

        when(espacioRepository.save(espacio))
                .thenReturn(espacio);

        Espacio resultado = espacioService.cambiarReservable("001", false);

        verify(espacio).cambiarReservable(false);

        verify(espacioRepository).save(espacio);

        assertEquals(espacio, resultado);
    }

    @Test
    void deberiaCambiarHorario() {

        UUID gerenteId = UUID.randomUUID();

        Persona gerente = mock(Persona.class);
        Espacio espacio = mock(Espacio.class);

        HorarioDisponible horario = new HorarioDisponible(
                LocalTime.of(8, 0),
                LocalTime.of(20, 0)
        );

        when(personaRepository.findById(gerenteId))
                .thenReturn(Optional.of(gerente));

        when(gerente.getRoles())
                .thenReturn(Set.of(Rol.GERENTE));

        when(espacioRepository.findById(any(EspacioId.class)))
                .thenReturn(Optional.of(espacio));

        when(espacioRepository.save(espacio))
                .thenReturn(espacio);

        Espacio resultado = espacioService.cambiarHorario(
                "001",
                horario,
                gerenteId
        );

        verify(espacio)
                .cambiarHorario(horario, Set.of(Rol.GERENTE));

        verify(espacioRepository)
                .save(espacio);

        assertEquals(espacio, resultado);
    }

    @Test
    void deberiaRestablecerHorario() {

        UUID gerenteId = UUID.randomUUID();

        Persona gerente = mock(Persona.class);
        Espacio espacio = mock(Espacio.class);

        when(personaRepository.findById(gerenteId))
                .thenReturn(Optional.of(gerente));

        when(gerente.getRoles())
                .thenReturn(Set.of(Rol.GERENTE));

        when(espacioRepository.findById(any(EspacioId.class)))
                .thenReturn(Optional.of(espacio));

        when(espacioRepository.save(espacio))
                .thenReturn(espacio);

        Espacio resultado = espacioService.restablecerHorario(
                "001",
                gerenteId
        );

        verify(espacio)
                .restablecerHorarioEdificio(Set.of(Rol.GERENTE));

        verify(espacioRepository)
                .save(espacio);

        assertEquals(espacio, resultado);
    }

    @Test
    void deberiaObtenerHorario() {

        Espacio espacio = mock(Espacio.class);

        HorarioDisponible horario = new HorarioDisponible(
                LocalTime.of(8, 0),
                LocalTime.of(21, 0)
        );

        when(espacioRepository.findById(any(EspacioId.class)))
                .thenReturn(Optional.of(espacio));

        when(espacio.getHorarioDisponible())
                .thenReturn(horario);

        HorarioDisponible resultado = espacioService.obtenerHorario("001");

        assertEquals(horario, resultado);
    }

    @Test
    void deberiaAsignarAEina() {

        Espacio espacio = mock(Espacio.class);

        when(espacioRepository.findById(any(EspacioId.class)))
                .thenReturn(Optional.of(espacio));

        when(espacioRepository.save(espacio))
                .thenReturn(espacio);

        Espacio resultado = espacioService.asignarAEina("001");

        verify(espacio)
                .cambiarAsignacion(any(Asignacion.class));

        verify(espacioRepository)
                .save(espacio);

        assertEquals(espacio, resultado);
    }

    @Test
    void deberiaAsignarADepartamento() {

        Espacio espacio = mock(Espacio.class);

        when(espacioRepository.findById(any(EspacioId.class)))
                .thenReturn(Optional.of(espacio));

        when(espacioRepository.save(espacio))
                .thenReturn(espacio);

        Espacio resultado = espacioService.asignarADepartamento(
                "010", // <-- ID válido
                1
        );

        verify(espacio)
                .cambiarAsignacion(any(Asignacion.class));

        verify(espacioRepository)
                .save(espacio);

        assertEquals(espacio, resultado);
    }

    @Test
    void deberiaAsignarAPersonas() {

        UUID personaUuid = UUID.randomUUID();

        Persona persona = mock(Persona.class);

        PersonaId personaId = new PersonaId(UUID.randomUUID());

        Espacio espacio = mock(Espacio.class);

        when(personaRepository.findById(personaUuid))
                .thenReturn(Optional.of(persona));

        when(persona.tieneRol(Rol.INVESTIGADOR_CONTRATADO))
                .thenReturn(true);

        when(persona.getPersonaId())
                .thenReturn(personaId);

        when(espacioRepository.findById(any(EspacioId.class)))
                .thenReturn(Optional.of(espacio));

        when(espacioRepository.save(espacio))
                .thenReturn(espacio);

        Espacio resultado = espacioService.asignarAPersonas(
                "010",
                Set.of(personaUuid)
        );

        verify(espacio)
                .cambiarAsignacion(any(Asignacion.class));

        verify(espacioRepository)
                .save(espacio);

        assertEquals(espacio, resultado);
    }

    @Test
    void deberiaLanzarExcepcionSiPersonaNoTieneRolValido() {

        UUID personaUuid = UUID.randomUUID();

        Persona persona = mock(Persona.class);

        when(personaRepository.findById(personaUuid))
                .thenReturn(Optional.of(persona));

        when(persona.tieneRol(any()))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> espacioService.asignarAPersonas(
                        "001",
                        Set.of(personaUuid)
                )
        );
    }

    @Test
    void deberiaObtenerAsignacion() {

        Espacio espacio = mock(Espacio.class);

        Asignacion asignacion = mock(Asignacion.class);

        when(espacioRepository.findById(any(EspacioId.class)))
                .thenReturn(Optional.of(espacio));

        when(espacio.getAsignacion())
                .thenReturn(asignacion);

        Asignacion resultado = espacioService.obtenerAsignacion("001");

        assertEquals(asignacion, resultado);
    }

    @Test
    void deberiaObtenerIdsPorAforo() {

        List<String> ids = List.of("001", "002");

        when(espacioRepository.findIdsByAforo(20))
                .thenReturn(ids);

        List<String> resultado = espacioService.obtenerIdsPorAforo(20);

        assertEquals(ids, resultado);
    }
}