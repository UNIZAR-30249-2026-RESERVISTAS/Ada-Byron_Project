package com.adabyron.domain.espacio;

import com.adabyron.domain.edificio.Edificio;
import com.adabyron.domain.espacio.exception.HorarioInvalidoException;
import com.adabyron.domain.persona.Rol;
import com.adabyron.domain.reserva.exception.OperacionNoAutorizadaException;

import java.util.Objects;
import java.util.Set;


public class Espacio {
    private final EspacioId id;
    private final int numOcupantes;
    private final double tamanyo;
    private Categoria categoria;
    private boolean reservable;
    private Asignacion asignacion;

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
        this.horarioEspecifico = Edificio.getHorarioPorDefecto(); // REQ-C5: Por defecto, usa el horario del edificio
        this.asignacion = asignacionPorDefecto(categoria); // Asignación por defecto    
    }

    /**
     * Constructor completo incluyendo horario específico (para rehidratación desde BD).
     */
    public Espacio(EspacioId id, int numOcupantes, Categoria categoria, double tamanyo,
                   boolean reservable, HorarioDisponible horarioEspecifico, Asignacion asignacion) {
        this.id = id;
        this.numOcupantes = numOcupantes;
        this.categoria = categoria;
        this.tamanyo = tamanyo;
        this.reservable = reservable;
        this.horarioEspecifico = horarioEspecifico;
        this.asignacion = Objects.requireNonNull(asignacion, "La asignación es obligatoria");
        validarAsignacionParaCategoria(this.categoria, this.asignacion);
    }

    /**
     * Valida que la asignación sea compatible con la categoría del espacio.
     * Los espacios que pueden ser asignados a la EINA son: Aula, Seminario, Laboratorio y Sala Común.
     * Los despachos deben ser asignados a un departamento o a personas específicas, no pueden ser asignados a la EINA.
     */
    private static Asignacion asignacionPorDefecto(Categoria categoria) {
        return switch (categoria.getId().valor()) {
            case 1, 2, 3, 5 -> Asignacion.eina();
            case 4 -> throw new IllegalArgumentException("Un despacho debe crearse con asignación explícita a departamento o personas");
            default -> throw new IllegalArgumentException("Categoría no soportada");
        };
    }

    /**
     * REQ-C8: Permite cambiar la asignación del espacio, validando que la nueva asignación sea compatible con la categoría del espacio.
     */
    public void cambiarAsignacion(Asignacion nuevaAsignacion) {
        validarAsignacionParaCategoria(this.categoria, nuevaAsignacion);
        this.asignacion = nuevaAsignacion;
    }

    /**
     * Valida que la asignación sea compatible con la categoría del espacio.
     * REQ-C8: Las aulas y salas comunes deben estar asignados a la EINA.
     * Los despachos deben estar asignados a un departamento o a personas específicas, no pueden estar asignados a la EINA.
     * Los seminarios y laboratorios pueden estar asignados a la EINA o a un departamento.
     */
    private void validarAsignacionParaCategoria(Categoria categoria, Asignacion asignacion) {
        int cat = categoria.getId().valor();
        switch (cat) {
            case 1, 5 -> {
                if (!asignacion.esEina()) {
                    throw new IllegalArgumentException("Aulas y salas comunes deben estar asignadas a EINA");
                }
            }
            case 4 -> {
                if (!(asignacion.esDepartamento() || asignacion.esPersonas())) {
                    throw new IllegalArgumentException("Los despachos deben asignarse a departamento o personas");
                }
            }
            case 2, 3 -> {
                if (!(asignacion.esEina() || asignacion.esDepartamento())) {
                    throw new IllegalArgumentException("Seminarios y laboratorios deben asignarse a EINA o departamento");
                }
            }
            default -> throw new IllegalArgumentException("Categoría no soportada");
        }
    }

    /**
     * REQ-C5: Obtiene el horario disponible del espacio.
     * Si el espacio tiene un horario específico configurado, lo devuelve.
     * Si no, devuelve el horario del edificio Ada Byron por defecto.
     */
    public HorarioDisponible getHorarioDisponible() {
        return horarioEspecifico != null
            ? horarioEspecifico
            : Edificio.getHorarioPorDefecto();
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
        HorarioDisponible horarioEdificio = Edificio.getHorarioPorDefecto();
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
        this.horarioEspecifico = Edificio.getHorarioPorDefecto();
    }

    /**
     * Verifica si el espacio tiene un horario específico configurado.
     * Si devuelve false, el espacio usa el horario del edificio por defecto,
     * si devuelve true, el espacio tiene un horario específico que se debe usar para validar reservas.
     */
    public boolean tieneHorarioEspecifico() {
        return horarioEspecifico != null;
    }

    /**
     * REQ-C2: Permite cambiar si el espacio es reservable o no.
     */
    public void cambiarReservable(boolean reservable){
        this.reservable = reservable;
    }

    /**
     * REQ-C3: Permite cambiar la categoría de un espacio, siempre que el espacio sea reservable.
     */
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
    public Asignacion getAsignacion() {
        return asignacion;
    }
}
