package com.adabyron.infraestructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartamentoController.class)
@WithMockUser
class DepartamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    // GET /api/departamentos — listarTodos
    @Test
    void listarTodos_Retorna200yLosDosDeptos() throws Exception {

        List<Map<String, Object>> departamentos = List.of(
                Map.of(
                        "id", 1,
                        "nombre", "Informática e Ingeniería de Sistemas",
                        "codigoSIGEUZ", "IIS"
                ),
                Map.of(
                        "id", 2,
                        "nombre", "Ingeniería Electrónica y Comunicaciones",
                        "codigoSIGEUZ", "IEC"
                )
        );

        when(rabbitTemplate.convertSendAndReceive(
                eq("departamento.listar"),
                eq("LISTAR")
        )).thenReturn(departamentos);

        mockMvc.perform(get("/api/departamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Informática e Ingeniería de Sistemas"))
                .andExpect(jsonPath("$[0].codigoSIGEUZ").value("IIS"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nombre").value("Ingeniería Electrónica y Comunicaciones"))
                .andExpect(jsonPath("$[1].codigoSIGEUZ").value("IEC"));
    }

    @Test
    void listarTodos_Retorna200yOrdenCorrecto() throws Exception {

        List<Map<String, Object>> departamentos = List.of(
                Map.of(
                        "id", 1,
                        "nombre", "Informática e Ingeniería de Sistemas",
                        "codigoSIGEUZ", "IIS"
                ),
                Map.of(
                        "id", 2,
                        "nombre", "Ingeniería Electrónica y Comunicaciones",
                        "codigoSIGEUZ", "IEC"
                )
        );

        when(rabbitTemplate.convertSendAndReceive(
                eq("departamento.listar"),
                eq("LISTAR")
        )).thenReturn(departamentos);

        mockMvc.perform(get("/api/departamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    // GET /api/departamentos/{id}
    @Test
    void buscarPorId_Retorna200_SiEsIIS() throws Exception {

        Map<String, Object> dto = Map.of(
                "id", 1,
                "nombre", "Informática e Ingeniería de Sistemas",
                "codigoSIGEUZ", "IIS"
        );

        when(rabbitTemplate.convertSendAndReceive(
                eq("departamento.buscar.porId"),
                eq(1)
        )).thenReturn(dto);

        mockMvc.perform(get("/api/departamentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Informática e Ingeniería de Sistemas"))
                .andExpect(jsonPath("$.codigoSIGEUZ").value("IIS"));
    }

    @Test
    void buscarPorId_Retorna200_SiEsIEC() throws Exception {

        Map<String, Object> dto = Map.of(
                "id", 2,
                "nombre", "Ingeniería Electrónica y Comunicaciones",
                "codigoSIGEUZ", "IEC"
        );

        when(rabbitTemplate.convertSendAndReceive(
                eq("departamento.buscar.porId"),
                eq(2)
        )).thenReturn(dto);

        mockMvc.perform(get("/api/departamentos/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nombre").value("Ingeniería Electrónica y Comunicaciones"))
                .andExpect(jsonPath("$.codigoSIGEUZ").value("IEC"));
    }

    @Test
    void buscarPorId_Retorna400_SiIdNoExisteEnElSistema() throws Exception {

        when(rabbitTemplate.convertSendAndReceive(
                eq("departamento.buscar.porId"),
                eq(3)
        )).thenThrow(new IllegalArgumentException("Departamento inválido"));

        mockMvc.perform(get("/api/departamentos/3"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void buscarPorId_Retorna400_SiIdEsNegativo() throws Exception {

        when(rabbitTemplate.convertSendAndReceive(
                eq("departamento.buscar.porId"),
                eq(-1)
        )).thenThrow(new IllegalArgumentException("Departamento inválido"));

        mockMvc.perform(get("/api/departamentos/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void buscarPorId_Retorna400_SiIdEsCero() throws Exception {

        when(rabbitTemplate.convertSendAndReceive(
                eq("departamento.buscar.porId"),
                eq(0)
        )).thenThrow(new IllegalArgumentException("Departamento inválido"));

        mockMvc.perform(get("/api/departamentos/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}