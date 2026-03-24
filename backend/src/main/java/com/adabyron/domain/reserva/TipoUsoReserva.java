package com.adabyron.domain.reserva;


/**
 * ENUM — Tipo de uso de una reserva (REQ-E1).
 */
public enum TipoUsoReserva {
    DOCENCIA,
    INVESTIGACION,
    GESTION, 
    OTRO;

    public String nombreUI() {
        return switch (this) {
            case DOCENCIA -> "Docencia";
            case INVESTIGACION -> "Investigación";
            case GESTION -> "Gestión";
            case OTRO -> "Otro";
        };
    }
}
