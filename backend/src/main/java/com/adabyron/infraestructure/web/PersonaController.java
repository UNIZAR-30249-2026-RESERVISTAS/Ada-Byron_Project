package com.adabyron.infraestructure.web;

import com.adabyron.application.persona.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador para la gestión de personas.
 */
@RestController
@RequestMapping("/api/personas")
public class PersonaController {

    private final PersonaService personaService;

    public PersonaController(PersonaService personaService) {
        this.personaService = personaService;
    }

    @PostMapping
    public ResponseEntity<PersonaDTO> crear(@RequestBody CrearPersonaDTO dto) {
        var persona = personaService.crearPersona(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(PersonaDTO.fromEntity(persona));
    }

    @GetMapping
    public List<PersonaDTO> listarTodas() {
        return personaService.listarTodas().stream()
                .map(PersonaDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}")
    public PersonaDTO buscarPorId(@PathVariable UUID id) {
        return PersonaDTO.fromEntity(personaService.buscarPorId(id));
    }

    @GetMapping("/email/{email}")
    public PersonaDTO buscarPorEmail(@PathVariable String email) {
        return PersonaDTO.fromEntity(personaService.buscarPorEmail(email));
    }

    @PutMapping("/{id}/rol")
    public PersonaDTO cambiarRol(@PathVariable UUID id, @RequestBody CambiarRolDTO dto) {
        return PersonaDTO.fromEntity(personaService.cambiarRol(id, dto));
    }

    @PutMapping("/{id}/gerente")
    public PersonaDTO añadirGerente(@PathVariable UUID id) {
        return PersonaDTO.fromEntity(personaService.añadirRolGerente(id));
    }

    @DeleteMapping("/{id}/gerente")
    public PersonaDTO quitarGerente(@PathVariable UUID id) {
        return PersonaDTO.fromEntity(personaService.quitarRolGerente(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        personaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
