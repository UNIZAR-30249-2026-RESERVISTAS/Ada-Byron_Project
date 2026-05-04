package com.adabyron.infraestructure.web;

import com.adabyron.application.edificio.CambiarPorcentajeOcupacionDTO;
import com.adabyron.application.edificio.EdificioHorarioDTO;
import com.adabyron.application.edificio.EdificioOcupacionDTO;
import com.adabyron.application.edificio.EdificioService;
import com.adabyron.domain.reserva.exception.OperacionNoAutorizadaException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/edificio")
public class EdificioController {

    private final EdificioService edificioService;
    private final RabbitTemplate rabbitTemplate;

    public EdificioController(EdificioService edificioService, RabbitTemplate rabbitTemplate) {

        this.edificioService = edificioService;
        this.rabbitTemplate = rabbitTemplate;
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
    public EdificioOcupacionDTO obtenerPorcentajeOcupacion() throws TimeoutException {
        //return new EdificioOcupacionDTO(edificioService.obtenerPorcentajeOcupacionMaxima());

        Object respuesta = rabbitTemplate.convertSendAndReceive("edificio.porcentajeOcupacion", "GET");

        if (respuesta == null) {
            throw new TimeoutException("El servicio de edificio no responde");
        }

        return (EdificioOcupacionDTO) respuesta;
    }

    @PutMapping("/ocupacion")
    public EdificioOcupacionDTO cambiarPorcentajeOcupacion(
            @RequestBody CambiarPorcentajeOcupacionDTO dto,
            HttpServletRequest request
    ) throws TimeoutException {
        requireGerente(request);
        //double nuevo = edificioService.cambiarPorcentajeOcupacionMaxima(dto);
        //return new EdificioOcupacionDTO(nuevo);

        Object respuesta = rabbitTemplate.convertSendAndReceive("edificio.cambiarPorcentajeOcupacion", dto);

        if (respuesta == null) {
            throw new TimeoutException("El servicio de edificio no responde");
        }

        return (EdificioOcupacionDTO) respuesta;
    }

    @ResponseStatus(value = HttpStatus.REQUEST_TIMEOUT)
    @ExceptionHandler(TimeoutException.class)
    public String timeout() {
        return "La petición no puede resolverse ahora, el servidor de personas no responde.";
    }
}