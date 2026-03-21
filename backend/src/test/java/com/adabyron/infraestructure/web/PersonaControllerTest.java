package com.adabyron.infraestructure.web;

import com.adabyron.application.persona.CambiarRolDTO;
import com.adabyron.application.persona.CrearPersonaDTO;
import com.adabyron.application.persona.PersonaService;
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

/**
 * Tests del DepartamentoController.
 *
 * Códigos HTTP:
 *  200 → operación exitosa (persona creada, persona encontrada, rol cambiado, etc.)
 *  201 → persona creada correctamente
 *  204 → persona eliminada correctamente (sin contenido)
 *  400 → datos de entrada inválidos o incoherentes (nombre vacío, email inválido, rol desconocido, etc.)
 *  404 → recurso no encontrado (persona con ID dado no existe)
 *  409 → conflicto de estado (email ya registrado, rol incompatible, etc.)
 */

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

    private static final String TEST_PASSWORD_HASH =
        "$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqKj3fB9lVBrPfIEBGDrY1f2QmQoK";

    @BeforeEach
    void setUp() {
        personaEstudiante = PersonaFactory.crearNuevaPersona(
            "Ana García", "ana.garcia@example.com", TEST_PASSWORD_HASH, Rol.ESTUDIANTE, null
        );
        estudianteId = personaEstudiante.getId();

        personaDocente = PersonaFactory.crearNuevaPersona(
            "Dr. House", "house@hospital.com", TEST_PASSWORD_HASH,
            Rol.DOCENTE_INVESTIGADOR, new DepartamentoId(1)
        );
        docenteId = personaDocente.getId();
    }

    // POST /api/personas — crearPersona
    @Test
    void crearPersona_Retorna201yDatos_CuandoEsValida() throws Exception {
        CrearPersonaDTO dto = new CrearPersonaDTO(
            "Ana García", "ana.garcia@example.com", "password123", "ESTUDIANTE", null
        );
        when(personaService.crearPersona(any(CrearPersonaDTO.class)))
            .thenReturn(personaEstudiante);

        mockMvc.perform(post("/api/personas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())                          // 201
                .andExpect(jsonPath("$.nombre").value("Ana García"))
                .andExpect(jsonPath("$.email").value("ana.garcia@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("ESTUDIANTE"));
    }

    @Test
    void crearPersona_Retorna409_CuandoEmailYaExiste() throws Exception {
        CrearPersonaDTO dto = new CrearPersonaDTO(
            "Ana García", "ana.garcia@example.com", "password123", "ESTUDIANTE", null
        );
        when(personaService.crearPersona(any(CrearPersonaDTO.class)))
            .thenThrow(new IllegalArgumentException(
                "Ya existe una persona con el email: ana.garcia@example.com"
            ));

        mockMvc.perform(post("/api/personas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())                         // 409
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void crearPersona_Retorna400_CuandoNombreEsVacio() throws Exception {
        CrearPersonaDTO dto = new CrearPersonaDTO(
            "", "ana.garcia@example.com", "password123", "ESTUDIANTE", null
        );
        when(personaService.crearPersona(any(CrearPersonaDTO.class)))
            .thenThrow(new IllegalArgumentException("El nombre no puede estar vacío"));

        mockMvc.perform(post("/api/personas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())                       // 400
                .andExpect(jsonPath("$.error").value("El nombre no puede estar vacío"));
    }

    @Test
    void crearPersona_Retorna400_CuandoPasswordEsNula() throws Exception {
        CrearPersonaDTO dto = new CrearPersonaDTO(
            "Ana García", "ana.garcia@example.com", null, "ESTUDIANTE", null
        );
        when(personaService.crearPersona(any(CrearPersonaDTO.class)))
            .thenThrow(new IllegalArgumentException("La contraseña es obligatoria"));

        mockMvc.perform(post("/api/personas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())                       // 400
                .andExpect(jsonPath("$.error").value("La contraseña es obligatoria"));
    }

    @Test
    void crearPersona_Retorna400_CuandoRolRequiereDepartamentoYNoSeProporciona() throws Exception {
        CrearPersonaDTO dto = new CrearPersonaDTO(
            "Dr. House", "house@hospital.com", "password123", "DOCENTE_INVESTIGADOR", null
        );
        when(personaService.crearPersona(any(CrearPersonaDTO.class)))
            .thenThrow(new DepartamentoRequeridoException(Rol.DOCENTE_INVESTIGADOR));

        mockMvc.perform(post("/api/personas")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())                       // 400
                .andExpect(jsonPath("$.error").exists());
    }

    // GET /api/personas — listarTodas
    @Test
    void listarTodas_Retorna200yLista_CuandoHayPersonas() throws Exception {
        when(personaService.listarTodas())
            .thenReturn(List.of(personaEstudiante, personaDocente));

        mockMvc.perform(get("/api/personas"))
                .andExpect(status().isOk())                               // 200
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Ana García"));
    }

    @Test
    void listarTodas_Retorna200yListaVacia_CuandoNoHayPersonas() throws Exception {
        when(personaService.listarTodas()).thenReturn(List.of());

        mockMvc.perform(get("/api/personas"))
                .andExpect(status().isOk())                               // 200
                .andExpect(jsonPath("$.length()").value(0));
    }

    // GET /api/personas/{id} — buscarPorId
    @Test
    void buscarPorId_Retorna200_SiExiste() throws Exception {
        when(personaService.buscarPorId(estudianteId))
            .thenReturn(personaEstudiante);

        mockMvc.perform(get("/api/personas/" + estudianteId))
                .andExpect(status().isOk())                               // 200
                .andExpect(jsonPath("$.nombre").value("Ana García"));
    }

    @Test
    void buscarPorId_Retorna404_SiNoExiste() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(personaService.buscarPorId(idInexistente))
            .thenThrow(new PersonaNotFoundException(idInexistente));

        mockMvc.perform(get("/api/personas/" + idInexistente))
                .andExpect(status().isNotFound())                         // 404
                .andExpect(jsonPath("$.error").exists());
    }

    // PUT /api/personas/{id}/rol — cambiarRol
    @Test
    void cambiarRol_Retorna200_SiSeCambiaOK() throws Exception {
        CambiarRolDTO dto = new CambiarRolDTO("CONSERJE", null);
        Persona personaActualizada = PersonaFactory.crearNuevaPersona(
            "Ana García", "ana.garcia@example.com", TEST_PASSWORD_HASH, Rol.CONSERJE, null
        );
        when(personaService.cambiarRol(eq(estudianteId), any(CambiarRolDTO.class)))
            .thenReturn(personaActualizada);

        mockMvc.perform(put("/api/personas/" + estudianteId + "/rol")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())                               // 200
                .andExpect(jsonPath("$.roles[0]").value("CONSERJE"));
    }

    @Test
    void cambiarRol_Retorna404_SiPersonaNoExiste() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        CambiarRolDTO dto = new CambiarRolDTO("CONSERJE", null);

        when(personaService.cambiarRol(eq(idInexistente), any(CambiarRolDTO.class)))
            .thenThrow(new PersonaNotFoundException(idInexistente));

        mockMvc.perform(put("/api/personas/" + idInexistente + "/rol")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())                         // 404
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void cambiarRol_Retorna400_CuandoRolEsInvalido() throws Exception {
        CambiarRolDTO dto = new CambiarRolDTO("ROL_QUE_NO_EXISTE", null);

        when(personaService.cambiarRol(eq(estudianteId), any(CambiarRolDTO.class)))
            .thenThrow(new IllegalArgumentException(
                "No enum constant com.adabyron.domain.persona.Rol.ROL_QUE_NO_EXISTE"
            ));

        mockMvc.perform(put("/api/personas/" + estudianteId + "/rol")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())                       // 400
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void cambiarRol_Retorna400_CuandoNuevoRolRequiereDepartamentoYNoSeProporciona() throws Exception {
        CambiarRolDTO dto = new CambiarRolDTO("DOCENTE_INVESTIGADOR", null);

        when(personaService.cambiarRol(eq(estudianteId), any(CambiarRolDTO.class)))
            .thenThrow(new DepartamentoRequeridoException(Rol.DOCENTE_INVESTIGADOR));

        mockMvc.perform(put("/api/personas/" + estudianteId + "/rol")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())                       // 400
                .andExpect(jsonPath("$.error").exists());
    }

    // PUT /api/personas/{id}/gerente — añadirGerente
    @Test
    void añadirGerente_Retorna200_CuandoEsPosible() throws Exception {
        personaDocente.añadirRolGerente();
        when(personaService.añadirRolGerente(docenteId))
            .thenReturn(personaDocente);

        mockMvc.perform(put("/api/personas/" + docenteId + "/gerente")
                .with(csrf()))
                .andExpect(status().isOk())                               // 200
                .andExpect(jsonPath("$.roles").isArray());
    }

    @Test
    void añadirGerente_Retorna404_SiPersonaNoExiste() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(personaService.añadirRolGerente(idInexistente))
            .thenThrow(new PersonaNotFoundException(idInexistente));

        mockMvc.perform(put("/api/personas/" + idInexistente + "/gerente")
                .with(csrf()))
                .andExpect(status().isNotFound())                         // 404
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void añadirGerente_Retorna409_SiPersonaNoEsDocenteInvestigador() throws Exception {
        // ESTUDIANTE no puede ser GERENTE, solo DOCENTE_INVESTIGADOR puede ser GERENTE
        when(personaService.añadirRolGerente(estudianteId))
            .thenThrow(new RolIncompatibleException(
                "Solo un Docente-Investigador puede añadir el rol de Gerente (REQ-B2). " +
                "Roles actuales: [ESTUDIANTE]"
            ));

        mockMvc.perform(put("/api/personas/" + estudianteId + "/gerente")
                .with(csrf()))
                .andExpect(status().isConflict())                         // 409
                .andExpect(jsonPath("$.error").exists());
    }

    // DELETE /api/personas/{id}/gerente — quitarGerente
    @Test
    void quitarGerente_Retorna200_CuandoEsPosible() throws Exception {
        personaDocente.añadirRolGerente();
        personaDocente.quitarRolGerente();
        when(personaService.quitarRolGerente(docenteId))
            .thenReturn(personaDocente);

        mockMvc.perform(delete("/api/personas/" + docenteId + "/gerente")
                .with(csrf()))
                .andExpect(status().isOk())                               // 200
                .andExpect(jsonPath("$.roles[0]").value("DOCENTE_INVESTIGADOR"));
    }

    @Test
    void quitarGerente_Retorna404_SiPersonaNoExiste() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        when(personaService.quitarRolGerente(idInexistente))
            .thenThrow(new PersonaNotFoundException(idInexistente));

        mockMvc.perform(delete("/api/personas/" + idInexistente + "/gerente")
                .with(csrf()))
                .andExpect(status().isNotFound())                         // 404
                .andExpect(jsonPath("$.error").exists());
    }

    // DELETE /api/personas/{id} — eliminarPersona
    @Test
    void eliminarPersona_Retorna204_SiExiste() throws Exception {
        doNothing().when(personaService).eliminar(estudianteId);

        mockMvc.perform(delete("/api/personas/" + estudianteId)
                .with(csrf()))
                .andExpect(status().isNoContent());                       // 204

        verify(personaService, times(1)).eliminar(estudianteId);
    }

    @Test
    void eliminarPersona_Retorna404_SiNoExiste() throws Exception {
        UUID idInexistente = UUID.randomUUID();
        doThrow(new PersonaNotFoundException(idInexistente))
            .when(personaService).eliminar(idInexistente);

        mockMvc.perform(delete("/api/personas/" + idInexistente)
                .with(csrf()))
                .andExpect(status().isNotFound())                         // 404
                .andExpect(jsonPath("$.error").exists());

        verify(personaService, times(1)).eliminar(idInexistente);
    }
}