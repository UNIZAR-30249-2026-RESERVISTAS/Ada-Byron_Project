package com.adabyron.application.reserva;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.espacio.EspacioRepository;
import com.adabyron.domain.persona.PersonaRepository;
import com.adabyron.domain.reserva.*;
import com.adabyron.domain.service.ReservaValidacionService;
import com.adabyron.domain.reserva.exception.ReservaNotFoundException;

@Service
@Transactional
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final EspacioRepository espacioRepository;
    private final PersonaRepository personaRepository;
    private final ReservaValidacionService validacionService;

    public ReservaService(ReservaRepository reservaRepository, EspacioRepository espacioRepository, PersonaRepository personaRepository, ReservaValidacionService validacionService) {
        this.reservaRepository = reservaRepository;
        this.espacioRepository = espacioRepository;
        this.personaRepository = personaRepository;
        this.validacionService = validacionService;
    }

    public Reserva crearReserva(CrearReservaDTO dto) {

    // 1. Validar existencia de persona
    var persona = personaRepository.findById(dto.reservadaPorId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Persona no encontrada: " + dto.reservadaPorId()));

    // 2. Convertir String IDs a EspacioId y cargar los Espacio completos
    List<EspacioId> espacioIds = dto.espacioIds().stream()
        .map(EspacioId::new)
        .toList();

    List<Espacio> espacios = espacioIds.stream()
        .map(id -> espacioRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Espacio no encontrado: " + id.id())))
        .toList();

    // 3. Construir el IntervaloTemporal antes de llamar al factory
    IntervaloTemporal intervalo = IntervaloTemporal.of(
        dto.fecha(), dto.horaInicio(), dto.duracionMinutos()
    );

    // 4. Crear reserva en estado SOLICITADA — factory recibe los tipos correctos
    var reserva = ReservaFactory.crearNuevaReserva(
        espacioIds,
        persona.getPersonaId(),   // PersonaId, no Persona
        dto.tipoUso(),
        dto.numeroAsistentes(),
        intervalo,                // IntervaloTemporal, no fecha/hora sueltos
        dto.detallesAdicionales()
    );

    // 5. Validar reglas F1-F8 para cada espacio
    try {
        for (Espacio espacio : espacios) {
            List<Reserva> reservasExistentes =
                reservaRepository.findActivasByEspacioId(espacio.getId().id());

            validacionService.validar(
                persona,
                espacio,
                dto.numeroAsistentes(),
                intervalo,
                1.0,                        // porcentaje ocupación — ajustar cuando esté el agregado Edificio
                reservasExistentes,
                persona.getDepartamentoId()
            );
        }
        reserva.confirmar();

    } catch (Exception ex) {
        reserva.rechazar(ex.getMessage());
    }

    return reservaRepository.save(reserva);
}


    /**
     * REQ-H1 — Los gerentes consultan todas las reservas activas
     * (aquellas cuya hora de finalización es posterior al momento actual).
     */
    @Transactional(readOnly = true)
    public List<Reserva> listarReservasActivas() {
        return reservaRepository.findReservasActivas(LocalDateTime.now());
    }
 
    @Transactional(readOnly = true)
    public Reserva buscarPorId(UUID id) {
        return reservaRepository.findById(id)
            .orElseThrow(() -> new ReservaNotFoundException("Reserva no encontrada: " + id));
    }
 
    @Transactional(readOnly = true)
    public List<Reserva> listarPorPersona(UUID personaId) {
        return reservaRepository.findByReservadaPorId(personaId);
    }
 
    /**
     * O4 — El gerente consulta las reservas potencialmente inválidas.
     */
    @Transactional(readOnly = true)
    public List<Reserva> listarPotencialmenteInvalidas() {
        return reservaRepository.findPotencialmenteInvalidas(LocalDateTime.now());
    }
 
    /**
     * REQ-H2 — El gerente cancela una reserva y se notifica al usuario (REQ-I2).
     */
    public Reserva cancelarReserva(UUID id, String motivo) {
        Reserva reserva = buscarPorId(id);
        reserva.cancelar(motivo != null ? motivo : "Cancelada por el gerente");
        return reservaRepository.save(reserva);
    }
 
    /**
     * O4 — El gerente devuelve una reserva potencialmente inválida a estado CONFIRMADA.
     */
    public Reserva revalidarReserva(UUID id) {
        Reserva reserva = buscarPorId(id);
        reserva.revalidar();
        return reservaRepository.save(reserva);
    }
 
    public void eliminarReserva(UUID id) {
        if (!reservaRepository.findById(id).isPresent())
            throw new ReservaNotFoundException("Reserva no encontrada: " + id);
        reservaRepository.deleteById(id);
    }
}
