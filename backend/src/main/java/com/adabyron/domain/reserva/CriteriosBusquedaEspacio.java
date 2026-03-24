package com.adabyron.domain.reserva;

import com.adabyron.domain.espacio.CategoriaReserva;

/**
 * VALUE OBJECT — Criterios para una reserva sin espacios concretos (O6).
 *
 * Permite solicitar una reserva indicando criterios en lugar de espacios concretos.
 * El sistema buscará espacios que los cumplan y realizará la reserva automáticamente.
 *
 * Ejemplo: 2 espacios, que en total permitan 60 personas, libres entre 8:00 y 11:00.
 */
public record CriteriosBusquedaEspacio (
    int numeroEspacios,  // Cuantos espacios se necesitan reservar (REQ-O6)
    int capacidadMinima, // Capacidad mínima requerida entre todos los espacios(REQ-O6)
    CategoriaReserva categoria, // Opcional, si se quiere limitar la búsqueda a una categoría concreta (REQ-O6)
    String planta       // Opcional, si se quiere limitar la búsqueda a una planta concreta (REQ-O6)
) {
    public CriteriosBusquedaEspacio {
        if (numeroEspacios <= 0) {
            throw new IllegalArgumentException("El número de espacios debe ser mayor que cero");
        }
        if (capacidadMinima <= 0) {
            throw new IllegalArgumentException("La capacidad mínima debe ser mayor que cero");
        }
    }
}
    
