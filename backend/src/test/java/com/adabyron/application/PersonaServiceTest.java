package com.adabyron.application.persona;

import com.adabyron.domain.espacio.EspacioRepository;
import com.adabyron.domain.persona.*;
import com.adabyron.domain.reserva.ReservaRepository;
import com.adabyron.infraestructure.mail.MailService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.adabyron.domain.persona.exception.PersonaNotFoundException;

import com.adabyron.domain.reserva.Reserva;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;
import java.util.List;

import com.adabyron.domain.reserva.IntervaloTemporal;
import com.adabyron.domain.reserva.EstadoReserva;
import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.Asignacion;

import java.time.LocalDateTime;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonaServiceTest {

    @Mock
    private PersonaRepository personaRepository;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private EspacioRepository espacioRepository;

    @Mock
    private MailService mailService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PersonaService personaService;

    @Test
    void deberiaCrearPersonaCorrectamente() {

        CrearPersonaDTO dto = new CrearPersonaDTO(
                "Jorge",
                "jorge@test.com",
                "password123",
                "ESTUDIANTE",
                null
        );

        Persona persona = mock(Persona.class);

        when(personaRepository.existsByEmail(dto.email()))
                .thenReturn(false);

        when(passwordEncoder.encode(dto.password()))
                .thenReturn("HASH");

        try (MockedStatic<PersonaFactory> mockedFactory =
                     mockStatic(PersonaFactory.class)) {

            mockedFactory.when(() ->
                            PersonaFactory.crearNuevaPersona(
                                    anyString(),
                                    anyString(),
                                    anyString(),
                                    any(),
                                    any()))
                    .thenReturn(persona);

            when(personaRepository.save(persona))
                    .thenReturn(persona);

            Persona resultado = personaService.crearPersona(dto);

            verify(personaRepository)
                    .save(persona);

            assertEquals(persona, resultado);
        }
    }

    @Test
    void deberiaLanzarExcepcionSiEmailExiste() {

        CrearPersonaDTO dto = new CrearPersonaDTO(
                "Jorge",
                "jorge@test.com",
                "password123",
                "ESTUDIANTE",
                null
        );

        when(personaRepository.existsByEmail(dto.email()))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> personaService.crearPersona(dto)
        );
    }

    @Test
    void deberiaLanzarExcepcionSiPasswordEsVacia() {

        CrearPersonaDTO dto = new CrearPersonaDTO(
                "Jorge",
                "jorge@test.com",
                "",
                "ESTUDIANTE",
                null
        );

        when(personaRepository.existsByEmail(dto.email()))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> personaService.crearPersona(dto)
        );
    }

    @Test
    void deberiaBuscarPersonaPorId() {

        UUID personaId = UUID.randomUUID();

        Persona persona = mock(Persona.class);

        when(personaRepository.findById(personaId))
                .thenReturn(Optional.of(persona));

        Persona resultado = personaService.buscarPorId(personaId);

        assertEquals(persona, resultado);
    }

    @Test
    void deberiaCambiarRol() {

        UUID personaId = UUID.randomUUID();

        Persona persona = mock(Persona.class);

        CambiarRolDTO dto = new CambiarRolDTO(
                "GERENTE",
                null
        );

        when(personaRepository.findById(personaId))
                .thenReturn(Optional.of(persona));

        when(personaRepository.save(persona))
                .thenReturn(persona);

        Persona resultado = personaService.cambiarRol(personaId, dto);

        verify(persona)
                .cambiarRol(eq(Rol.GERENTE), isNull());

        verify(personaRepository)
                .save(persona);

        assertEquals(persona, resultado);
    }

    @Test
    void deberiaAñadirRolGerente() {

        UUID personaId = UUID.randomUUID();

        Persona persona = mock(Persona.class);

        when(personaRepository.findById(personaId))
                .thenReturn(Optional.of(persona));

        when(personaRepository.save(persona))
                .thenReturn(persona);

        personaService.añadirRolGerente(personaId);

        verify(persona)
                .añadirRolGerente();

        verify(personaRepository)
                .save(persona);
    }

    @Test
    void deberiaQuitarRolGerente() {

        UUID personaId = UUID.randomUUID();

        Persona persona = mock(Persona.class);

        when(personaRepository.findById(personaId))
                .thenReturn(Optional.of(persona));

        when(personaRepository.save(persona))
                .thenReturn(persona);

        personaService.quitarRolGerente(personaId);

        verify(persona)
                .quitarRolGerente();

        verify(personaRepository)
                .save(persona);
    }

    @Test
    void deberiaEliminarPersona() {

        UUID personaId = UUID.randomUUID();

        when(personaRepository.existsById(personaId))
                .thenReturn(true);

        personaService.eliminar(personaId);

        verify(personaRepository)
                .deleteById(personaId);
    }

    @Test
    void deberiaBuscarPersonaPorEmail() {

        String email = "test@test.com";
        Persona persona = mock(Persona.class);

        when(personaRepository.findByEmail(email))
                .thenReturn(Optional.of(persona));

        Persona resultado = personaService.buscarPorEmail(email);

        assertEquals(persona, resultado);

        verify(personaRepository).findByEmail(email);
    }

    @Test
    void deberiaLanzarExcepcionSiEmailNoExiste() {

        String email = "noexiste@test.com";

        when(personaRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                PersonaNotFoundException.class,
                () -> personaService.buscarPorEmail(email)
        );
    }

    @Test
    void deberiaNoCambiarDepartamentoSiEsIgual() {

        UUID id = UUID.randomUUID();

        Persona persona = mock(Persona.class);
        DepartamentoId depto = new DepartamentoId(1);

        CambiarDepartamentoDTO dto = new CambiarDepartamentoDTO(1);

        when(personaRepository.findById(id))
                .thenReturn(Optional.of(persona));

        when(persona.getDepartamentoId())
                .thenReturn(depto);

        Persona resultado = personaService.cambiarDepartamento(id, dto);

        verify(personaRepository, never()).save(any());
        verify(persona, never()).cambiarDepartamento(any());

        assertEquals(persona, resultado);
    }

    @Test
    void deberiaCambiarDepartamentoCorrectamente() {

        UUID id = UUID.randomUUID();

        Persona persona = mock(Persona.class);

        PersonaId personaIdMock = new PersonaId(UUID.randomUUID());

        CambiarDepartamentoDTO dto = new CambiarDepartamentoDTO(2);

        when(persona.getPersonaId()).thenReturn(personaIdMock);

        when(personaRepository.findById(id))
                .thenReturn(Optional.of(persona));

        when(persona.getDepartamentoId())
                .thenReturn(new DepartamentoId(1));

        when(personaRepository.save(persona))
                .thenReturn(persona);

        Persona resultado = personaService.cambiarDepartamento(id, dto);

        verify(persona).cambiarDepartamento(any(DepartamentoId.class));
        verify(personaRepository).save(persona);

        assertEquals(persona, resultado);
    }

    @Test
    void deberiaMarcarReservaComoPotencialmenteInvalidaAlCambiarDepartamento() {

        UUID personaId = UUID.randomUUID();

        Persona persona = mock(Persona.class);

        CambiarDepartamentoDTO dto = new CambiarDepartamentoDTO(1);

        // IMPORTANTE: usar PersonaId real
        PersonaId personaIdReal = new PersonaId(personaId);

        when(personaRepository.findById(personaId))
                .thenReturn(Optional.of(persona));

        when(persona.getDepartamentoId())
                .thenReturn(new DepartamentoId(2));

        when(persona.getPersonaId())
                .thenReturn(personaIdReal);

        // ===== RESERVA =====
        Reserva reserva = mock(Reserva.class);

        EspacioId espacioId = new EspacioId("001");

        when(reservaRepository.findByReservadaPorId(any()))
                .thenReturn(List.of(reserva));

        when(reserva.getEstado())
                .thenReturn(EstadoReserva.CONFIRMADA);

        IntervaloTemporal intervalo = mock(IntervaloTemporal.class);
        when(reserva.getIntervalo())
                .thenReturn(intervalo);
        when(intervalo.fechaFin())
                .thenReturn(LocalDateTime.now().plusDays(1));

        when(reserva.getEspacioIds())
                .thenReturn(List.of(espacioId));

        // ===== ESPACIO =====
        Espacio espacio = mock(Espacio.class);
        Categoria categoria = mock(Categoria.class);

        when(espacioRepository.findById(espacioId))
                .thenReturn(Optional.of(espacio));

        when(espacio.getCategoria())
                .thenReturn(categoria);

        when(categoria.getNombre())
                .thenReturn("Laboratorio");

        Asignacion asignacion = mock(Asignacion.class);

        when(espacio.getAsignacion())
                .thenReturn(asignacion);

        when(asignacion.esDepartamento())
                .thenReturn(true);

        when(asignacion.getDepartamentoId())
                .thenReturn(new DepartamentoId(2)); // distinto al nuevoDept (1 vs 2)

        // ===== ACT =====
        personaService.cambiarDepartamento(personaId, dto);

        // ===== VERIFY =====
        verify(reserva).marcarComoPotencialmenteInvalida(anyString());
        verify(reservaRepository).save(reserva);
    }
}