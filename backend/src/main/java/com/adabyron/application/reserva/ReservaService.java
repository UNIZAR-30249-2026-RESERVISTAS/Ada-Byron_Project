package com.adabyron.application.reserva;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.adabyron.domain.persona.exception.PersonaNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import com.adabyron.domain.edificio.Edificio;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.espacio.EspacioRepository;
import com.adabyron.domain.persona.PersonaRepository;
import com.adabyron.domain.reserva.*;
import com.adabyron.domain.service.ReservaValidacionService;
import com.adabyron.domain.reserva.exception.ReservaNotFoundException;
import com.adabyron.infraestructure.mail.MailService;

@Service
@Transactional
public class ReservaService {
    private final ReservaRepository reservaRepository;
    private final EspacioRepository espacioRepository;
    private final PersonaRepository personaRepository;
    private final ReservaValidacionService validacionService;
    private final MailService servicioEmail;

    public ReservaService(ReservaRepository reservaRepository, EspacioRepository espacioRepository,
            PersonaRepository personaRepository, ReservaValidacionService validacionService,
            MailService servicioEmail) {
        this.reservaRepository = reservaRepository;
        this.espacioRepository = espacioRepository;
        this.personaRepository = personaRepository;
        this.validacionService = validacionService;
        this.servicioEmail = servicioEmail;
    }

