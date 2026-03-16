package com.adabyron.domain.espacio;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Espacio {
    private final EspacioId id;
    private final int numOcupantes;
    private final double tamanyo;
    private Categoria categoria;
    private boolean reservable;


    public Espacio(EspacioId id, int numOcupantes, Categoria categoria, double tamanyo, boolean reservable) {
        this.id = id;
        this.numOcupantes = numOcupantes;
        this.categoria = categoria;
        this.tamanyo = tamanyo;
        this.reservable = reservable;
    }

    public void cambiarReservable(boolean reservable){
        this.reservable = reservable;
    }

    public void cambiarCategoria(Categoria categoria){
        if(this.reservable == true){
            this.categoria = categoria;
        }
    }


}
