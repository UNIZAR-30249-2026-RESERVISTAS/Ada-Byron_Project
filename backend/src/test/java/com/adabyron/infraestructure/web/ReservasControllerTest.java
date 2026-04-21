package com.adabyron.infraestructure.web;

import com.adabyron.application.reserva.*;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.persona.PersonaId;
import com.adabyron.domain.persona.exception.PersonaNotFoundException;
import com.adabyron.domain.reserva.*;
import com.adabyron.domain.reserva.exception.ReservaNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.mock.web.MockHttpSession;
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
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    private Reserva reserva1;
    private ReservaId id1;

    private Reserva reserva2;
    private ReservaId id2;

    private MockHttpSession sessionGerente;
    private MockHttpSession sessionUsuarioNormal;

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

        sessionGerente = new MockHttpSession();
        sessionGerente.setAttribute("personaId", UUID.fromString("00000000-0000-0000-0000-000000001000"));
        sessionGerente.setAttribute("roles", List.of("GERENTE"));

        sessionUsuarioNormal = new MockHttpSession();
        sessionUsuarioNormal.setAttribute("personaId", UUID.randomUUID());
        sessionUsuarioNormal.setAttribute("roles", List.of("ESTUDIANTE"));

    }

    //GET  /api/reservas - Listar reservas activas
    @Test
    void listarTodas_Retorna200yLista_CuandoHayReservas() throws Exception {
        when(rabbitTemplate.convertSendAndReceiveAsType(
                eq("reserva.listar.activas"),
                eq("Listar"),
                any(ParameterizedTypeReference.class)
        )).thenReturn(List.of(ReservaDTO.fromEntity(reserva1), ReservaDTO.fromEntity(reserva2)));

        mockMvc.perform(get("/api/reservas").session(sessionGerente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(reserva1.getId().toString()));
    }


    //GET /api/reservas/{id} - Buscar reserva por Id
    @Test
    void buscarPorId_Retorna200_SiExiste() throws Exception {
        when(rabbitTemplate.convertSendAndReceive(eq("reserva.buscar.porId"), eq(id1)))
                .thenReturn(ReservaDTO.fromEntity(reserva1));

        mockMvc.perform(get("/api/reservas/" + id1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id1.toString()));
    }

    @Test
    void buscarPorId_Retorna404_SiNoExiste() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(rabbitTemplate.convertSendAndReceive(eq("reserva.buscar.porId"), eq(idInexistente)))
                .thenThrow(new ReservaNotFoundException(idInexistente));

        mockMvc.perform(get("/api/reservas/" + idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    //GET /api/reservas/activas/{personaId} - Listar reservas activas de una persona
    @Test
    void listarPorId_Retorna200_SiExiste_CuandoHayReservas() throws Exception {
        UUID personaUuid = UUID.fromString("00000000-0000-0000-0000-000000001000");

        when(rabbitTemplate.convertSendAndReceiveAsType(
                eq("reserva.listar.activas.porPersona"),
                eq(personaUuid),
                any(ParameterizedTypeReference.class)
        )).thenReturn(List.of(ReservaDTO.fromEntity(reserva1)));

        mockMvc.perform(get("/api/reservas/activas/" + personaUuid).session(sessionGerente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(reserva1.getId().toString()));
    }


    @Test
    void listarPorId_Retorna404_SiNoExiste() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(rabbitTemplate.convertSendAndReceiveAsType(
                eq("reserva.listar.activas.porPersona"),
                eq(idInexistente),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new PersonaNotFoundException(idInexistente));

        mockMvc.perform(get("/api/reservas/activas/" + idInexistente).session(sessionGerente))
                .andExpect(status().isNotFound());
    }


    //GET /api/reservas/potencialmente-invalidas - Listar reservas potencialmente inválidas
    @Test
    void listarPotencialmenteInvalidas_Retorna200yLista_CuandoHayReservas() throws Exception {

    }


    //DELETE /api/reservas/{id} - Cancelar una reserva
    @Test
    void cancelar_Retorna204yCancela_SiExiste() throws Exception {
        UUID reservaUuid = reserva1.getIdRaw();
        UUID personaUuid = UUID.fromString("00000000-0000-0000-0000-000000001000");

        CancelarReservaCommand comando = new CancelarReservaCommand(reservaUuid, personaUuid, "Test");

        when(rabbitTemplate.convertSendAndReceive(eq("reserva.cancelar"), eq(comando)))
                .thenReturn("OK");

        mockMvc.perform(delete("/api/reservas/" + reservaUuid + "?solicitanteId=" + personaUuid + "&motivo=Test")
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void cancelar_Retorna404_SiNoExistePersona() throws Exception {
        /*
        UUID reservaUuid = UUID.fromString(id1.toString());
        UUID idInexistente = UUID.randomUUID();
        when(reservaService.cancelarReserva(eq(reservaUuid), eq(idInexistente), any()))
                .thenThrow(new PersonaNotFoundException(idInexistente));

        mockMvc.perform(delete("/api/reservas/" + reservaUuid + "?solicitanteId=" + idInexistente)
                        .with(csrf()))
                        .andExpect(status().isNotFound());
         */
    }


    @Test
    void cancelar_Retorna404_SiNoExisteReserva() throws Exception {
        UUID reservaUuid = reserva1.getIdRaw();
        UUID idInexistente = UUID.randomUUID();

        CancelarReservaCommand comando = new CancelarReservaCommand(reservaUuid, idInexistente, null);

        when(rabbitTemplate.convertSendAndReceive(eq("reserva.cancelar"), eq(comando)))
                .thenThrow(new PersonaNotFoundException(idInexistente));

        mockMvc.perform(delete("/api/reservas/" + reservaUuid + "?solicitanteId=" + idInexistente)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }


    //DELETE /api/reservas/{id}/permanente - Eliminar permanente una reserva
    @Test
    void eliminarPermanente_Retorna200yElimina_SiExiste() throws Exception {
        UUID reservaUuid = reserva1.getIdRaw();
        UUID solicitanteUuid = UUID.fromString("00000000-0000-0000-0000-000000001000");

        EliminarReservaCommand comando = new EliminarReservaCommand(reservaUuid, solicitanteUuid);

        when(rabbitTemplate.convertSendAndReceive(eq("reserva.eliminar"), eq(comando)))
                .thenReturn("OK");

        mockMvc.perform(delete("/api/reservas/" + reservaUuid + "/permanente")
                        .param("solicitanteId", solicitanteUuid.toString())
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // Ya no podemos hacer verify() sobre reservaService porque el controlador no lo llama.
        // Nos basta con verificar que el RabbitTemplate recibió el comando correcto.
        verify(rabbitTemplate, times(1)).convertSendAndReceive(eq("reserva.eliminar"), eq(comando));
    }

    @Test
    void eliminarPermanente_Retorna404_SiNoExistePersona() throws Exception {
        /*
        UUID reservaUuid = UUID.fromString(id1.toString());
        UUID idInexistente = UUID.randomUUID();
        doThrow(new PersonaNotFoundException(idInexistente)).when(reservaService).eliminarReserva(reservaUuid, idInexistente);

        mockMvc.perform(delete("/api/reservas/" + reservaUuid + "/permanente")
                        .param("solicitanteId", idInexistente.toString())
                        .with(csrf()))
                        .andExpect(status().isNotFound());

        verify(reservaService, times(1)).eliminarReserva(reservaUuid, idInexistente);
             */
    }



    @Test
    void eliminarPermanente_Retorna404_SiNoExisteReserva() throws Exception {
        /*
        UUID solicitanteUuid = UUID.fromString("00000000-0000-0000-0000-000000001000");
        UUID idInexistente = UUID.randomUUID();
        doThrow(new ReservaNotFoundException(idInexistente)).when(reservaService).eliminarReserva(idInexistente, solicitanteUuid);

        mockMvc.perform(delete("/api/reservas/" + idInexistente + "/permanente")
                        .param("solicitanteId", solicitanteUuid.toString())
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(reservaService, times(1)).eliminarReserva(idInexistente, solicitanteUuid);
        */
    }


}
