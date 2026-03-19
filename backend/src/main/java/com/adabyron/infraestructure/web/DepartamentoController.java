package com.adabyron.infraestructure.web;

import com.adabyron.application.persona.DepartamentoDTO;
import com.adabyron.domain.persona.Departamento;
import com.adabyron.domain.persona.DepartamentoId;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departamentos")
@Tag(name = "Departamentos", description = "Consulta del catálogo de departamentos disponibles en el sistema")
public class DepartamentoController {

    @Operation(
        summary = "Listar todos los departamentos",
        description = "Devuelve la lista completa de departamentos definidos en el sistema. " +
                      "Los departamentos son un catálogo estático derivado del enum `Departamento`."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de departamentos obtenida correctamente.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                array = @ArraySchema(schema = @Schema(implementation = DepartamentoDTO.class))
            )
        )
    })
    @GetMapping
    public List<DepartamentoDTO> listarTodos() {
        return Departamento.values().stream()
                .map(DepartamentoDTO::fromEntity)
                .toList();
    }

    @Operation(
        summary = "Buscar departamento por ID",
        description = "Devuelve el departamento cuyo identificador numérico coincide con el proporcionado. " +
                      "Si el ID no corresponde a ningún departamento definido, se lanzará una excepción."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Departamento encontrado.",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = DepartamentoDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No existe ningún departamento con el ID indicado.",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public DepartamentoDTO buscarPorId(
        @Parameter(description = "Identificador numérico del departamento", example = "1", required = true)
        @PathVariable int id
    ) {
        Departamento departamento = Departamento.fromId(new DepartamentoId(id));
        return DepartamentoDTO.fromEntity(departamento);
    }
}