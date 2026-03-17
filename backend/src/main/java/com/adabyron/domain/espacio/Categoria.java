package com.adabyron.domain.espacio;

public class Categoria {
    private final CategoriaId id;
    private final String nombre;

    public static final Categoria AULA = new Categoria(CategoriaId.AULA);
    public static final Categoria SEMINARIO = new Categoria(CategoriaId.SEMINARIO);
    public static final Categoria LABORATORIO = new Categoria(CategoriaId.LABORATORIO);
    public static final Categoria DESPACHO = new Categoria(CategoriaId.DESPACHO);
    public static final Categoria SALA_COMUN = new Categoria(CategoriaId.SALA_COMUN);

    public Categoria(CategoriaId id){
        this.id = id;
        this.nombre = switch (id.valor()) {
            case 1 -> "Aula";
            case 2 -> "Seminario";
            case 3 -> "Laboratorio";
            case 4 -> "Despacho";
            case 5 -> "Sala Común";
            default -> "Desconocido";
        };
    }

    public static Categoria desdeNombre(String nombre) {
        return switch (nombre) {
            case "Aula" -> AULA;
            case "Seminario" -> SEMINARIO;
            case "Laboratorio" -> LABORATORIO;
            case "Despacho" -> DESPACHO;
            case "Sala Común" -> SALA_COMUN;
            default -> throw new IllegalArgumentException("Nombre de categoría no reconocido: " + nombre);
        };
    }

    public CategoriaId getId() { return id; }
    public String getNombre() { return nombre; }
}
