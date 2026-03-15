package com.adabyron.infraestructure.web;

import com.adabyron.application.persona.DepartamentoDTO;
import com.adabyron.domain.persona.Departamento;
import com.adabyron.domain.persona.DepartamentoId;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para la gestión de departamentos.
 */
@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoController {

    @GetMapping
    public List<DepartamentoDTO> listarTodos() {
        return Departamento.values().stream()
                .map(DepartamentoDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public DepartamentoDTO buscarPorId(@PathVariable int id) {
        Departamento departamento = Departamento.fromId(new DepartamentoId(id));
        return DepartamentoDTO.fromEntity(departamento);
    }
}
