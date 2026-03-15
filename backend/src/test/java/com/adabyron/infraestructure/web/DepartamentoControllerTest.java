package com.adabyron.infraestructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartamentoController.class)
class DepartamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarTodos_Retorna200yLosDosDeptos() throws Exception {
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
    void buscarPorId_Retorna200_SiEsIIS() throws Exception {
        mockMvc.perform(get("/api/departamentos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Informática e Ingeniería de Sistemas"))
                .andExpect(jsonPath("$.codigoSIGEUZ").value("IIS"));
    }

    @Test
    void buscarPorId_Retorna200_SiEsIEC() throws Exception {
        mockMvc.perform(get("/api/departamentos/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nombre").value("Ingeniería Electrónica y Comunicaciones"))
                .andExpect(jsonPath("$.codigoSIGEUZ").value("IEC"));
    }

    @Test
    void buscarPorId_Retorna400_SiIdInvalido() throws Exception {
        mockMvc.perform(get("/api/departamentos/3"))
                .andExpect(status().isBadRequest());
    }
}
