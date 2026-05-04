package com.adabyron.domain.service;

import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioRepository;
import com.adabyron.domain.reserva.IntervaloTemporal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AsignadorEspaciosService {

    private final EspacioRepository espacioRepository;

    public AsignadorEspaciosService(EspacioRepository espacioRepository) {
        this.espacioRepository = espacioRepository;
    }

    public List<Espacio> asignar(
            int numEspacios,
            int capacidadTotal,
            IntervaloTemporal intervalo
    ) {

        List<Espacio> disponibles =
            espacioRepository.findDisponibles(intervalo.inicio(), intervalo.fin());

        disponibles.sort(Comparator.comparing(Espacio::getTamanyo));
        List<Espacio> seleccionados = new ArrayList<>();
        int capacidadAcumulada = 0;

        for (Espacio espacio : disponibles) {
            seleccionados.add(espacio);
            capacidadAcumulada += espacio.getTamanyo();

            if (seleccionados.size() >= numEspacios &&
                capacidadAcumulada >= capacidadTotal) {
                break;
            }
        }

        if (capacidadAcumulada < capacidadTotal) {
            throw new IllegalStateException("No hay espacios suficientes disponibles");
        }

        return seleccionados;
    }
}