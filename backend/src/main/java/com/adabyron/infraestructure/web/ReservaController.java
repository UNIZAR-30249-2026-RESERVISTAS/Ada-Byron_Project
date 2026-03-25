package com.adabyron.infraestructure.web;

import java.util.List;
import java.util.UUID;

import com.adabyron.application.reserva.CrearReservaDTO;
import com.adabyron.application.reserva.ReservaDTO;
import com.adabyron.application.reserva.ReservaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;


@RestController
@RequestMapping("/api/reservas")
@Tag(name = "Reservas", description = "Gestión de reservas de espacios: creación, consulta, cancelación y revalidación")
public class ReservaController {
 
    private final ReservaService reservaService;
 
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }
 
    @Operation(
        summary = "Crear una nueva reserva",
        description = "Registra una solicitud de reserva para uno o más espacios. " +
                      "El sistema valida automáticamente las reglas de negocio (REQ-F1 a REQ-F8). " +
                      "Si se cumplen, la reserva queda en estado CONFIRMADA. " +
                      "Si alguna regla falla, queda en estado RECHAZADA con el motivo indicado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Reserva creada. Puede estar CONFIRMADA o RECHAZADA según las reglas.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ReservaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos (fechas incorrectas, asistentes <= 0, etc.).",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "La persona o alguno de los espacios indicados no existe.",
            content = @Content
        )
    })
    @PostMapping
    public ResponseEntity<ReservaDTO> crear(@RequestBody CrearReservaDTO dto) {
        var reserva = reservaService.crearReserva(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(ReservaDTO.fromEntity(reserva));
    }
 
    @Operation(
        summary = "Listar reservas activas",
        description = "Devuelve todas las reservas cuya hora de finalización es posterior al momento actual " +
                      "y cuyo estado es CONFIRMADA o POTENCIALMENTE_INVALIDA. " +
                      "Operación destinada a usuarios con rol Gerente (REQ-H1)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de reservas activas obtenida correctamente.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = ReservaDTO.class))
            )
        )
    })
    @GetMapping
    public List<ReservaDTO> listarActivas() {
        return reservaService.listarReservasActivas().stream()
                             .map(ReservaDTO::fromEntity)
                             .toList();
    }
 
    @Operation(
        summary = "Buscar reserva por ID",
        description = "Devuelve los datos completos de la reserva cuyo UUID coincide con el proporcionado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Reserva encontrada.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ReservaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No existe ninguna reserva con el ID indicado.",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ReservaDTO buscarPorId(
        @Parameter(description = "UUID de la reserva", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable UUID id
    ) {
        return ReservaDTO.fromEntity(reservaService.buscarPorId(id));
    }
 
    @Operation(
        summary = "Listar reservas de una persona",
        description = "Devuelve todas las reservas realizadas por la persona cuyo UUID se indica, " +
                      "independientemente de su estado actual."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de reservas de la persona obtenida correctamente.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = ReservaDTO.class))
            )
        )
    })
    @GetMapping("/persona/{personaId}")
    public List<ReservaDTO> listarPorPersona(
        @Parameter(description = "UUID de la persona", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable UUID personaId
    ) {
        return reservaService.listarPorPersona(personaId).stream()
                             .map(ReservaDTO::fromEntity)
                             .toList();
    }
 
    @Operation(
        summary = "Listar reservas potencialmente inválidas",
        description = "Devuelve todas las reservas en estado POTENCIALMENTE_INVALIDA. " +
                      "Son reservas que estaban confirmadas pero han dejado de cumplir las reglas " +
                      "tras un cambio en las condiciones del sistema (p.ej. cambio del porcentaje de ocupación). " +
                      "El gerente puede revalidarlas o cancelarlas una a una (O4)."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de reservas potencialmente inválidas obtenida correctamente.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = ReservaDTO.class))
            )
        )
    })
    @GetMapping("/potencialmente-invalidas")
    public List<ReservaDTO> listarPotencialmenteInvalidas() {
        return reservaService.listarPotencialmenteInvalidas().stream()
                             .map(ReservaDTO::fromEntity)
                             .toList();
    }
 
    @Operation(
        summary = "Revalidar una reserva potencialmente inválida",
        description = "El gerente decide que una reserva en estado POTENCIALMENTE_INVALIDA " +
                      "sigue siendo válida y la devuelve al estado CONFIRMADA (O4). " +
                      "Solo puede realizarse por un usuario con rol GERENTE."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Reserva revalidada correctamente. Vuelve a estado CONFIRMADA.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ReservaDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "La reserva no está en estado POTENCIALMENTE_INVALIDA y no puede revalidarse.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "El usuario no tiene rol de GERENTE para realizar esta operación.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No existe ninguna reserva con el ID indicado o la persona no existe.",
            content = @Content
        )
    })
    @PutMapping("/{id}/revalidar")
    public ReservaDTO revalidar(
        @Parameter(description = "UUID de la reserva", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable UUID id,
        @Parameter(description = "UUID del gerente que ejecuta la revalidación", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @RequestParam UUID gerenteId
    ) {
        return ReservaDTO.fromEntity(reservaService.revalidarReserva(id, gerenteId));
    }
 
    @Operation(
        summary = "Cancelar una reserva",
        description = "Cancela una reserva en estado CONFIRMADA o POTENCIALMENTE_INVALIDA. " +
                      "La reserva pasa a estado CANCELADA y se notifica al usuario que la realizó (REQ-H2, REQ-I2). " +
                      "Se puede indicar un motivo de cancelación opcional. " +
                      "Puede ser realizada por:\n" +
                      "- El GERENTE (puede cancelar cualquier reserva)\n" +
                      "- El usuario que creó la reserva (solo puede cancelar las suyas)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Reserva cancelada correctamente. Sin cuerpo en la respuesta."
        ),
        @ApiResponse(
            responseCode = "400",
            description = "La reserva ya está cancelada o rechazada y no puede modificarse.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "El usuario no tiene permisos para cancelar esta reserva. " +
                         "Solo el gerente o el creador pueden cancelarla.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No existe ninguna reserva con el ID indicado o la persona no existe.",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(
        @Parameter(description = "UUID de la reserva", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable UUID id,
        @Parameter(description = "UUID de la persona que ejecuta la cancelación", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @RequestParam UUID solicitanteId,
        @Parameter(description = "Motivo de la cancelación (opcional)", example = "El espacio ya no está disponible")
        @RequestParam(required = false) String motivo
    ) {
        reservaService.cancelarReserva(id, solicitanteId, motivo);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Eliminar permanentemente una reserva",
        description = "Elimina físicamente una reserva de la base de datos (hard delete). " +
                      "IMPORTANTE: Esta operación es IRREVERSIBLE. " +
                      "La reserva se elimina completamente y no hay manera de recuperarla. " +
                      "Se recomienda usar la operación de cancelar en lugar de eliminar para mantener el historial. " +
                      "Puede ser realizada por:\n" +
                      "- El GERENTE (puede eliminar cualquier reserva)\n" +
                      "- El usuario que creó la reserva (solo puede eliminar las suyas)"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Reserva eliminada permanentemente. Sin cuerpo en la respuesta."
        ),
        @ApiResponse(
            responseCode = "403",
            description = "El usuario no tiene permisos para eliminar esta reserva. " +
                         "Solo el gerente o el creador pueden eliminarla.",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No existe ninguna reserva con el ID indicado o la persona no existe.",
            content = @Content
        )
    })
    @DeleteMapping("/{id}/permanente")
    public ResponseEntity<Void> eliminarPermanentemente(
        @Parameter(description = "UUID de la reserva", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @PathVariable UUID id,
        @Parameter(description = "UUID de la persona que ejecuta la eliminación", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @RequestParam UUID solicitanteId
    ) {
        reservaService.eliminarReserva(id, solicitanteId);
        return ResponseEntity.noContent().build();
    }
}
