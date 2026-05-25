package com.adabyron.infraestructure.web;

import com.adabyron.application.espacio.CambiarCategoriaCommand;
import com.adabyron.application.espacio.CambiarCategoriaDTO;
import com.adabyron.application.espacio.CambiarEstadoCommand;
import com.adabyron.application.espacio.CambiarReservableDTO;
import com.adabyron.application.espacio.EspacioDTO;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioFactory;
import com.adabyron.domain.espacio.EspacioId;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EspacioController.class)
@WithMockUser
public class EspacioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Espacio espacio;
    private EspacioId id;

    @BeforeEach
    void setUp() {

        espacio = EspacioFactory.crearNuevoEspacio(
                "2",
                20,
                3,
                20.3,
                true
        );

        id = espacio.getId();
    }

    @Test
    void buscarPorId_Retorna200_SiExiste() throws Exception {

        when(rabbitTemplate.convertSendAndReceive(
                "espacio.buscar.porId",
                id.toString()
        )).thenReturn(EspacioDTO.fromEntity(espacio));

        mockMvc.perform(get("/api/espacios/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void cambiaEstado_Retorna200_SiSeCambiaOK() throws Exception {

        CambiarReservableDTO dto =
                new CambiarReservableDTO(false);

        Espacio espacioActualizado =
                EspacioFactory.crearNuevoEspacio(
                        "2",
                        20,
                        3,
                        20.3,
                        false
                );

        when(rabbitTemplate.convertSendAndReceive(
                any(String.class),
                any(CambiarEstadoCommand.class)
        )).thenReturn(EspacioDTO.fromEntity(espacioActualizado));

        mockMvc.perform(
                put("/api/espacios/" + id + "/reservable")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isOk());
    }

    @Test
    void cambiaCategoria_Retorna200_SiSeCambiaOK() throws Exception {

        CambiarCategoriaDTO dto =
                new CambiarCategoriaDTO("Seminario");

        Espacio espacioActualizado =
                EspacioFactory.crearNuevoEspacio(
                        "2",
                        20,
                        2,
                        20.3,
                        true
                );

        when(rabbitTemplate.convertSendAndReceive(
                any(String.class),
                any(CambiarCategoriaCommand.class)
        )).thenReturn(EspacioDTO.fromEntity(espacioActualizado));

        mockMvc.perform(
                put("/api/espacios/" + id + "/categoria")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
        ).andExpect(status().isOk());
    }
}