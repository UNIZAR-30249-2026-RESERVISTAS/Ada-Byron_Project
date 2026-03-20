// backend/src/test/java/com/adabyron/infraestructure/web/PersonaControllerTest.java

package com.adabyron.infraestructure.web;

import com.adabyron.application.persona.CambiarRolDTO;
import com.adabyron.application.persona.CrearPersonaDTO;
import com.adabyron.application.persona.PersonaService;
import com.adabyron.domain.persona.DepartamentoId;
import com.adabyron.domain.persona.Persona;
import com.adabyron.domain.persona.PersonaFactory;
import com.adabyron.domain.persona.Rol;
import com.adabyron.domain.persona.exception.PersonaNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(PersonaController.class)
@WithMockUser
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonaService personaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Persona personaEstudiante;
    private Persona personaDocente;
    private UUID estudianteId;
    private UUID docenteId;

    // Password hash de prueba (hash BCrypt de "password123")
    private static final String TEST_PASSWORD_HASH = "$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqKj3fB9lVBrPfIEBGDrY1f2QmQoK";

    @BeforeEach
    void setUp() {
        personaEstudiante = PersonaFactory.crearNuevaPersona(
                "Ana García", "ana.garcia@example.com", TEST_PASSWORD_HASH, Rol.ESTUDIANTE, null
        );
        estudianteId = personaEstudiante.getId();

        personaDocente = PersonaFactory.crearNuevaPersona(
                "Dr. House", "house@hospital.com", TEST_PASSWORD_HASH, Rol.DOCENTE_INVESTIGADOR, new DepartamentoId(1)
        );
        docenteId = personaDocente.getId();
    }

    @Test
    void crearPersona_Retorna201yDatos_CuandoEsValida() throws Exception {
        CrearPersonaDTO dto = new CrearPersonaDTO("Ana García", "ana.garcia@example.com", "password123", "ESTUDIANTE", null);
        when(personaService.crearPersona(any(CrearPersonaDTO.class))).thenReturn(personaEstudiante);

        mockMvc.perform(post("/api/personas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ana García"))
                .andExpect(jsonPath("$.email").value("ana.garcia@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("ESTUDIANTE"));
    }

    @Test
    void listarTodas_Retorna200yLista() throws Exception {
        when(personaService.listarTodas()).thenReturn(List.of(personaEstudiante, personaDocente));

        mockMvc.perform(get("/api/personas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Ana García"));
    }

    @Test
    void buscarPorId_Retorna200_SiExiste() throws Exception {
        when(personaService.buscarPorId(estudianteId)).thenReturn(personaEstudiante);

        mockMvc.perform(get("/api/personas/" + estudianteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana García"));
    }

    @Test
    void buscarPorId_Retorna404_SiNoExiste() throws Exception {
        UUID idInválido = UUID.randomUUID();
        when(personaService.buscarPorId(idInválido)).thenThrow(new PersonaNotFoundException(idInválido));

        mockMvc.perform(get("/api/personas/" + idInválido))
                .andExpect(status().isNotFound());
    }

    @Test
    void cambiarRol_Retorna200_SiSeCambiaOK() throws Exception {
        CambiarRolDTO dto = new CambiarRolDTO("CONSERJE", null);
        Persona personaActualizada = PersonaFactory.crearNuevaPersona("Ana García", "ana.garcia@example.com", TEST_PASSWORD_HASH, Rol.CONSERJE, null);

        when(personaService.cambiarRol(eq(estudianteId), any(CambiarRolDTO.class))).thenReturn(personaActualizada);

        mockMvc.perform(put("/api/personas/" + estudianteId + "/rol")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("CONSERJE"));
    }

    @Test
    void añadirGerente_Retorna200_CuandoEsPosible() throws Exception {
        personaDocente.añadirRolGerente();
        when(personaService.añadirRolGerente(docenteId)).thenReturn(personaDocente);

        mockMvc.perform(put("/api/personas/" + docenteId + "/gerente")
                .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void eliminarPersona_Retorna204_SiExiste() throws Exception {
        doNothing().when(personaService).eliminar(estudianteId);

        mockMvc.perform(delete("/api/personas/" + estudianteId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(personaService, times(1)).eliminar(estudianteId);
    }
}