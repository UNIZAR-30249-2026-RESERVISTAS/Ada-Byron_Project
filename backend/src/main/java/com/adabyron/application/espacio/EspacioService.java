package com.adabyron.application.espacio;

import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.espacio.EspacioRepository;
import com.adabyron.domain.espacio.HorarioDisponible;
import com.adabyron.domain.espacio.exception.EspacioNotFoundException;
import com.adabyron.domain.persona.PersonaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class EspacioService {
    private final EspacioRepository espacioRepository;
    private final PersonaRepository personaRepository;

    public EspacioService(EspacioRepository espacioRepository, PersonaRepository personaRepository) {
        this.espacioRepository = espacioRepository;
        this.personaRepository = personaRepository;
    }

    @Transactional(readOnly = true)
    public Espacio obtenerDetalles(String id){
        EspacioId espacioId = new EspacioId(id);
        return espacioRepository.findById(espacioId).orElseThrow(() -> new EspacioNotFoundException(id));
    }

    public Espacio cambiarCategoria(String id, Categoria categoria){
        EspacioId espacioId = new EspacioId(id);
        Espacio espacio = espacioRepository.findById(espacioId).orElseThrow(() -> new EspacioNotFoundException(id));
        espacio.cambiarCategoria(categoria);
        return espacioRepository.save(espacio);
    }

    public Espacio cambiarReservable(String id, boolean reservable){
        EspacioId espacioId = new EspacioId(id);
        Espacio espacio = espacioRepository.findById(espacioId).orElseThrow(() -> new EspacioNotFoundException(id));
        espacio.cambiarReservable(reservable);
        return espacioRepository.save(espacio);
    }

    /**
     * REQ-C6: Permite a los gerentes cambiar el horario de un espacio.
     * El horario debe estar dentro del horario del edificio Ada Byron (8:00 - 21:00).
     */
    public Espacio cambiarHorario(String espacioId, HorarioDisponible nuevoHorario, UUID gerenteId) {
        // Validamos que la persona existe y obtenemos sus roles
        var persona = personaRepository.findById(gerenteId)
            .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + gerenteId));

        // Buscamos el espacio
        Espacio espacio = espacioRepository.findById(new EspacioId(espacioId))
            .orElseThrow(() -> new EspacioNotFoundException(espacioId));

        // Cambia horario (valida permisos y que esté dentro del horario del edificio)
        espacio.cambiarHorario(nuevoHorario, persona.getRoles());

        return espacioRepository.save(espacio);
    }

    /**
     * REQ-C6: Restablece el horario del espacio al horario por defecto del edificio Ada Byron.
     * Solo los gerentes pueden realizar esta operación.
     */
    public Espacio restablecerHorario(String espacioId, UUID gerenteId) {
        // Validamos que la persona existe y obtenemos sus roles
        var persona = personaRepository.findById(gerenteId)
            .orElseThrow(() -> new IllegalArgumentException("Persona no encontrada: " + gerenteId));

        // Buscamos el espacio
        Espacio espacio = espacioRepository.findById(new EspacioId(espacioId))
            .orElseThrow(() -> new EspacioNotFoundException(espacioId));

        // Restablecemos horario (valida permisos)
        espacio.restablecerHorarioEdificio(persona.getRoles());

        return espacioRepository.save(espacio);
    }

    /**
     * REQ-C5: Obtiene el horario disponible de un espacio.
     * Si el espacio tiene un horario específico, lo devuelve.
     * Si no, devuelve el horario del edificio Ada Byron por defecto.
     */
    @Transactional(readOnly = true)
    public HorarioDisponible obtenerHorario(String espacioId) {
        Espacio espacio = espacioRepository.findById(new EspacioId(espacioId))
            .orElseThrow(() -> new EspacioNotFoundException(espacioId));

        return espacio.getHorarioDisponible();
    }

}
