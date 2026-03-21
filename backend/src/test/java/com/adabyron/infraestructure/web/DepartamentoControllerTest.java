package com.adabyron.infraestructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests del DepartamentoController.
 *
 * Códigos HTTP:
 *   200 → departamento encontrado
 *   400 → ID inválido (no existe ningún departamento con ese ID)
 *         DepartamentoId lanza IllegalArgumentException → GlobalExceptionHandler → 400
 */
@WebMvcTest(DepartamentoController.class)
@WithMockUser
class DepartamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // GET /api/departamentos — listarTodos
    @Test
    void listarTodos_Retorna200yLosDosDeptos() throws Exception {
        mockMvc.perform(get("/api/departamentos"))
                .andExpect(status().isOk())                                        // 200
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
        // El primero siempre es IIS (id=1) y el segundo IEC (id=2)
        // porque TODOS = List.of(INFORMATICA..., INGENIERIA_ELECTRONICA...)
        mockMvc.perform(get("/api/departamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    // GET /api/departamentos/{id} — buscarPorId
    @Test
    void buscarPorId_Retorna200_SiEsIIS() throws Exception {
        mockMvc.perform(get("/api/departamentos/1"))
                .andExpect(status().isOk())                                        // 200
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Informática e Ingeniería de Sistemas"))
                .andExpect(jsonPath("$.codigoSIGEUZ").value("IIS"));
    }

    @Test
    void buscarPorId_Retorna200_SiEsIEC() throws Exception {
        mockMvc.perform(get("/api/departamentos/2"))
                .andExpect(status().isOk())                                        // 200
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nombre").value("Ingeniería Electrónica y Comunicaciones"))
                .andExpect(jsonPath("$.codigoSIGEUZ").value("IEC"));
    }

    @Test
    void buscarPorId_Retorna400_SiIdNoExisteEnElSistema() throws Exception {
        // El ID 3 no existe — DepartamentoId lanza IllegalArgumentException → 400
        // (no es 404 porque en este dominio cualquier ID fuera de {1,2} es directamente inválido)
        mockMvc.perform(get("/api/departamentos/3"))
                .andExpect(status().isBadRequest())                                // 400
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void buscarPorId_Retorna400_SiIdEsNegativo() throws Exception {
        mockMvc.perform(get("/api/departamentos/-1"))
                .andExpect(status().isBadRequest())                                // 400
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void buscarPorId_Retorna400_SiIdEsCero() throws Exception {
        mockMvc.perform(get("/api/departamentos/0"))
                .andExpect(status().isBadRequest())                                // 400
                .andExpect(jsonPath("$.error").exists());
    }
}