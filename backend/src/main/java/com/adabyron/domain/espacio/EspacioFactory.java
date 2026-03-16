package com.adabyron.domain.espacio;

public class EspacioFactory {
    public static Espacio crearNuevoEspacio(String planta, int numero, int categoriaId, double tamanyo, boolean reservable){
        String idFinal;

        if (planta.startsWith("S")) {
            // Formato para Sótano: S + planta(1) + numero(001) = S1001
            idFinal = String.format("S%s%03d", planta.replace("S", ""), numero);
        } else {
            // Formato para Planta: planta(0) + numero(01) = 001
            idFinal = String.format("%s%02d", planta, numero);
        }
        //Hay que calcular los ocupantes de alguna forma

        EspacioId espacioId = new EspacioId(idFinal);

        return new Espacio(espacioId, 0, new Categoria(new CategoriaId(categoriaId)), tamanyo, reservable);
    }

}
