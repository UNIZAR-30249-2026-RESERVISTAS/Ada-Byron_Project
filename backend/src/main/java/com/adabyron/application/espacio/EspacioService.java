package com.adabyron.application.espacio;

import com.adabyron.domain.espacio.Categoria;
import com.adabyron.domain.espacio.Espacio;
import com.adabyron.domain.espacio.EspacioId;
import com.adabyron.domain.espacio.EspacioRepository;
import com.adabyron.domain.espacio.exception.EspacioNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EspacioService {
    private final EspacioRepository espacioRepository;

    public EspacioService(EspacioRepository espacioRepository) {
        this.espacioRepository = espacioRepository;
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

}
