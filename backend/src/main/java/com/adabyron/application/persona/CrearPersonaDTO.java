package com.adabyron.application.persona;

public record CrearPersonaDTO(
    String nombre,
    String email,
    String rol,
    Integer departamentoId
) {}
