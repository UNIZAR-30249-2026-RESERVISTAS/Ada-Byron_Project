package com.adabyron.application.persona;

public record CrearPersonaDTO(
    String nombre,
    String email,
    String password,
    String rol,
    Integer departamentoId
) {}
