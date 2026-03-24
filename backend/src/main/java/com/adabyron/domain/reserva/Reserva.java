package com.adabyron.domain.reserva;

import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.persona.PersonaId;
import java.util.*;


/**
 * Agregado Raíz — Reserva.
 *
 * Una reserva puede incluir UNO o VARIOS espacios (O6 permite reservar múltiples).
 * Los espacios y la persona se referencian solo por ID (regla de DDD entre agregados).
 *
 * La creación e hidratación de instancias se delega a ReservaFactory,
 * siguiendo el mismo patrón que PersonaFactory en el dominio de persona.
 *
 * INVARIANTES:
 *   INV-1: Debe tener al menos un espacio.
 *   INV-2: El intervalo temporal es válido (mismo día, inicio < fin).
 *   INV-3: El número de asistentes es mayor que 0.
 *   INV-4: El estado sigue las transiciones del autómata finito.
 *   INV-5: Una reserva cancelada o rechazada no puede modificarse.
 */
public class Reserva {
    // Identidad
    private UUID id;
 
    // Referencias a otros agregados — solo por ID (REQ DDD)
    private List<EspacioId> espacioIds;   // INV-1 — uno o varios (O6)
    private UUID reservadaPorId;           // PersonaId del solicitante
 
    // Datos de la reserva (REQ-E1)
    private TipoUsoReserva tipoUso;
    private int numeroAsistentes;
    private IntervaloTemporal intervalo;
    private String detallesAdicionales;    // REQ-E3 — campo de texto libre
 
    // Estado del ciclo de vida
    private EstadoReserva estado;
    private String motivoRechazoOCancelacion;    // Mensaje informativo para el usuario
 
    // Constructor privado — solo desde factory
    protected Reserva() {} // para JPA
 
    Reserva(ReservaId id, List<EspacioId> espacioIds, PersonaId reservadaPorId,
                    TipoUsoReserva tipoUso, int numeroAsistentes, IntervaloTemporal intervalo,
                    String detallesAdicionales) {
        if (espacioIds == null || espacioIds.isEmpty())
            throw new IllegalArgumentException("La reserva debe incluir al menos un espacio (INV-1)");
        if (numeroAsistentes <= 0)
            throw new IllegalArgumentException("El número de asistentes debe ser mayor que 0 (INV-3)");
 
        this.id                   = id.valor();
        this.espacioIds           = new ArrayList<>(espacioIds);
        this.reservadaPorId       = reservadaPorId.valor();
        this.tipoUso              = tipoUso;
        this.numeroAsistentes     = numeroAsistentes;
        this.intervalo            = intervalo;
        this.detallesAdicionales  = detallesAdicionales;
        this.estado               = EstadoReserva.SOLICITADA;
    }

     /**
     * Constructor para rehidratación desde BD — estado restaurado.
     * Package-private: solo ReservaFactory puede instanciar Reserva.
     */
    Reserva(ReservaId id, List<EspacioId> espacioIds, PersonaId reservadaPorId,
            TipoUsoReserva tipoUso, int numeroAsistentes, IntervaloTemporal intervalo,
            String detallesAdicionales, EstadoReserva estado,
            String motivoRechazoOCancelacion) {
        this(id, espacioIds, reservadaPorId, tipoUso, numeroAsistentes,
             intervalo, detallesAdicionales);
        this.estado = estado;
        this.motivoRechazoOCancelacion = motivoRechazoOCancelacion;
    }

    // Funciones que representan las transiciones del automata finito de estados
    /**
     * SOLICITADA → CONFIRMADA
     * La reserva cumple todas las reglas — se aprueba automáticamente (REQ-G1).
     */
    public void confirmar() {
        if (estado != EstadoReserva.SOLICITADA)
            throw new com.adabyron.domain.reserva.exception.TransicionEstadoInvalidaException(
                estado, EstadoReserva.CONFIRMADA);
        this.estado = EstadoReserva.CONFIRMADA;
    }

    /**
     * SOLICITADA → RECHAZADA
     * La reserva no cumple alguna regla — se rechaza automáticamente (REQ-G1).
     */
    public void rechazar(String motivo) {
        if (estado != EstadoReserva.SOLICITADA)
            throw new com.adabyron.domain.reserva.exception.TransicionEstadoInvalidaException(
                estado, EstadoReserva.RECHAZADA);
        this.estado = EstadoReserva.RECHAZADA;
        this.motivoRechazoOCancelacion = motivo;
    }

    /**
     * CONFIRMADA → POTENCIALMENTE_INVALIDA (O4)
     * Las condiciones han cambiado y la reserva puede haber dejado de ser válida.
     */
    public void marcarComoPotencialmenteInvalida(String motivo) {
        if (estado != EstadoReserva.CONFIRMADA)
            throw new com.adabyron.domain.reserva.exception.TransicionEstadoInvalidaException(
                estado, EstadoReserva.POTENCIALMENTE_INVALIDA);
        this.estado = EstadoReserva.POTENCIALMENTE_INVALIDA;
        this.motivoRechazoOCancelacion = motivo;
    }

    /**
     * POTENCIALMENTE_INVALIDA → CONFIRMADA (O4)
     * El gerente decide que la reserva sigue siendo válida.
     */
    public void revalidar() {
        if (estado != EstadoReserva.POTENCIALMENTE_INVALIDA)
            throw new com.adabyron.domain.reserva.exception.TransicionEstadoInvalidaException(
                estado, EstadoReserva.CONFIRMADA);
        this.estado = EstadoReserva.CONFIRMADA;
        this.motivoRechazoOCancelacion = null;
    }

    /**
     * CONFIRMADA | POTENCIALMENTE_INVALIDA → CANCELADA (REQ-H2, REQ-I2)
     * El gerente elimina la reserva.
     */
    public void cancelar(String motivo) {
        if (!estado.esModificable())
            throw new com.adabyron.domain.reserva.exception.TransicionEstadoInvalidaException(
                estado, EstadoReserva.CANCELADA);
        this.estado = EstadoReserva.CANCELADA;
        this.motivoRechazoOCancelacion = motivo;
    }

    // Queries
    public boolean estaActiva() {
        return estado.estaActiva();
    }

    public boolean seSolapaCon(IntervaloTemporal otroIntervalo) {
        return intervalo.seSolapaCon(otroIntervalo);
    }

    public boolean incluyeEspacio(EspacioId espacioId) {
        return espacioIds.contains(espacioId);
    }

    // Getters:
    public ReservaId getId()                       { return new ReservaId(id); }
    public UUID getIdRaw()                          { return id; }
    public List<EspacioId> getEspacioIds()          { return Collections.unmodifiableList(espacioIds); }
    public PersonaId getReservadaPorId()            { return new PersonaId(reservadaPorId); }
    public UUID getReservadaPorIdRaw()              { return reservadaPorId; }
    public TipoUsoReserva getTipoUso()                     { return tipoUso; }
    public int getNumeroAsistentes()                { return numeroAsistentes; }
    public IntervaloTemporal getIntervalo()         { return intervalo; }
    public String getDetallesAdicionales()          { return detallesAdicionales; }
    public EstadoReserva getEstado()                { return estado; }
    public String getMotivoRechazoOCancelacion()    { return motivoRechazoOCancelacion; }

}
