package com.adabyron.infraestructure.web;

import com.adabyron.domain.persona.exception.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PersonaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(PersonaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        RolIncompatibleException.class,
        DepartamentoRequeridoException.class,
        DepartamentoNoPermitidoException.class
    })
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
