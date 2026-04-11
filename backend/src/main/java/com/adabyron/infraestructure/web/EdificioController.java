package com.adabyron.infraestructure.web;

import com.adabyron.application.edificio.CambiarPorcentajeOcupacionDTO;
import com.adabyron.application.edificio.EdificioOcupacionDTO;
import com.adabyron.application.edificio.EdificioService;
import com.adabyron.domain.reserva.exception.OperacionNoAutorizadaException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping("/api/edificio")
public class EdificioController {

    private final EdificioService edificioService;

    public EdificioController(EdificioService edificioService) {
        this.edificioService = edificioService;
    }

    private UUID requirePersonaId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("personaId") == null) {
            throw new OperacionNoAutorizadaException("Debes iniciar sesión");
        }
        return UUID.fromString(String.valueOf(session.getAttribute("personaId")));
    }

    private boolean esGerente(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;

        Object rolesObj = session.getAttribute("roles");
        return rolesObj instanceof Collection<?> roles
                && roles.stream().anyMatch(r -> "GERENTE".equals(String.valueOf(r)));
    }

    private void requireGerente(HttpServletRequest request) {
        requirePersonaId(request);
        if (!esGerente(request)) {
            throw new OperacionNoAutorizadaException("Solo el gerente puede acceder a este recurso");
        }
    }

    @GetMapping("/ocupacion")
    public EdificioOcupacionDTO obtenerPorcentajeOcupacion() {
        return new EdificioOcupacionDTO(edificioService.obtenerPorcentajeOcupacionMaxima());
    }

    @PutMapping("/ocupacion")
    public EdificioOcupacionDTO cambiarPorcentajeOcupacion(
            @RequestBody CambiarPorcentajeOcupacionDTO dto,
            HttpServletRequest request
    ) {
        requireGerente(request);
        double nuevo = edificioService.cambiarPorcentajeOcupacionMaxima(dto);
        return new EdificioOcupacionDTO(nuevo);
    }
}