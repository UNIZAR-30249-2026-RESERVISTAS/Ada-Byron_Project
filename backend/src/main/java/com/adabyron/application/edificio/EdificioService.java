package com.adabyron.application.edificio;

import com.adabyron.domain.edificio.Edificio;
import com.adabyron.domain.edificio.EdificioRepository;
import com.adabyron.domain.edificio.PorcentajeOcupacion;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EdificioService {

    private final EdificioRepository edificioRepository;

    public EdificioService(EdificioRepository edificioRepository) {
        this.edificioRepository = edificioRepository;
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

        // Mantiene coherencia con ReservaService, que ya lee de Edificio en memoria.
        Edificio.cambiarPorcentajeOcupacionMaxima(nuevo.valor());

        return nuevo.valor();
    }
}
