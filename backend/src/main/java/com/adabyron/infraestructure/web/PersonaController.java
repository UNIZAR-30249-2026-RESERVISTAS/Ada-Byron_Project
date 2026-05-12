package com.adabyron.infraestructure.web;

import com.adabyron.application.persona.*;
import com.adabyron.domain.reserva.exception.OperacionNoAutorizadaException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Collection;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/personas")
@Tag(name = "Personas", description = "Gestión de personas: creación, consulta, asignación de roles y eliminación")
public class PersonaController {

    private final RabbitTemplate rabbitTemplate;

    public PersonaController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Funciones auxiliares para autorización basada en sesión
    private UUID requirePersonaId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("personaId") == null) {
            throw new OperacionNoAutorizadaException("Debes iniciar sesión");
        }
        return UUID.fromString(String.valueOf(session.getAttribute("personaId")));
    }

    private void requireGerente(HttpServletRequest request) {
        requirePersonaId(request);
        if (!esGerente(request)) {
            throw new OperacionNoAutorizadaException("Solo el gerente puede acceder a este recurso");
        }
    }

    private boolean esGerente(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null)
            return false;

        Object rolesObj = session.getAttribute("roles");
        return rolesObj instanceof Collection<?> roles
                && roles.stream().anyMatch(r -> "GERENTE".equals(String.valueOf(r)));
    }

    @Operation(summary = "Crear una nueva persona", description = "Registra una nueva persona en el sistema a partir de los datos proporcionados. "
            +
            "Devuelve la entidad creada con su ID generado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Persona creada correctamente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PersonaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos o incompletos.", content = @Content),
            @ApiResponse(responseCode = "409", description = "Ya existe una persona registrada con el mismo email.", content = @Content)
    })
    @PostMapping
    public ResponseEntity<PersonaDTO> crear(@RequestBody CrearPersonaDTO dto) throws TimeoutException {
        // var persona = personaService.crearPersona(dto);
        // return
        // ResponseEntity.status(HttpStatus.CREATED).body(PersonaDTO.fromEntity(persona));

        Object respuesta = rabbitTemplate.convertSendAndReceive("persona.crear", dto);
        if (respuesta == null) {
            throw new TimeoutException();
        }
        PersonaDTO personaConfirmada = (PersonaDTO) respuesta;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(personaConfirmada);
    }

    @Operation(summary = "Listar todas las personas", description = "Devuelve la lista completa de personas registradas en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de personas obtenida correctamente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, array = @ArraySchema(schema = @Schema(implementation = PersonaDTO.class))))
    })
    @GetMapping
    public List<PersonaDTO> listarTodas(HttpServletRequest request) throws TimeoutException {
        requireGerente(request);
        // return personaService.listarTodas().stream()
        // .map(PersonaDTO::fromEntity)
        // .toList();

        ParameterizedTypeReference<List<PersonaDTO>> tipoRespuesta = new ParameterizedTypeReference<>() {
        };
        List<PersonaDTO> lista = rabbitTemplate.convertSendAndReceiveAsType("persona.listar", "Listar", tipoRespuesta);

        if (lista == null) {
            throw new TimeoutException();
        }

        return lista;

    }

    @Operation(summary = "Buscar persona por ID", description = "Devuelve los datos de la persona cuyo UUID coincide con el proporcionado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Persona encontrada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PersonaDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe ninguna persona con el ID indicado.", content = @Content)
    })
    @GetMapping("/{id}")
    public PersonaDTO buscarPorId(
            @Parameter(description = "UUID de la persona", example = "123e4567-e89b-12d3-a456-426614174000", required = true) @PathVariable UUID id)
            throws TimeoutException {
        // return PersonaDTO.fromEntity(personaService.buscarPorId(id));
        Object respuesta = rabbitTemplate.convertSendAndReceive("persona.buscar.porId", id);

        if (respuesta == null) {
            throw new TimeoutException("El servidor de aplicaciones no responde.");
        }

        return (PersonaDTO) respuesta;
    }

    @Operation(summary = "Buscar persona por email", description = "Devuelve los datos de la persona cuyo email coincide exactamente con el proporcionado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Persona encontrada.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PersonaDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe ninguna persona con ese email.", content = @Content)
    })
    @GetMapping("/email/{email}")
    public PersonaDTO buscarPorEmail(
            @Parameter(description = "Email de la persona", example = "usuario@ejemplo.com", required = true) @PathVariable String email)
            throws TimeoutException {
        // return PersonaDTO.fromEntity(personaService.buscarPorEmail(email));

        Object respuesta = rabbitTemplate.convertSendAndReceive("persona.buscar.porEmail", email);

        if (respuesta == null) {
            throw new TimeoutException("El servidor de aplicaciones no responde.");
        }

        return (PersonaDTO) respuesta;
    }

    @Operation(summary = "Cambiar el rol de una persona", description = "Sustituye el rol actual de la persona indicada por el rol especificado en el cuerpo de la petición.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol actualizado correctamente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PersonaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Rol inválido o no permitido.", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe ninguna persona con el ID indicado.", content = @Content)
    })
    @PutMapping("/{id}/rol")
    public PersonaDTO cambiarRol(
            @Parameter(description = "UUID de la persona", example = "123e4567-e89b-12d3-a456-426614174000", required = true) @PathVariable UUID id,
            @RequestBody CambiarRolDTO dto,
            HttpServletRequest request) throws TimeoutException {
        requireGerente(request);
        // return PersonaDTO.fromEntity(personaService.cambiarRol(id, dto));
        CambiarRolCommand comando = new CambiarRolCommand(id, dto);
        Object respuesta = rabbitTemplate.convertSendAndReceive("persona.cambiarRol", comando);
        if (respuesta == null) {
            throw new TimeoutException("El servidor de aplicaciones no responde.");
        }

        return (PersonaDTO) respuesta;
    }

    @Operation(summary = "Añadir rol de gerente", description = "Asigna el rol de gerente a la persona indicada, además de los roles que ya tenga.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol de gerente añadido correctamente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PersonaDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe ninguna persona con el ID indicado.", content = @Content)
    })
    @PutMapping("/{id}/gerente")
    public PersonaDTO añadirGerente(
            @Parameter(description = "UUID de la persona", example = "123e4567-e89b-12d3-a456-426614174000", required = true) @PathVariable UUID id,
            HttpServletRequest request) throws TimeoutException {
        requireGerente(request);
        // return PersonaDTO.fromEntity(personaService.añadirRolGerente(id));
        Object respuesta = rabbitTemplate.convertSendAndReceive("persona.añadirGerente", id);
        if (respuesta == null) {
            throw new TimeoutException("El servidor de aplicaciones no responde.");
        }
        return (PersonaDTO) respuesta;
    }

    @Operation(summary = "Quitar rol de gerente", description = "Revoca el rol de gerente de la persona indicada, manteniendo el resto de sus roles.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rol de gerente eliminado correctamente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PersonaDTO.class))),
            @ApiResponse(responseCode = "404", description = "No existe ninguna persona con el ID indicado.", content = @Content)
    })
    @DeleteMapping("/{id}/gerente")
    public PersonaDTO quitarGerente(
            @Parameter(description = "UUID de la persona", example = "123e4567-e89b-12d3-a456-426614174000", required = true) @PathVariable UUID id,
            HttpServletRequest request) throws TimeoutException {
        requireGerente(request);
        // return PersonaDTO.fromEntity(personaService.quitarRolGerente(id));
        Object respuesta = rabbitTemplate.convertSendAndReceive("persona.quitarGerente", id);
        if (respuesta == null) {
            throw new TimeoutException("El servidor de aplicaciones no responde.");
        }
        return (PersonaDTO) respuesta;
    }

    @Operation(summary = "Cambiar el departamento de una persona", description = "Actualiza la adscripción de departamento de la persona indicada. "
            +
            "Solo permitido si su rol principal requiere departamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Departamento actualizado correctamente.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = PersonaDTO.class))),
            @ApiResponse(responseCode = "400", description = "Departamento inválido o rol no compatible.", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe ninguna persona con el ID indicado.", content = @Content)
    })
    @PutMapping("/{id}/departamento")
    public PersonaDTO cambiarDepartamento(
            @Parameter(description = "UUID de la persona", example = "123e4567-e89b-12d3-a456-426614174000", required = true) @PathVariable UUID id,
            @RequestBody CambiarDepartamentoDTO dto,
            HttpServletRequest request) throws TimeoutException {
        requireGerente(request);
        // return PersonaDTO.fromEntity(personaService.cambiarDepartamento(id, dto));
        CambiarDepartamentoCommand comando = new CambiarDepartamentoCommand(id, dto);
        Object respuesta = rabbitTemplate.convertSendAndReceive("persona.cambiarDepartamento", comando);
        if (respuesta == null) {
            throw new TimeoutException("El servidor de aplicaciones no responde.");
        }

        return (PersonaDTO) respuesta;
    }

    @Operation(summary = "Eliminar una persona", description = "Elimina permanentemente del sistema la persona con el UUID indicado. La operación es irreversible.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Persona eliminada correctamente. Sin cuerpo en la respuesta."),
            @ApiResponse(responseCode = "404", description = "No existe ninguna persona con el ID indicado.", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "UUID de la persona", example = "123e4567-e89b-12d3-a456-426614174000", required = true) @PathVariable UUID id,
            HttpServletRequest request) throws TimeoutException {
        requireGerente(request);
        // personaService.eliminar(id);
        // return ResponseEntity.noContent().build();

        Object confirmacion = rabbitTemplate.convertSendAndReceive("persona.eliminar", id);

        if (confirmacion == null) {
            throw new TimeoutException("El servidor de aplicaciones no responde.");
        }

        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(value = HttpStatus.REQUEST_TIMEOUT)
    @ExceptionHandler(TimeoutException.class)
    public String timeout() {
        return "La petición no puede resolverse ahora, el servidor de personas no responde.";
    }
}