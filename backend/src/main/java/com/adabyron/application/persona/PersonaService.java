package com.adabyron.application.persona;

import com.adabyron.domain.espacio.CategoriaReserva;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioRepository;
import com.adabyron.domain.reserva.*;
import com.adabyron.domain.persona.*;
import com.adabyron.domain.persona.exception.PersonaNotFoundException;
import com.adabyron.infraestructure.mail.MailService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para la gestión de personas.
 */
@Service
@Transactional
public class PersonaService {

    private final PersonaRepository personaRepository;
    private final ReservaRepository reservaRepository;
    private final PasswordEncoder passwordEncoder;
    private final EspacioRepository espacioRepository;
    private final MailService mailService;

    public PersonaService(
            PersonaRepository personaRepository,
            ReservaRepository reservaRepository,
            EspacioRepository espacioRepository,
            MailService mailService,
            PasswordEncoder passwordEncoder) {
        this.personaRepository = personaRepository;
        this.reservaRepository = reservaRepository;
        this.espacioRepository = espacioRepository;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    public Persona crearPersona(CrearPersonaDTO dto) {
        if (personaRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("Ya existe una persona con el email: " + dto.email());
        }
        if (dto.password() == null || dto.password().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        Rol rol = Rol.valueOf(dto.rol());
        DepartamentoId deptId = dto.departamentoId() != null
                ? new DepartamentoId(dto.departamentoId())
                : null;
        String passwordHash = passwordEncoder.encode(dto.password());
        Persona persona = PersonaFactory.crearNuevaPersona(dto.nombre(), dto.email(), passwordHash, rol, deptId);
        return personaRepository.save(persona);
    }

    @Transactional(readOnly = true) 
    public List<Persona> listarTodas() {
        return personaRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Persona buscarPorId(UUID id) {
        return personaRepository.findById(id)
                .orElseThrow(() -> new PersonaNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Persona buscarPorEmail(String email) {
        return personaRepository.findByEmail(email)
                .orElseThrow(() -> new PersonaNotFoundException(email));
    }

    public Persona cambiarRol(UUID id, CambiarRolDTO dto) {
        Persona persona = buscarPorId(id);
        Rol nuevoRol = Rol.valueOf(dto.rol());
        DepartamentoId deptId = dto.departamentoId() != null
                ? new DepartamentoId(dto.departamentoId())
                : null;
        persona.cambiarRol(nuevoRol, deptId);
        return personaRepository.save(persona);
    }

    public Persona añadirRolGerente(UUID id) {
        Persona persona = buscarPorId(id);
        persona.añadirRolGerente();
        return personaRepository.save(persona);
    }

    public Persona quitarRolGerente(UUID id) {
        Persona persona = buscarPorId(id);
        persona.quitarRolGerente();
        return personaRepository.save(persona);
    }

    /**
     * Cambia el departamento de una persona y valida sus reservas activas.
     * Si el cambio viola REQ-F4 (laboratorios) o REQ-O3 (despachos),
     * marca las reservas afectadas como POTENCIALMENTE_INVALIDA (O4).
     */
    public Persona cambiarDepartamento(UUID id, CambiarDepartamentoDTO dto) {
        Persona persona = buscarPorId(id);
        DepartamentoId nuevoDeptId = dto.departamentoId() != null
                ? new DepartamentoId(dto.departamentoId())
                : null;

        DepartamentoId deptAnterior = persona.getDepartamentoId();

        // Si no cambia departamento, nada que hacer
        if (deptAnterior != null && deptAnterior.equals(nuevoDeptId)) {
            return persona;
        }

        // Cambiar departamento
        persona.cambiarDepartamento(nuevoDeptId);
        personaRepository.save(persona);

        // Validar reservas confirmadas que puedan ser afectadas
        validarReservasAlCambiarDepartamento(persona, nuevoDeptId);

        return persona;
    }

    /**
     * Valida las reservas confirmadas de la persona tras cambio de departamento.
     * Marca como POTENCIALMENTE_INVALIDA aquellas que violen REQ-F4 o REQ-O3.
     */
    private void validarReservasAlCambiarDepartamento(Persona persona, DepartamentoId nuevoDeptId) {
        // Obtener reservas confirmadas activas de esta persona
        List<Reserva> reservasActivas = reservaRepository.findByReservadaPorId(persona.getPersonaId().valor())
                .stream()
                .filter(r -> r.getEstado() == EstadoReserva.CONFIRMADA)
                .filter(r -> r.getIntervalo().fechaFin().isAfter(LocalDateTime.now()))
                .toList();

        for (Reserva reserva : reservasActivas) {
            boolean violaReglas = false;
            String motivo = null;

            // Verificar cada espacio de la reserva
            for (var espacioId : reserva.getEspacioIds()) {
                Espacio espacio = espacioRepository.findById(espacioId)
                        .orElseThrow(() -> new IllegalStateException(
                                "Espacio no encontrado al validar reserva: " + espacioId.id()));

                String categoriaNombre = espacio.getCategoria().getNombre();
                CategoriaReserva categoria = CategoriaReserva.valueOf(
                        switch (categoriaNombre) {
                            case "Laboratorio" -> "LABORATORIO";
                            case "Despacho" -> "DESPACHO";
                            default -> "OTRO";
                        }
                );

                // Obtener departamento del espacio
                var asignacionEspacio = espacio.getAsignacion();
                var deptEspacio = (asignacionEspacio != null && asignacionEspacio.esDepartamento())
                        ? asignacionEspacio.getDepartamentoId()
                        : null;

                // REQ-F4: Laboratorios requieren mismo departamento
                if (categoria == CategoriaReserva.LABORATORIO) {
                    if (deptEspacio != null && !deptEspacio.equals(nuevoDeptId)) {
                        violaReglas = true;
                        motivo = "Ya no perteneces al departamento del laboratorio " + espacioId.id()
                                + " (REQ-F4)";
                        break;
                    }
                }

                // REQ-O3: Despachos requieren mismo departamento
                if (categoria == CategoriaReserva.DESPACHO) {
                    if (deptEspacio != null && !deptEspacio.equals(nuevoDeptId)) {
                        violaReglas = true;
                        motivo = "Ya no perteneces al departamento del despacho " + espacioId.id()
                                + " (REQ-O3)";
                        break;
                    }
                }
            }

            // Marcar como potencialmente inválida si viola reglas
            if (violaReglas) {
                reserva.marcarComoPotencialmenteInvalida(motivo);
                reservaRepository.save(reserva);

                // Notificar al usuario
                try {
                    String contenido = "Tu reserva (ID: " + reserva.getId() + ") ha pasado a estado "
                            + "POTENCIALMENTE_INVALIDA debido a un cambio de departamento.\n"
                            + "Motivo: " + motivo + "\n"
                            + "Por favor, contacta al gerente para revalidarla.";
                    mailService.enviarCorreoInvalida(persona.getEmail(), contenido);
                } catch (Exception e) {
                    // Log pero no fallar la operación
                    System.err.println("Error enviando email de validación: " + e.getMessage());
                }
            }
        }
    }

    public void eliminar(UUID id) {
        if (!personaRepository.existsById(id)) {
            throw new PersonaNotFoundException(id);
        }
        personaRepository.deleteById(id);
    }
}
