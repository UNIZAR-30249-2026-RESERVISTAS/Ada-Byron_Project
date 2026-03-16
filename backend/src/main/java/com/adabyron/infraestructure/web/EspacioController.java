package com.adabyron.infraestructure.web;

import com.adabyron.application.espacio.CambiarCategoriaDTO;
import com.adabyron.application.espacio.CambiarReservableDTO;
import com.adabyron.application.espacio.EspacioDTO;
import com.adabyron.application.espacio.EspacioService;
import com.adabyron.domain.espacio.Categoria;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/espacios")
@CrossOrigin(origins = "http://localhost:3000")
public class EspacioController {
    private final EspacioService espacioService;

    public EspacioController(EspacioService espacioService) {
        this.espacioService = espacioService;
    }

    @GetMapping("/{id}")
    public EspacioDTO buscarPorId(@PathVariable String id){
        return EspacioDTO.fromEntity(espacioService.obtenerDetalles(id));
    }

    @PutMapping("/{id}/categoria")
    public EspacioDTO cambiarCategoria(@PathVariable String id, @RequestBody CambiarCategoriaDTO dto){
        return EspacioDTO.fromEntity(espacioService.cambiarCategoria(id, Categoria.desdeNombre(dto.categoria())));
    }

    @PutMapping("/{id}/reservable")
    public EspacioDTO cambiarEstado(@PathVariable String id, @RequestBody CambiarReservableDTO dto){
        return EspacioDTO.fromEntity(espacioService.cambiarReservable(id, dto.reservable()));
    }

}

