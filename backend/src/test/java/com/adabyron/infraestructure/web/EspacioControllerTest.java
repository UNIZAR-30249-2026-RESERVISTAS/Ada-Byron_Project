package com.adabyron.infraestructure.web;

import com.adabyron.application.espacio.CambiarCategoriaDTO;
import com.adabyron.application.espacio.CambiarReservableDTO;
import com.adabyron.application.espacio.EspacioService;
import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioFactory;
import com.adabyron.domain.espacio.EspacioId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(EspacioController.class)
@WithMockUser
public class EspacioControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EspacioService espacioService;

    @Autowired
    private ObjectMapper objectMapper;

    private Espacio espacio;
    private EspacioId id;

    @BeforeEach
    void setUp(){
        //Espacio de la segunda planta, máximo número de ocupantes 20, laboratorio, 20.3 de área y reservable creado
        espacio = EspacioFactory.crearNuevoEspacio("2", 20, 3, 20.3, true);
        id = espacio.getId();
    }

    @Test
    void buscarPorId_Retorna200_SiExiste() throws Exception{
        when(espacioService.obtenerDetalles(id.toString())).thenReturn(espacio);
        mockMvc.perform(get("/api/espacios/" + id))
                .andExpect(status().isOk());
    }

    @Test
    void cambiaEstado_Retorna200_SiSeCambiaOK() throws Exception{
        CambiarReservableDTO dto = new CambiarReservableDTO(false);
        Espacio espacioActualizado = EspacioFactory.crearNuevoEspacio("2", 20, 3, 20.3, dto.reservable());
        when(espacioService.cambiarReservable(id.toString(), dto.reservable())).thenReturn(espacioActualizado);
        mockMvc.perform(put("/api/espacios/" + id + "/reservable")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isOk());
    }

    @Test
    void cambiaCategoria_Retorna200_SiSeCambiaOK() throws Exception{
        CambiarCategoriaDTO dto = new CambiarCategoriaDTO("Seminario");
        Espacio espacioActualizado = EspacioFactory.crearNuevoEspacio("2", 20, 2, 20.3, true);
        when(espacioService.cambiarCategoria(id.toString(), Categoria.desdeNombre(dto.categoria()))).thenReturn(espacioActualizado);
        mockMvc.perform(put("/api/espacios/" + id + "/categoria")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                        .andExpect(status().isOk());
    }
}
