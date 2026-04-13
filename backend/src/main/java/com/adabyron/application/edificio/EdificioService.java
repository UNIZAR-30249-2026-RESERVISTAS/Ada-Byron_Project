package com.adabyron.application.edificio;

import com.adabyron.domain.edificio.Edificio;
import com.adabyron.domain.edificio.EdificioRepository;
import com.adabyron.domain.edificio.PorcentajeOcupacion;
import com.adabyron.domain.espacio.EspacioRepository;
import com.adabyron.domain.persona.PersonaRepository;
import com.adabyron.domain.persona.exception.PersonaNotFoundException;
import com.adabyron.domain.reserva.EstadoReserva;
import com.adabyron.domain.reserva.Reserva;
import com.adabyron.domain.reserva.ReservaRepository;
import java.util.*;

import com.adabyron.infraestructure.mail.MailService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class EdificioService {

    private final EdificioRepository edificioRepository;
    private final ReservaRepository reservaRepository;
    private final EspacioRepository espacioRepository;
    private final PersonaRepository personaRepository;
    private final MailService mailService;

    public EdificioService(
            EdificioRepository edificioRepository,
            ReservaRepository reservaRepository,
            EspacioRepository espacioRepository,
            PersonaRepository personaRepository,
            MailService mailService
    ) {
        this.edificioRepository = edificioRepository;
        this.reservaRepository = reservaRepository;
        this.espacioRepository = espacioRepository;
        this.personaRepository = personaRepository;
        this.mailService = mailService;
    }

    @PostConstruct
    public void inicializarDesdeBD() {
        double porcentajePersistido = edificioRepository
                .obtenerPorcentajeOcupacionMaxima()
                .valor();
        Edificio.cambiarPorcentajeOcupacionMaxima(porcentajePersistido);
    }

    @Transactional(readOnly = true)
    public double obtenerPorcentajeOcupacionMaxima() {
        return edificioRepository.obtenerPorcentajeOcupacionMaxima().valor();
    }

    public double cambiarPorcentajeOcupacionMaxima(CambiarPorcentajeOcupacionDTO dto) {
        if (dto.porcentajeOcupacionMaxima() == null) {
            throw new IllegalArgumentException("El porcentaje de ocupacion es obligatorio");
        }

        PorcentajeOcupacion nuevo = PorcentajeOcupacion.of(dto.porcentajeOcupacionMaxima());
        edificioRepository.guardarPorcentajeOcupacionMaxima(nuevo);

        Edificio.cambiarPorcentajeOcupacionMaxima(nuevo.valor());

        marcarConfirmadasQueSuperanAforo(nuevo.valor());

        return nuevo.valor();
    }

    /**
     * Función auxiliar que marca como potencialmente inválidas las reservas confirmadas que superan el nuevo aforo permitido.
     * Está función se llama tras un cambio en el porcentaje de ocupación máxima del edificio para asegurar que las reservas que ya estaban confirmadas
     *  pero que ahora superan el nuevo límite de ocupación sean identificadas y puedan ser revisadas por los usuarios o el sistema.
     */
    private void marcarConfirmadasQueSuperanAforo(double porcentajeActual) {
        List<Reserva> activas = reservaRepository.findReservasActivas(LocalDateTime.now());

        for (Reserva reserva : activas) {
            if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
                continue;
            }

            boolean superaAforo = reserva.getEspacioIds().stream()
                    .map(espacioId -> espacioRepository.findById(espacioId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Espacio no encontrado al revalidar reserva: " + espacioId.id())))
                    .anyMatch(espacio -> {
                        int maxPermitidos = (int) Math.floor(espacio.getNumOcupantes() * porcentajeActual);
                        return reserva.getNumeroAsistentes() > maxPermitidos;
                    });

            if (superaAforo) {
                reserva.marcarComoPotencialmenteInvalida(
                        "Supera el aforo permitido tras cambio del porcentaje de ocupacion del edificio");
                String id = reserva.getId().toString();
                String contenido = "Su reserva con id " + id + " ha sido cancelada";
                UUID personaId = reserva.getReservadaPorIdRaw();
                var persona = personaRepository.findById(personaId)
                        .orElseThrow(() -> new PersonaNotFoundException(personaId));
                String mail = persona.getEmail();
                mailService.enviarCorreoInvalida(mail, contenido);
                reservaRepository.save(reserva);
            }
        }
    }
}