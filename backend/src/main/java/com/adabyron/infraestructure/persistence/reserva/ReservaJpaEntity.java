package com.adabyron.infraestructure.persistence.reserva;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.*;

import com.adabyron.domain.reserva.EstadoReserva;
import com.adabyron.domain.reserva.TipoUsoReserva;

/**
 * Entidad JPA para la persistencia de reservas — capa de infraestructura.
 *
 * REPRESENTACIÓN TEMPORAL:
 * Utiliza dos columnas separadas (fechaInicio, fechaFin) de tipo LocalDateTime por dos razones:
 *   1. JPA requiere campos "planos" para mapear a columnas de base de datos.
 *      Los Value Objects como IntervaloTemporal requieren @Embedded o descomposición manual.
 *   2. Facilita consultas SQL directas (ej: "WHERE fechaFin > :ahora" para reservas activas).
 *
 * El dominio (Reserva) encapsula esto en un IntervaloTemporal (Value Object), y el mapper
 * (ReservaRepositoryJpa) se encarga de la conversión bidireccional:
 *   - Dominio → BD: intervalo.fechaInicio() / intervalo.fechaFin()
 *   - BD → Dominio: new IntervaloTemporal(fechaInicio, fechaFin)
 *
 * Ver IntervaloTemporal para la representación en el dominio.
 * Ver CrearReservaDTO para la representación en el API.
 */
@Entity
@Table(name = "reservas")
public class ReservaJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID reservadaPorId;

    @Enumerated(EnumType.STRING)
    @Column( nullable = false, length = 100)
    private TipoUsoReserva tipoUso;

    @Column(nullable = false)
    private int numeroAsistentes;

    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Column(length = 1000)
    private String detallesAdicionales;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoReserva estado;

    @Column(length = 500)
    private String motivoRechazoOCancelacion;

    @ElementCollection
    @CollectionTable(name = "reserva_espacios", joinColumns = @JoinColumn(name = "reserva_id"))
    @Column(name = "espacio_id")
    private List<String> espacioIds = new ArrayList<>();

    protected ReservaJpaEntity() {}

    public ReservaJpaEntity(UUID id, UUID reservadaPorId, TipoUsoReserva tipoUso, int numeroAsistentes,
                            LocalDateTime fechaInicio, LocalDateTime fechaFin,
                            String detallesAdicionales, EstadoReserva estado,
                            String motivoRechazoOCancelacion, List<String> espacioIds) {
        this.id = id;
        this.reservadaPorId = reservadaPorId;
        this.tipoUso = tipoUso;
        this.numeroAsistentes = numeroAsistentes;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.detallesAdicionales = detallesAdicionales;
        this.estado = estado;
        this.motivoRechazoOCancelacion = motivoRechazoOCancelacion;
        this.espacioIds = espacioIds;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getReservadaPorId() { return reservadaPorId; }
    public TipoUsoReserva getTipoUso() { return tipoUso; }
    public int getNumeroAsistentes() { return numeroAsistentes; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public String getDetallesAdicionales() { return detallesAdicionales; }
    public EstadoReserva getEstado() { return estado; }
    public String getMotivoRechazoOCancelacion() { return motivoRechazoOCancelacion; }
    public List<String> getEspacioIds() { return espacioIds; }
}
