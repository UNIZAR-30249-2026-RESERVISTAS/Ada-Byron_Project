package com.adabyron.infraestructure.web;

import com.adabyron.application.espacio.*;
import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.HorarioDisponible;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/espacios")
@Tag(name = "Espacios", description = "Gestión de espacios: consulta, asignación de categoría, estado y horarios")
public class EspacioController {
    private final EspacioService espacioService;

    public EspacioController(EspacioService espacioService) {
        this.espacioService = espacioService;
    }

    @Operation(
            summary = "Obtener información de un espacio",
            description = "Devuelve el identificador, la categoría, el número de ocupantes máximos, el tamaño, si es reservable " +
                    "y el horario disponible del espacio cuyo ID coincide con el proporcionado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Espacio encontrado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EspacioDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "No existe ningún espacio con el ID indicado.", content = @Content)
    })
    @GetMapping("/{id}")
    public EspacioDTO buscarPorId(@PathVariable String id){
        return EspacioDTO.fromEntity(espacioService.obtenerDetalles(id));
    }

    @Operation(
            summary = "Cambiar la categoría de un espacio",
            description = "Sustituye la categoría actual del espacio indicado por la categoría especificada en el cuerpo de la petición."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría actualizada correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EspacioDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "No existe ningún espacio con el ID indicado.", content = @Content)
    })
    @PutMapping("/{id}/categoria")
    public EspacioDTO cambiarCategoria(@PathVariable String id, @RequestBody CambiarCategoriaDTO dto){
        return EspacioDTO.fromEntity(espacioService.cambiarCategoria(id, Categoria.desdeNombre(dto.categoria())));
    }

    @Operation(
            summary = "Cambiar el estado de un espacio",
            description = "Sustituye el estado actual del espacio indicado por el estado especificado en el cuerpo de la petición."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado actualizado correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EspacioDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "No existe ningún espacio con el ID indicado.", content = @Content)
    })
    @PutMapping("/{id}/reservable")
    public EspacioDTO cambiarEstado(@PathVariable String id, @RequestBody CambiarReservableDTO dto){
        return EspacioDTO.fromEntity(espacioService.cambiarReservable(id, dto.reservable()));
    }
    
    // ENDPOINTS DE HORARIOS (REQ-C5, REQ-C6)

    @Operation(
            summary = "Obtener el horario de un espacio",
            description = "REQ-C5: Devuelve el horario disponible del espacio. " +
                    "Si el espacio tiene un horario específico (REQ-C6), se devuelve ese horario. " +
                    "Si no, se devuelve el horario por defecto del edificio Ada Byron (8:00 - 21:00)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Horario del espacio obtenido correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = HorarioDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "No existe ningún espacio con el ID indicado.", content = @Content)
    })
    @GetMapping("/{id}/horario")
    public HorarioDTO obtenerHorario(@PathVariable String id) {
        var espacio = espacioService.obtenerDetalles(id);
        return HorarioDTO.fromDomain(
            espacio.getHorarioDisponible(),
            !espacio.tieneHorarioEspecifico()
        );
    }

    @Operation(
            summary = "Cambiar el horario de un espacio",
            description = "REQ-C6: Permite a los gerentes cambiar el horario de reserva de un espacio. " +
                    "El nuevo horario debe estar contenido dentro del horario del edificio Ada Byron (8:00 - 21:00). " +
                    "Solo puede realizarse por un usuario con rol GERENTE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Horario actualizado correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EspacioDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Horario inválido (fuera del horario del edificio o hora apertura >= hora cierre).", content = @Content),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol de GERENTE para realizar esta operación.", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe ningún espacio con el ID indicado o la persona no existe.", content = @Content)
    })
    @PutMapping("/{id}/horario")
    public EspacioDTO cambiarHorario(
            @Parameter(description = "ID del espacio", example = "107", required = true)
            @PathVariable String id,
            @Parameter(description = "UUID del gerente que ejecuta la operación", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @RequestParam UUID gerenteId,
            @RequestBody CambiarHorarioDTO dto
    ) {
        HorarioDisponible nuevoHorario = new HorarioDisponible(dto.horaApertura(), dto.horaCierre());
        return EspacioDTO.fromEntity(espacioService.cambiarHorario(id, nuevoHorario, gerenteId));
    }

    @Operation(
            summary = "Restablecer el horario de un espacio al horario del edificio",
            description = "REQ-C6: Elimina el horario específico del espacio y restablece el horario por defecto del edificio Ada Byron (8:00 - 21:00). " +
                    "Solo puede realizarse por un usuario con rol GERENTE."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Horario restablecido al del edificio correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = EspacioDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene rol de GERENTE para realizar esta operación.", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe ningún espacio con el ID indicado o la persona no existe.", content = @Content)
    })
    @DeleteMapping("/{id}/horario")
    public EspacioDTO restablecerHorario(
            @Parameter(description = "ID del espacio", example = "107", required = true)
            @PathVariable String id,
            @Parameter(description = "UUID del gerente que ejecuta la operación", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
            @RequestParam UUID gerenteId
    ) {
        return EspacioDTO.fromEntity(espacioService.restablecerHorario(id, gerenteId));
    }

	@Operation(
    summary = "Obtener la asignación actual de un espacio",
    description = "Devuelve a quién o qué está asignado el espacio: la EINA, un departamento o una o más personas concretas (REQ-C7, REQ-C8)."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Asignación del espacio obtenida correctamente.",
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = AsignacionDTO.class)
			)
		),
		@ApiResponse(responseCode = "404", description = "No existe ningún espacio con el ID indicado.", content = @Content)
	})
    @GetMapping("/{id}/asignacion")
	public AsignacionDTO obtenerAsignacion(@PathVariable String id) {
		return AsignacionDTO.fromDomain(espacioService.obtenerAsignacion(id));
	}

	@Operation(
    summary = "Asignar un espacio a la EINA",
    description = "Asigna el espacio indicado a la EINA. " +
                  "Aplicable a aulas y salas comunes (REQ-C9)."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Espacio asignado a la EINA correctamente.",
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = EspacioDTO.class)
			)
		),
		@ApiResponse(responseCode = "400", description = "La categoría del espacio no permite asignación a la EINA.", content = @Content),
		@ApiResponse(responseCode = "404", description = "No existe ningún espacio con el ID indicado.", content = @Content)
	})
	@PutMapping("/{id}/asignacion/eina")
	public EspacioDTO asignarAEina(@PathVariable String id) {
		return EspacioDTO.fromEntity(espacioService.asignarAEina(id));
	}

	@Operation(
    summary = "Asignar un espacio a un departamento",
    description = "Asigna el espacio indicado al departamento especificado. " +
                  "Aplicable a despachos, seminarios y laboratorios (REQ-C10, REQ-C11)."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Espacio asignado al departamento correctamente.",
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = EspacioDTO.class)
			)
		),
		@ApiResponse(responseCode = "400", description = "La categoría del espacio no permite asignación a un departamento.", content = @Content),
		@ApiResponse(responseCode = "404", description = "No existe ningún espacio con el ID indicado.", content = @Content)
	})
	@PutMapping("/{id}/asignacion/departamento")
	public EspacioDTO asignarADepartamento(
			@PathVariable String id,
			@RequestBody AsignarDepartamentoDTO dto
	) {
		return EspacioDTO.fromEntity(
			espacioService.asignarADepartamento(id, dto.departamentoId())
		);
	}

	@Operation(
    summary = "Asignar un espacio a una o más personas",
    description = "Asigna el espacio indicado a las personas especificadas. " +
                  "Solo aplicable a despachos, y las personas deben tener rol de " +
                  "investigador contratado o docente-investigador (REQ-C10)."
	)
	@ApiResponses({
		@ApiResponse(
			responseCode = "200",
			description = "Espacio asignado a las personas correctamente.",
			content = @Content(
				mediaType = MediaType.APPLICATION_JSON_VALUE,
				schema = @Schema(implementation = EspacioDTO.class)
			)
		),
		@ApiResponse(responseCode = "400", description = "La categoría del espacio no permite asignación a personas, o alguna persona no tiene el rol requerido.", content = @Content),
		@ApiResponse(responseCode = "404", description = "No existe ningún espacio con el ID indicado o alguna de las personas no existe.", content = @Content)
	})
	@PutMapping("/{id}/asignacion/personas")
	public EspacioDTO asignarAPersonas(
			@PathVariable String id,
			@RequestBody AsignarPersonasDTO dto
	) {
		return EspacioDTO.fromEntity(
			espacioService.asignarAPersonas(id, dto.personaIds())
		);
	}

}