package com.adabyron.infraestructure.web;

import com.adabyron.application.persona.CambiarRolDTO;
import com.adabyron.application.persona.CrearPersonaDTO;
import com.adabyron.application.persona.PersonaDTO;
import com.adabyron.domain.persona.DepartamentoId;
import com.adabyron.domain.persona.Persona;
import com.adabyron.domain.persona.PersonaFactory;
import com.adabyron.domain.persona.Rol;
import com.adabyron.domain.persona.exception.DepartamentoRequeridoException;
import com.adabyron.domain.persona.exception.PersonaNotFoundException;
import com.adabyron.domain.persona.exception.RolIncompatibleException;
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
import org.springframework.core.ParameterizedTypeReference;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import org.mockito.ArgumentMatchers;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PersonaController.class)
@WithMockUser(roles = "ADMIN")
class PersonaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private Persona personaEstudiante;
    private Persona personaDocente;

    private UUID estudianteId;
    private UUID docenteId;

    private static final String TEST_PASSWORD_HASH =
            "$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqKj3fB9lVBrPfIEBGDrY1f2QmQoK";

    @BeforeEach
    void setUp() {

        personaEstudiante = PersonaFactory.crearNuevaPersona(
                "Ana García",
                "ana.garcia@example.com",
                TEST_PASSWORD_HASH,
                Rol.ESTUDIANTE,
                null
        );

        estudianteId = personaEstudiante.getId();

        personaDocente = PersonaFactory.crearNuevaPersona(
                "Dr. House",
                "house@hospital.com",
                TEST_PASSWORD_HASH,
                Rol.DOCENTE_INVESTIGADOR,
                new DepartamentoId(1)
        );

        docenteId = personaDocente.getId();
    }

    // POST /api/personas

    @Test
    void crearPersona_Retorna201yDatos_CuandoEsValida() throws Exception {

        CrearPersonaDTO dto = new CrearPersonaDTO(
                "Ana García",
                "ana.garcia@example.com",
                "password123",
                "ESTUDIANTE",
                null
        );

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenReturn(PersonaDTO.fromEntity(personaEstudiante));

        mockMvc.perform(post("/api/personas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ana García"))
                .andExpect(jsonPath("$.email").value("ana.garcia@example.com"));
    }

    @Test
    void crearPersona_Retorna409_CuandoEmailYaExiste() throws Exception {

        CrearPersonaDTO dto = new CrearPersonaDTO(
                "Ana García",
                "ana.garcia@example.com",
                "password123",
                "ESTUDIANTE",
                null
        );

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenThrow(new IllegalArgumentException("Ya existe una persona"));

        mockMvc.perform(post("/api/personas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void crearPersona_Retorna400_CuandoNombreEsVacio() throws Exception {

        CrearPersonaDTO dto = new CrearPersonaDTO(
                "",
                "ana.garcia@example.com",
                "password123",
                "ESTUDIANTE",
                null
        );

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenThrow(new IllegalArgumentException("Nombre vacío"));

        mockMvc.perform(post("/api/personas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearPersona_Retorna400_CuandoPasswordEsNula() throws Exception {

        CrearPersonaDTO dto = new CrearPersonaDTO(
                "Ana García",
                "ana.garcia@example.com",
                null,
                "ESTUDIANTE",
                null
        );

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenThrow(new IllegalArgumentException("Password nula"));

        mockMvc.perform(post("/api/personas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearPersona_Retorna400_CuandoRolRequiereDepartamentoYNoSeProporciona() throws Exception {
        CrearPersonaDTO dto = new CrearPersonaDTO(
                "Dr. House",
                "house@hospital.com",
                "password123",
                "DOCENTE_INVESTIGADOR",
                null
        );

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenThrow(new DepartamentoRequeridoException(Rol.DOCENTE_INVESTIGADOR));

        mockMvc.perform(post("/api/personas")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }



    // GET /api/personas
    @Test
    void listarTodas_Retorna200yLista_CuandoHayPersonas() throws Exception {

        when(rabbitTemplate.convertSendAndReceiveAsType(
                anyString(),
                any(Object.class),
                ArgumentMatchers.<ParameterizedTypeReference<List<PersonaDTO>>>any()
        )).thenReturn(List.of(
                PersonaDTO.fromEntity(personaEstudiante),
                PersonaDTO.fromEntity(personaDocente)
        ));

        mockMvc.perform(get("/api/personas")
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listarTodas_Retorna200yListaVacia_CuandoNoHayPersonas() throws Exception {

        when(rabbitTemplate.convertSendAndReceiveAsType(
                anyString(),
                any(Object.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/personas")
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // GET /api/personas/{id}

    @Test
    void buscarPorId_Retorna200_SiExiste() throws Exception {

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenReturn(PersonaDTO.fromEntity(personaEstudiante));

        mockMvc.perform(get("/api/personas/" + estudianteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana García"));
    }

    @Test
    void buscarPorId_Retorna404_SiNoExiste() throws Exception {

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenThrow(new PersonaNotFoundException(estudianteId));

        mockMvc.perform(get("/api/personas/" + estudianteId))
                .andExpect(status().isNotFound());
    }

    // TODO: Fallan de aquí en adelante

    // PUT /rol
    @Test
    void cambiarRol_Retorna200_SiSeCambiaOK() throws Exception {

        CambiarRolDTO dto = new CambiarRolDTO("CONSERJE", null);

        Persona actualizada = PersonaFactory.crearNuevaPersona(
                "Ana García",
                "ana.garcia@example.com",
                TEST_PASSWORD_HASH,
                Rol.CONSERJE,
                null
        );

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenReturn(PersonaDTO.fromEntity(actualizada));

        mockMvc.perform(put("/api/personas/" + estudianteId + "/rol")
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void cambiarRol_Retorna404_SiPersonaNoExiste() throws Exception {

        CambiarRolDTO dto = new CambiarRolDTO("CONSERJE", null);

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenThrow(new PersonaNotFoundException(estudianteId));

        mockMvc.perform(put("/api/personas/" + estudianteId + "/rol")
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void cambiarRol_Retorna400_CuandoRolEsInvalido() throws Exception {

        CambiarRolDTO dto = new CambiarRolDTO("INVALIDO", null);

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenThrow(new IllegalArgumentException("Rol inválido"));

        mockMvc.perform(put("/api/personas/" + estudianteId + "/rol")
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambiarRol_Retorna400_CuandoNuevoRolRequiereDepartamentoYNoSeProporciona() throws Exception {

        CambiarRolDTO dto = new CambiarRolDTO("DOCENTE_INVESTIGADOR", null);

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenThrow(new DepartamentoRequeridoException(Rol.DOCENTE_INVESTIGADOR));

        mockMvc.perform(put("/api/personas/" + estudianteId + "/rol")
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // gerente

    @Test
    void añadirGerente_Retorna200_CuandoEsPosible() throws Exception {

        personaDocente.añadirRolGerente();

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenReturn(PersonaDTO.fromEntity(personaDocente));

        mockMvc.perform(put("/api/personas/" + docenteId + "/gerente")
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE"))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void añadirGerente_Retorna404_SiPersonaNoExiste() throws Exception {

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenThrow(new PersonaNotFoundException(docenteId));

        mockMvc.perform(put("/api/personas/" + docenteId + "/gerente")
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE"))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void añadirGerente_Retorna409_SiPersonaNoEsDocenteInvestigador() throws Exception {

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenThrow(new RolIncompatibleException("Rol incompatible"));

        mockMvc.perform(put("/api/personas/" + estudianteId + "/gerente")
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE"))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    void quitarGerente_Retorna200_CuandoEsPosible() throws Exception {

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenReturn(PersonaDTO.fromEntity(personaDocente));

        mockMvc.perform(delete("/api/personas/" + docenteId + "/gerente")
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE"))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void quitarGerente_Retorna404_SiPersonaNoExiste() throws Exception {

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenThrow(new PersonaNotFoundException(docenteId));

        mockMvc.perform(delete("/api/personas/" + docenteId + "/gerente")
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE"))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // DELETE persona

    @Test
    void eliminarPersona_Retorna204_SiExiste() throws Exception {

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenReturn("OK");

        mockMvc.perform(delete("/api/personas/" + estudianteId)
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminarPersona_Retorna404_SiNoExiste() throws Exception {

        when(rabbitTemplate.convertSendAndReceive(
                anyString(),
                any(Object.class)
        )).thenThrow(new PersonaNotFoundException(estudianteId));

        mockMvc.perform(delete("/api/personas/" + estudianteId)
                        .sessionAttr("personaId", UUID.randomUUID().toString())
                        .sessionAttr("roles", List.of("GERENTE"))
                        .with(csrf())).andDo(print())
                .andExpect(status().isNotFound());

        verify(rabbitTemplate).convertSendAndReceive(anyString(), any(Object.class));
    }
}