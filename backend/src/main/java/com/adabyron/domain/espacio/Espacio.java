package com.adabyron.domain.espacio;

import com.adabyron.domain.edificio.HorarioEdificio;
import com.adabyron.domain.espacio.exception.HorarioInvalidoException;
import com.adabyron.domain.persona.Rol;
import com.adabyron.domain.reserva.exception.OperacionNoAutorizadaException;

import java.util.Set;


public class Espacio {
    private final EspacioId id;
    private final int numOcupantes;
    private final double tamanyo;
    private Categoria categoria;
    private boolean reservable;

    /**
     * Horario específico del espacio (REQ-C6).
     * Si es null, se usa el horario del edificio Ada Byron por defecto (REQ-C5).
     * Los gerentes pueden cambiar este horario, pero debe estar dentro del horario del edificio.
     */
    private HorarioDisponible horarioEspecifico;


    public Espacio(EspacioId id, int numOcupantes, Categoria categoria, double tamanyo, boolean reservable) {
        this.id = id;
        this.numOcupantes = numOcupantes;
        this.categoria = categoria;
        this.tamanyo = tamanyo;
        this.reservable = reservable;
        this.horarioEspecifico = null; // REQ-C5: Por defecto, usa el horario del edificio
    }

    /**
     * Constructor completo incluyendo horario específico (para rehidratación desde BD).
     */
    public Espacio(EspacioId id, int numOcupantes, Categoria categoria, double tamanyo,
                   boolean reservable, HorarioDisponible horarioEspecifico) {
        this.id = id;
        this.numOcupantes = numOcupantes;
        this.categoria = categoria;
        this.tamanyo = tamanyo;
        this.reservable = reservable;
        this.horarioEspecifico = horarioEspecifico;
    }

    /**
     * REQ-C5: Obtiene el horario disponible del espacio.
     * Si el espacio tiene un horario específico configurado, lo devuelve.
     * Si no, devuelve el horario del edificio Ada Byron por defecto.
     */
    public HorarioDisponible getHorarioDisponible() {
        return horarioEspecifico != null
            ? horarioEspecifico
            : HorarioEdificio.obtenerHorarioEdificio();
    }

    /**
     * REQ-C6: Permite a los gerentes cambiar el horario de un espacio.
     * El nuevo horario debe estar contenido en el horario del edificio Ada Byron.
     */
    public void cambiarHorario(HorarioDisponible nuevoHorario, Set<Rol> rolesDelSolicitante) {
        // Validamos que solo gerentes pueden cambiar horarios
        if (!rolesDelSolicitante.contains(Rol.GERENTE)) {
            throw new OperacionNoAutorizadaException(
                "Solo el gerente puede cambiar el horario de un espacio");
        }

        // Validamos que el horario está dentro del horario del edificio
        HorarioDisponible horarioEdificio = HorarioEdificio.obtenerHorarioEdificio();
        if (!nuevoHorario.estaContenidoEn(horarioEdificio)) {
            throw new HorarioInvalidoException(
                "El horario del espacio (" + nuevoHorario + ") debe estar contenido en el horario del edificio (" + horarioEdificio + ")");
        }

        this.horarioEspecifico = nuevoHorario;
    }

    /**
     * REQ-C6: Restablece el horario del espacio al horario por defecto del edificio.
     * Solo los gerentes pueden realizar esta operación.
     */
    public void restablecerHorarioEdificio(Set<Rol> rolesDelSolicitante) {
        if (!rolesDelSolicitante.contains(Rol.GERENTE)) {
            throw new OperacionNoAutorizadaException(
                "Solo el gerente puede restablecer el horario de un espacio");
        }
        this.horarioEspecifico = null;
    }

    /**
     * Verifica si el espacio tiene un horario específico configurado.
     * Si devuelve false, el espacio usa el horario del edificio por defecto,
     * si devuelve true, el espacio tiene un horario específico que se debe usar para validar reservas.
     */
    public boolean tieneHorarioEspecifico() {
        return horarioEspecifico != null;
    }

    public void cambiarReservable(boolean reservable){
        this.reservable = reservable;
    }

    public void cambiarCategoria(Categoria categoria){
        if(this.reservable == true){
            this.categoria = categoria;
        }
    }

    // Getters ( añadidos para evitar warnings)
    public EspacioId getId() {
        return id;
    }
    public int getNumOcupantes() {
        return numOcupantes;
    }
    public double getTamanyo() {
        return tamanyo;
    }
    public Categoria getCategoria() {
        return categoria;
    }
    public boolean isReservable() {
        return reservable;
    }


}
