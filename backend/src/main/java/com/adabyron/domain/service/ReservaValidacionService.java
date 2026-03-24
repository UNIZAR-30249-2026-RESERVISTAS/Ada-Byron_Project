package com.adabyron.domain.service;

import com.adabyron.domain.espacio.CategoriaReserva;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.persona.DepartamentoId;
import com.adabyron.domain.persona.Persona;
import com.adabyron.domain.persona.Rol;
import com.adabyron.domain.reserva.IntervaloTemporal;
import com.adabyron.domain.reserva.Reserva;
import com.adabyron.domain.reserva.exception.ReservaInvalidaException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ReservaValidacionService {
    /**
     * Valida todas las reglas de reserva (F1-F8) para un espacio concreto.
     * Lanza ReservaInvalidaException con el motivo si alguna regla no se cumple.
     *
     * @param persona         La persona que solicita la reserva
     * @param espacio         El espacio que se quiere reservar
     * @param numAsistentes   Número de personas que asistirán
     * @param intervalo       Intervalo temporal solicitado
     * @param porcentajeOcup  Porcentaje de ocupación actual del edificio (0.0 - 1.0)
     * @param reservasExist   Reservas vigentes del espacio (para comprobar solapamientos)
     * @param deptoEspacio    Departamento al que está asignado el espacio (puede ser null)
     */
    public void validar(Persona persona, Espacio espacio, int numAsistentes,
                        IntervaloTemporal intervalo, double porcentajeOcup,
                        List<Reserva> reservasExist, DepartamentoId deptoEspacio) {
 
        // REQ-F8 — el gerente puede reservar cualquier espacio reservable
        if (persona.tieneRol(Rol.GERENTE)) {
            validarDisponibilidad(espacio, intervalo, reservasExist);
            return;
        }
 
        // El espacio debe ser reservable
        if (!espacio.isReservable())
            throw new ReservaInvalidaException("El espacio " + espacio.getId().id() + " no es reservable");
 
        // Obtener la categoría de reserva del espacio
        CategoriaReserva categoria = obtenerCategoria(espacio);
 
        // REQ-F5 — los despachos no pueden reservarse (salvo O3 y gerente)
        if (categoria == CategoriaReserva.DESPACHO)
            throw new ReservaInvalidaException("Los despachos no pueden reservarse (REQ-F5)");
 
        // REQ-F1, F2, F3 — permisos por rol
        Rol rolPrincipal = persona.rolPrincipal();
        if (!rolPrincipal.puedeReservarTipoEspacio(categoria))
            throw new ReservaInvalidaException(
                "El rol '" + rolPrincipal.nombreUI() +
                "' no puede reservar espacios de tipo '" + espacio.getCategoria().getNombre() + "'"
            );
 
        // REQ-F4 — laboratorios: técnico de lab, investigador contratado y docente-investigador
        //          solo pueden reservar labs de su mismo departamento
        if (categoria == CategoriaReserva.LABORATORIO) {
            validarDepartamentoLaboratorio(persona, deptoEspacio);
        }
 
        // REQ-F6 — el número de asistentes no puede superar Máximo_Ocupantes * %_Uso_Permitido
        validarAforo(espacio, numAsistentes, porcentajeOcup);
 
        // REQ-F7 — el espacio no puede estar ya reservado en ese intervalo
        validarDisponibilidad(espacio, intervalo, reservasExist);
    }

    // Métodos privados de validación
 
    private void validarDepartamentoLaboratorio(Persona persona, DepartamentoId deptoEspacio) {
        boolean requiereValidacionDepto =
            persona.tieneRol(Rol.TECNICO_LABORATORIO) ||
            persona.tieneRol(Rol.INVESTIGADOR_CONTRATADO) ||
            persona.tieneRol(Rol.DOCENTE_INVESTIGADOR);
 
        if (!requiereValidacionDepto) return;
 
        DepartamentoId deptoPersona = persona.getDepartamentoId();
        if (deptoPersona == null || deptoEspacio == null || !deptoPersona.equals(deptoEspacio))
            throw new ReservaInvalidaException(
                "Solo puedes reservar laboratorios de tu departamento (REQ-F4)"
            );
    }

    private void validarAforo(Espacio espacio, int numAsistentes, double porcentajeOcup) {
        int maxPermitidos = calcularAforo(espacio, porcentajeOcup);
        if (numAsistentes > maxPermitidos)
            throw new ReservaInvalidaException(
                "El número de asistentes (" + numAsistentes +
                ") supera el máximo permitido (" + maxPermitidos +
                ") con el porcentaje de ocupación actual del edificio (REQ-F6)"
            );
    }
 
    private int calcularAforo(Espacio espacio, double porcentajeOcup) {
        // REQ-F6: Máximo_Ocupantes * %_Uso_Permitido
        return (int) Math.floor(espacio.getNumOcupantes() * porcentajeOcup);
    }
 
    private void validarDisponibilidad(Espacio espacio, IntervaloTemporal intervalo,
                                        List<Reserva> reservasExistentes) {
        boolean hayConflicto = reservasExistentes.stream()
            .filter(Reserva::estaActiva) // Solo consideramos reservas activas (no canceladas ni rechazadas)
            .anyMatch(r -> r.seSolapaCon(intervalo));
 
        if (hayConflicto)
            throw new ReservaInvalidaException(
                "El espacio " + espacio.getId().id() +
                " ya está reservado en ese intervalo de tiempo (REQ-F7)"
            );
    }
 
    /**
     * Obtiene la CategoriaReserva correspondiente a la Categoria del Espacio.
     * Necesario porque Espacio usa Categoria (clase) y Rol usa CategoriaReserva (enum).
     */
    private CategoriaReserva obtenerCategoria(Espacio espacio) {
        return switch (espacio.getCategoria().getNombre()) {
            case "Aula"       -> CategoriaReserva.AULA;
            case "Seminario"  -> CategoriaReserva.SEMINARIO;
            case "Laboratorio"-> CategoriaReserva.LABORATORIO;
            case "Despacho"   -> CategoriaReserva.DESPACHO;
            case "Sala Común" -> CategoriaReserva.SALA_COMUN;
            default -> throw new IllegalArgumentException(
                "Categoría de espacio no reconocida: " + espacio.getCategoria().getNombre());
        };
    }
}
