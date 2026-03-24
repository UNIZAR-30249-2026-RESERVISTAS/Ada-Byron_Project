package com.adabyron.infraestructure.web;

import java.util.List;
import java.util.UUID;

import com.adabyron.application.reserva.CrearReservaDTO;
import com.adabyron.application.reserva.ReservaDTO;
import com.adabyron.application.reserva.ReservaService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
    private final ReservaService reservaService;
 
    public ReservaController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

     //  POST /api/reservas 
    // REQ-E1: Crear una nueva reserva
    @PostMapping
    public ResponseEntity<ReservaDTO> crear(@RequestBody CrearReservaDTO dto) {
        var reserva = reservaService.crearReserva(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                             .body(ReservaDTO.fromEntity(reserva));
    }
 
    //  GET /api/reservas 
    // REQ-H1: Listar todas las reservas activas (solo disponibles para gerentes??)
    @GetMapping
    public List<ReservaDTO> listarActivas() {
        return reservaService.listarReservasActivas().stream()
                             .map(ReservaDTO::fromEntity)
                             .toList();
    }
 
    // GET /api/reservas/{id} 
    @GetMapping("/{id}")
    public ReservaDTO buscarPorId(@PathVariable UUID id) {
        return ReservaDTO.fromEntity(reservaService.buscarPorId(id));
    }

    // GET /api/reservas/persona/{personaId} 
    // Reservas de una persona concreta
    @GetMapping("/persona/{personaId}")
    public List<ReservaDTO> listarPorPersona(@PathVariable UUID personaId) {
        return reservaService.listarPorPersona(personaId).stream()
                             .map(ReservaDTO::fromEntity)
                             .toList();
    }
 
    //  GET /api/reservas/potencialmente-invalidas 
    // O4: El gerente consulta reservas potencialmente inválidas
    @GetMapping("/potencialmente-invalidas")
    public List<ReservaDTO> listarPotencialmenteInvalidas() {
        return reservaService.listarPotencialmenteInvalidas().stream()
                             .map(ReservaDTO::fromEntity)
                             .toList();
    }

    // PUT /api/reservas/{id}/revalidar 
    // O4: El gerente devuelve una reserva a estado CONFIRMADA
    @PutMapping("/{id}/revalidar")
    public ReservaDTO revalidar(@PathVariable UUID id) {
        return ReservaDTO.fromEntity(reservaService.revalidarReserva(id));
    }
 
    // DELETE /api/reservas/{id} 
    // REQ-H2: El gerente cancela/elimina una reserva
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(
            @PathVariable UUID id,
            @RequestParam(required = false) String motivo) {
        reservaService.cancelarReserva(id, motivo);
        return ResponseEntity.noContent().build();
    }
}
