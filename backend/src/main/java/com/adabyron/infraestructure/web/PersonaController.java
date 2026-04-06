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

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/personas")
@Tag(name = "Personas", description = "Gestión de personas: creación, consulta, asignación de roles y eliminación")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
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
        if (session == null) return false;

        Object rolesObj = session.getAttribute("roles");
        return rolesObj instanceof Collection<?> roles
                && roles.stream().anyMatch(r -> "GERENTE".equals(String.valueOf(r)));
    }

    @Operation(
        summary = "Crear una nueva persona",
        description = "Registra una nueva persona en el sistema a partir de los datos proporcionados. " +
                      "Devuelve la entidad creada con su ID generado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Persona creada correctamente.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PersonaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos o incompletos.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe una persona registrada con el mismo email.",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<PersonaDTO> crear(@RequestBody CrearPersonaDTO dto) {
        var persona = personaService.crearPersona(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(PersonaDTO.fromEntity(persona));
    }

    @Operation(
        summary = "Listar todas las personas",
        description = "Devuelve la lista completa de personas registradas en el sistema."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de personas obtenida correctamente.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = PersonaDTO.class))
            )
        )
    })
    @GetMapping
    public List<PersonaDTO> listarTodas(HttpServletRequest request) {
        requireGerente(request);
        return personaService.listarTodas().stream()
                .map(PersonaDTO::fromEntity)
                .toList();
    }

    @Operation(
        summary = "Buscar persona por ID",
        description = "Devuelve los datos de la persona cuyo UUID coincide con el proporcionado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Persona encontrada.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PersonaDTO.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "No existe ninguna persona con el ID indicado.", content = @Content)
    })
    @GetMapping("/{id}")
    public PersonaDTO buscarPorId(
        @Parameter(description = "UUID de la persona", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable UUID id
    ) {
        return PersonaDTO.fromEntity(personaService.buscarPorId(id));
    }

    @Operation(
        summary = "Buscar persona por email",
        description = "Devuelve los datos de la persona cuyo email coincide exactamente con el proporcionado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Persona encontrada.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PersonaDTO.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "No existe ninguna persona con ese email.", content = @Content)
    })
    @GetMapping("/email/{email}")
    public PersonaDTO buscarPorEmail(
        @Parameter(description = "Email de la persona", example = "usuario@ejemplo.com", required = true)
        @PathVariable String email
    ) {
        return PersonaDTO.fromEntity(personaService.buscarPorEmail(email));
    }

    @Operation(
        summary = "Cambiar el rol de una persona",
        description = "Sustituye el rol actual de la persona indicada por el rol especificado en el cuerpo de la petición."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Rol actualizado correctamente.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PersonaDTO.class)
            )
        ),
        @ApiResponse(responseCode = "400", description = "Rol inválido o no permitido.", content = @Content),
        @ApiResponse(responseCode = "404", description = "No existe ninguna persona con el ID indicado.", content = @Content)
    })
    @PutMapping("/{id}/rol")
    public PersonaDTO cambiarRol(
        @Parameter(description = "UUID de la persona", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable UUID id,
        @RequestBody CambiarRolDTO dto,
        HttpServletRequest request
    ) {
        requireGerente(request);
        return PersonaDTO.fromEntity(personaService.cambiarRol(id, dto));
    }

    @Operation(
        summary = "Añadir rol de gerente",
        description = "Asigna el rol de gerente a la persona indicada, además de los roles que ya tenga."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Rol de gerente añadido correctamente.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PersonaDTO.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "No existe ninguna persona con el ID indicado.", content = @Content)
    })
    @PutMapping("/{id}/gerente")
    public PersonaDTO añadirGerente(
        @Parameter(description = "UUID de la persona", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        requireGerente(request);
        return PersonaDTO.fromEntity(personaService.añadirRolGerente(id));
    }

    @Operation(
        summary = "Quitar rol de gerente",
        description = "Revoca el rol de gerente de la persona indicada, manteniendo el resto de sus roles."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Rol de gerente eliminado correctamente.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PersonaDTO.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "No existe ninguna persona con el ID indicado.", content = @Content)
    })
    @DeleteMapping("/{id}/gerente")
    public PersonaDTO quitarGerente(
        @Parameter(description = "UUID de la persona", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        requireGerente(request);
        return PersonaDTO.fromEntity(personaService.quitarRolGerente(id));
    }

    @Operation(
        summary = "Eliminar una persona",
        description = "Elimina permanentemente del sistema la persona con el UUID indicado. La operación es irreversible."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Persona eliminada correctamente. Sin cuerpo en la respuesta."),
        @ApiResponse(responseCode = "404", description = "No existe ninguna persona con el ID indicado.", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
        @Parameter(description = "UUID de la persona", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable UUID id,
        HttpServletRequest request
    ) {
        requireGerente(request);
        personaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}