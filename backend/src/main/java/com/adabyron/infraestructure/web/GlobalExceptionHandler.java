package com.adabyron.infraestructure.web;

import com.adabyron.domain.persona.exception.*;
import com.adabyron.domain.reserva.exception.OperacionNoAutorizadaException;
import com.adabyron.domain.reserva.exception.ReservaNotFoundException;
import com.adabyron.domain.reserva.exception.TransicionEstadoInvalidaException;
import com.adabyron.domain.espacio.exception.HorarioInvalidoException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 — recurso no encontrado
    @ExceptionHandler(PersonaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(PersonaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    // 409 — conflicto de estado: el rol actual no permite la operación
    @ExceptionHandler(RolIncompatibleException.class)
    public ResponseEntity<Map<String, String>> handleRolIncompatible(RolIncompatibleException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    // 409 — conflicto de estado: el recurso ya existe
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        if (ex.getMessage() != null && ex.getMessage().startsWith("Ya existe una persona")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
        // Resto de IllegalArgumentException → 400
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    // 400 — datos enviados incorrectos o incoherentes
    @ExceptionHandler({
        DepartamentoRequeridoException.class,
        DepartamentoNoPermitidoException.class,
        TransicionEstadoInvalidaException.class,
        HorarioInvalidoException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    // 403 — operación no autorizada (faltan permisos de gerente)
    @ExceptionHandler(OperacionNoAutorizadaException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(OperacionNoAutorizadaException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    // 404 — reserva no encontrada
    @ExceptionHandler(ReservaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleReservaNotFound(ReservaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(com.adabyron.domain.espacio.exception.EspacioNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEspacioNotFound(
            com.adabyron.domain.espacio.exception.EspacioNotFoundException ex
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
}