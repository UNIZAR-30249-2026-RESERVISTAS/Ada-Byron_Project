package com.adabyron.infraestructure.web;

import com.adabyron.application.espacio.CambiarCategoriaDTO;
import com.adabyron.application.espacio.CambiarReservableDTO;
import com.adabyron.application.espacio.EspacioDTO;
import com.adabyron.application.espacio.EspacioService;
import com.adabyron.application.persona.PersonaDTO;
import com.adabyron.domain.espacio.Categoria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/espacios")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Espacios", description = "Gestión de espacios: consulta, asignación de categoría y de estado")
public class EspacioController {
    private final EspacioService espacioService;

    public EspacioController(EspacioService espacioService) {
        this.espacioService = espacioService;
    }

    @Operation(
            summary = "Obtener información de un espacio",
            description = "Devuelve el identificador, la categoría, el número de ocupantes máximos, el tamaño y si es reservable" +
                    "cuyo ID coincide con el proporcinado"
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
            //@ApiResponse(responseCode = "400", description = "Categoría inválida o no permitido.", content = @Content),
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
            //@ApiResponse(responseCode = "400", description = "Categoría inválida o no permitido.", content = @Content),
            @ApiResponse(responseCode = "404", description = "No existe ningún espacio con el ID indicado.", content = @Content)
    })
    @PutMapping("/{id}/reservable")
    public EspacioDTO cambiarEstado(@PathVariable String id, @RequestBody CambiarReservableDTO dto){
        return EspacioDTO.fromEntity(espacioService.cambiarReservable(id, dto.reservable()));
    }

}

