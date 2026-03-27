package com.adabyron.infraestructure.persistence.reserva;

import com.adabyron.domain.reserva.*;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.persona.PersonaId;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;

@Repository
public class ReservaRepositoryJpa implements ReservaRepository {
   
    private final SpringDataReservaRepository jpa;

    public ReservaRepositoryJpa(SpringDataReservaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Reserva save(Reserva reserva) {
        ReservaJpaEntity entity = toEntity(reserva); 
        jpa.save(entity); 
        return reserva;
    }

    @Override
    public Optional<Reserva> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Reserva> findReservasActivas(LocalDateTime ahora) {
        return jpa.findReservasActivas(ahora).stream()
                  .map(this::toDomain)
                  .toList();
    }

    @Override
    public List<Reserva> findActivasByEspacioId(String espacioId) {
        return jpa.findActivasByEspacioId(espacioId).stream()
                  .map(this::toDomain)
                  .toList();
    }

    @Override
    public List<Reserva> findByReservadaPorId(UUID personaId) {
        return jpa.findByReservadaPorId(personaId).stream()
                  .map(this::toDomain)
                  .toList();
    }

    @Override
    public List<Reserva> findReservasActivasPorId(UUID personaId, LocalDateTime ahora) {
        return  jpa.findReservasActivasPorId(personaId, ahora).stream()
                  .map(this::toDomain)
                  .toList();
    }

    @Override
    public List<Reserva> findPotencialmenteInvalidas(LocalDateTime ahora) {
        return jpa.findPotencialmenteInvalidas(ahora).stream()
                  .map(this::toDomain)
                  .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    private ReservaJpaEntity toEntity(Reserva reserva) {
        List<String> espaciosUUID = reserva.getEspacioIds().stream()
                                        .map(EspacioId::id)
                                        .collect(Collectors.toList());
        return new ReservaJpaEntity(
            reserva.getId().valor(),
            reserva.getReservadaPorId().valor(),
            reserva.getTipoUso(),
            reserva.getNumeroAsistentes(),
            reserva.getIntervalo().fechaInicio(),
            reserva.getIntervalo().fechaFin(),
            reserva.getDetallesAdicionales(),
            reserva.getEstado(),
            reserva.getMotivoRechazoOCancelacion(),
            espaciosUUID
        );
    }

    private Reserva toDomain(ReservaJpaEntity entity) {
        List<EspacioId> espacioIds = entity.getEspacioIds().stream()
                                        .map(uuid -> new EspacioId(uuid))
                                        .collect(Collectors.toList());
        IntervaloTemporal intervalo = new IntervaloTemporal(entity.getFechaInicio(), entity.getFechaFin());
        return ReservaFactory.reconstruirReserva(
                new ReservaId(entity.getId()),
                espacioIds,
                new PersonaId(entity.getReservadaPorId()),
                entity.getTipoUso(),
                entity.getNumeroAsistentes(),
                intervalo,
                entity.getDetallesAdicionales(),
                entity.getEstado(),
                entity.getMotivoRechazoOCancelacion()
        );
    }
}
