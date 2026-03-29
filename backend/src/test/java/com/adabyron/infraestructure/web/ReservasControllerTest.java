package com.adabyron.infraestructure.web;

import com.adabyron.application.reserva.CrearReservaDTO;
import com.adabyron.application.reserva.ReservaService;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.persona.PersonaId;
import com.adabyron.domain.persona.exception.PersonaNotFoundException;
import com.adabyron.domain.reserva.*;
import com.adabyron.domain.reserva.exception.ReservaNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservaController.class)
@WithMockUser
public class ReservasControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReservaService reservaService;

    @Autowired
    private ObjectMapper objectMapper;

    private CrearReservaDTO reservaDTO;

    private Reserva reserva1;
    private ReservaId id1;

    private Reserva reserva2;
    private ReservaId id2;

    @BeforeEach
    void setUp() {
        reserva1 = ReservaFactory.crearNuevaReserva(List.of(new EspacioId("101"), new EspacioId("102")),
                new PersonaId(UUID.fromString("00000000-0000-0000-0000-000000001000")),
                TipoUsoReserva.DOCENCIA, 5, IntervaloTemporal.of(LocalDate.of(2026, Month.AUGUST,
                        25), LocalTime.now(), 100), "Test");
        id1 = reserva1.getId();

        reserva2 = ReservaFactory.crearNuevaReserva(List.of(new EspacioId("101"), new EspacioId("102")),
                new PersonaId(UUID.randomUUID()), TipoUsoReserva.DOCENCIA, 5,
                IntervaloTemporal.of(LocalDate.of(2026, Month.JUNE, 25),
                        LocalTime.now(), 100), "Test");
        id2 = reserva2.getId();
    }

    //GET  /api/reservas - Listar reservas activas
    @Test
    void listarTodas_Retorna200yLista_CuandoHayReservas() throws Exception {
        when(reservaService.listarReservasActivas()).thenReturn(List.of(reserva1, reserva2));

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(reserva1.getId().toString()));
    }


    //GET /api/reservas/{id} - Buscar reserva por Id
    @Test
    void buscarPorId_Retorna200_SiExiste() throws Exception {
        when(reservaService.buscarPorId(UUID.fromString(id1.toString()))).thenReturn(reserva1);

        mockMvc.perform(get("/api/reservas/" + reserva1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id1.toString()));
    }

    @Test
    void buscarPorId_Retorna404_SiNoExiste() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(reservaService.buscarPorId(idInexistente)).thenThrow(new ReservaNotFoundException(idInexistente));

        mockMvc.perform(get("/api/reservas/" + idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    //GET /api/reservas/activas/{personaId} - Listar reservas activas de una persona
    @Test
    void listarPorId_Retorna200_SiExiste_CuandoHayReservas() throws Exception {
        UUID personaUuid = UUID.fromString("00000000-0000-0000-0000-000000001000");
        when(reservaService.listarActivasPorPersona(personaUuid)).thenReturn(List.of(reserva1));

        mockMvc.perform(get("/api/reservas/activas/" + personaUuid))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(reserva1.getId().toString()));
    }


    @Test
    void listarPorId_Retorna404_SiNoExiste() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(reservaService.listarActivasPorPersona(idInexistente)).thenThrow(new PersonaNotFoundException(idInexistente));

        mockMvc.perform(get("/api/reservas/activas/"
                        + idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }


    //GET /api/reservas/potencialmente-invalidas - Listar reservas potencialmente inválidas
    @Test
    void listarPotencialmenteInvalidas_Retorna200yLista_CuandoHayReservas() throws Exception {

    }


    //DELETE /api/reservas/{id} - Cancelar una reserva
    @Test
    void cancelar_Retorna200yCancela_SiExiste() throws Exception {
        UUID reservaUuid = UUID.fromString(id1.toString());
        UUID personaUuid = UUID.fromString("00000000-0000-0000-0000-000000001000");
        when(reservaService.cancelarReserva(reservaUuid, personaUuid, "Test")).thenReturn(reserva1);

        mockMvc.perform(delete("/api/reservas/" + reservaUuid + "?solicitanteId=" + personaUuid)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelar_Retorna404_SiNoExistePersona() throws Exception {
        UUID reservaUuid = UUID.fromString(id1.toString());
        UUID idInexistente = UUID.randomUUID();
        when(reservaService.cancelarReserva(eq(reservaUuid), eq(idInexistente), any()))
                .thenThrow(new PersonaNotFoundException(idInexistente));

        mockMvc.perform(delete("/api/reservas/" + reservaUuid + "?solicitanteId=" + idInexistente)
                        .with(csrf()))
                        .andExpect(status().isNotFound());
    }

    @Test
    void cancelar_Retorna404_SiNoExisteReserva() throws Exception {
        UUID personaUuid = UUID.fromString("00000000-0000-0000-0000-000000001000");
        UUID idInexistente = UUID.randomUUID();
        when(reservaService.cancelarReserva(eq(idInexistente), eq(personaUuid), any())).thenThrow(new ReservaNotFoundException(idInexistente));

        mockMvc.perform(delete("/api/reservas/" + idInexistente + "?solicitanteId=" + personaUuid)
                        .with(csrf()))
                        .andExpect(status().isNotFound());
    }


    //DELETE /api/reservas/{id}/permanente - Eliminar permanente una reserva
    @Test
    void eliminarPermanente_Retorna200yElimina_SiExiste() throws Exception {
        UUID reservaUuid = UUID.fromString(id1.toString());
        UUID solicitanteUuid = UUID.fromString("00000000-0000-0000-0000-000000001000");
        doNothing().when(reservaService).eliminarReserva(reservaUuid, solicitanteUuid);

        mockMvc.perform(delete("/api/reservas/" + reservaUuid + "/permanente")
                        .param("solicitanteId", solicitanteUuid.toString())
                        .with(csrf()))
                        .andExpect(status().isNoContent());

        verify(reservaService, times(1)).eliminarReserva(reservaUuid, solicitanteUuid);
    }

    @Test
    void eliminarPermanente_Retorna404_SiNoExistePersona() throws Exception {
        UUID reservaUuid = UUID.fromString(id1.toString());
        UUID idInexistente = UUID.randomUUID();
        doThrow(new PersonaNotFoundException(idInexistente)).when(reservaService).eliminarReserva(reservaUuid, idInexistente);

        mockMvc.perform(delete("/api/reservas/" + reservaUuid + "/permanente")
                        .param("solicitanteId", idInexistente.toString())
                        .with(csrf()))
                        .andExpect(status().isNotFound());

        verify(reservaService, times(1)).eliminarReserva(reservaUuid, idInexistente);
    }

    @Test
    void eliminarPermanente_Retorna404_SiNoExisteReserva() throws Exception {
        UUID solicitanteUuid = UUID.fromString("00000000-0000-0000-0000-000000001000");
        UUID idInexistente = UUID.randomUUID();
        doThrow(new ReservaNotFoundException(idInexistente)).when(reservaService).eliminarReserva(idInexistente, solicitanteUuid);

        mockMvc.perform(delete("/api/reservas/" + idInexistente + "/permanente")
                        .param("solicitanteId", solicitanteUuid.toString())
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(reservaService, times(1)).eliminarReserva(idInexistente, solicitanteUuid);
    }

}