    public Reserva crearReserva(CrearReservaDTO dto) {

        // 1. Validamos existencia de persona
        var persona = personaRepository.findById(dto.reservadaPorId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Persona no encontrada: " + dto.reservadaPorId()));

        // 2. Convertimos String IDs a EspacioId y cargar los Espacio completos
        List<EspacioId> espacioIds = dto.espacioIds().stream()
                .map(EspacioId::new)
                .toList();

        List<Espacio> espacios = espacioIds.stream()
                .map(id -> espacioRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Espacio no encontrado: " + id.id())))
                .toList();

        // 3. Construimos el IntervaloTemporal antes de llamar al factory
        IntervaloTemporal intervalo = IntervaloTemporal.of(
                dto.fecha(), dto.horaInicio(), dto.duracionMinutos());

        // 4. Creamos reserva en estado SOLICITADA — factory recibe los tipos correctos
        var reserva = ReservaFactory.crearNuevaReserva(
                espacioIds,
                persona.getPersonaId(),
                dto.tipoUso(),
                dto.numeroAsistentes(),
                intervalo,
                dto.detallesAdicionales());

        // 5. Validamos reglas F1-F8 para cada espacio
        try {
            for (Espacio espacio : espacios) {
                List<Reserva> reservasExistentes = reservaRepository.findActivasByEspacioId(espacio.getId().id());

                var asignacion = espacio.getAsignacion();
                var deptoEspacio = (asignacion != null && asignacion.esDepartamento())
                        ? asignacion.getDepartamentoId()
                        : null;

                validacionService.validar(
                        persona,
                        espacio,
                        dto.numeroAsistentes(),
                        intervalo,
                        Edificio.getPorcentajeOcupacionMaxima(),
                        reservasExistentes,
                        deptoEspacio);
            }
            reserva.confirmar();

        } catch (Exception ex) {
            reserva.rechazar(ex.getMessage());
        }

        return reservaRepository.save(reserva);
    }

    public Reserva crearReservaCriterios(CrearReservaPorCriteriosDTO dto) {

        // 1. Validar persona
        var persona = personaRepository.findById(dto.reservadaPorId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Persona no encontrada: " + dto.reservadaPorId()));

        // 2. Construir intervalo
        IntervaloTemporal intervalo = IntervaloTemporal.of(
                dto.fecha(),
                dto.horaInicio(),
                dto.duracionMinutos());

        // 3. Buscar espacios disponibles
        List<Espacio> disponibles = espacioRepository.findDisponiblesByCategoria(
                intervalo.fechaInicio(),
                intervalo.fechaFin(),
                dto.categoria());

        if (disponibles.isEmpty()) {
            throw new IllegalStateException("No hay espacios disponibles");
        }

        // 4. Ordenar por capacidad
        List<Espacio> listaModificable = new ArrayList<>(disponibles);
        listaModificable.sort(Comparator.comparingInt(Espacio::getNumOcupantes));

        double porcentaje = Edificio.getPorcentajeOcupacionMaxima();
        // 5. Seleccionar espacios según criterios
        List<Espacio> seleccionados = new ArrayList<>();
        int capacidadEspaciosReal = 0;

        for (Espacio espacio : listaModificable) {
            seleccionados.add(espacio);
            int capacidadRealDelEspacio = (int) Math.floor(espacio.getNumOcupantes() * porcentaje);
            capacidadEspaciosReal += capacidadRealDelEspacio;

            if (capacidadEspaciosReal >= dto.numeroAsistentes()) {
                break;
            }
        }

        // 6. Validar que se cumplen criterios
        if (capacidadEspaciosReal < dto.numeroAsistentes()) {
            throw new IllegalStateException("No hay suficientes espacios para cubrir la capacidad requerida");
        }

        // 7. Convertir a IDs
        List<EspacioId> espacioIds = seleccionados.stream()
                .map(Espacio::getId)
                .toList();

        // 8. Crear reserva
        var reserva = ReservaFactory.crearNuevaReserva(
                espacioIds,
                persona.getPersonaId(),
                dto.tipoUso(),
                dto.numeroAsistentes(),
                intervalo,
                dto.detallesAdicionales());

        // 9. Validaciones
        try {
            int asistentesPendientes = dto.numeroAsistentes();
            for (Espacio espacio : seleccionados) {
                // System.out.println("Validando espacio: " + espacio.getId().id() + " con
                // capacidad "
                // + espacio.getNumOcupantes() + " y asistentes pendientes: " +
                // asistentesPendientes);

                List<Reserva> reservasExistentes = reservaRepository.findActivasByEspacioId(espacio.getId().id());

                var asignacion = espacio.getAsignacion();
                var deptoEspacio = (asignacion != null && asignacion.esDepartamento())
                        ? asignacion.getDepartamentoId()
                        : null;
                int capacidadRealDelEspacio = (int) Math.floor(espacio.getNumOcupantes() * porcentaje);

                int asistentesEnEsteEspacio = Math.min(asistentesPendientes, capacidadRealDelEspacio);

                asistentesPendientes -= asistentesEnEsteEspacio;

                validacionService.validar(
                        persona,
                        espacio,
                        asistentesEnEsteEspacio,
                        intervalo,
                        Edificio.getPorcentajeOcupacionMaxima(),
                        reservasExistentes,
                        deptoEspacio);
            }

            reserva.confirmar();

        } catch (Exception ex) {
            reserva.rechazar(ex.getMessage());
        }

        return reservaRepository.save(reserva);
    }

    /**
     * REQ-H1 — Los gerentes consultan todas las reservas activas
     * (aquellas cuya hora de finalización es posterior al momento actual y su
     * estado es CONFIRMADA o POTENCIALMENTE_INVÁLIDA).
     */
    @Transactional(readOnly = true)
    public List<Reserva> listarReservasActivas() {
        return reservaRepository.findReservasActivas(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Reserva buscarPorId(UUID id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarPorPersona(UUID personaId) {
        return reservaRepository.findByReservadaPorId(personaId);
    }

    @Transactional(readOnly = true)
    public List<Reserva> listarActivasPorPersona(UUID personaId) {
        return reservaRepository.findReservasActivasPorId(personaId, LocalDateTime.now());
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
     * También permite al creador de la reserva cancelar su propia reserva.
     */
    public Reserva cancelarReserva(UUID id, UUID solicitanteId, String motivo) {
        // Validamos que la persona existe y obtener sus roles
        var persona = personaRepository.findById(solicitanteId)
                .orElseThrow(() -> new PersonaNotFoundException(solicitanteId));

        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));
        reserva.cancelar(persona.getRoles(), persona.getPersonaId(), motivo != null ? motivo : "Cancelada");

        String contenido = "Su reserva con id " + id + " ha sido cancelada";
        String mail = persona.getEmail();

        servicioEmail.enviarCorreoCancelacion(mail, contenido);

        return reservaRepository.save(reserva);
    }

    /**
     * O4 — El gerente convierte una reserva potencialmente inválida a estado
     * CONFIRMADA.
     */
    public Reserva revalidarReserva(UUID id, UUID gerenteId) {
        // Validamos que la persona existe y obtener sus roles
        var persona = personaRepository.findById(gerenteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Persona no encontrada: " + gerenteId));

        Reserva reserva = buscarPorId(id);
        reserva.revalidar(persona.getRoles());
        return reservaRepository.save(reserva);
    }

    /**
     * Elimina físicamente una reserva de la BD (hard delete).
     * IMPORTANTE: Esta operación es irreversible.
     *
     * Puede ser realizada por:
     * - El GERENTE (puede eliminar cualquier reserva)
     * - El usuario que creó la reserva (solo puede eliminar las suyas)
     */
    public void eliminarReserva(UUID id, UUID solicitanteId) {
        // Validamos que la persona existe y obtener sus roles
        var persona = personaRepository.findById(solicitanteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Persona no encontrada: " + solicitanteId));

        var mail = persona.getEmail();

        // Buscamos la reserva
        Reserva reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));

        // Validamos permisos (misma lógica que cancelar)
        boolean esGerente = persona.getRoles().contains(com.adabyron.domain.persona.Rol.GERENTE);
        boolean esCreador = reserva.getReservadaPorId().equals(persona.getPersonaId());

        if (!esGerente && !esCreador) {
            throw new com.adabyron.domain.reserva.exception.OperacionNoAutorizadaException(
                    "Solo el gerente o el creador de la reserva pueden eliminarla");
        }

        // Eliminamos físicamente
        reservaRepository.deleteById(id);

        String contenido = "Su reserva con id " + id + " ha sido eliminada";

        servicioEmail.enviarCorreoEliminacion(mail, contenido);
    }
}
