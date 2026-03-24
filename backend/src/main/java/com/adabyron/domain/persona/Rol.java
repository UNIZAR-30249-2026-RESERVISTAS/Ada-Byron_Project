package com.adabyron.domain.persona;

import com.adabyron.domain.espacio.CategoriaReserva;

public enum Rol {
    ESTUDIANTE,
    DOCENTE_INVESTIGADOR,
    INVESTIGADOR_CONTRATADO,
    CONSERJE,
    TECNICO_LABORATORIO,
    GERENTE;

    /**
     * REQ-B3, comprobamos si este rol exige adscripción a un departamento.
     * ESTUDIANTE, CONSERJE, GERENTE → NO necesitan departamento.
     * INVESTIGADOR_CONTRATADO, DOCENTE_INVESTIGADOR, TECNICO_LABORATORIO → SÍ necesitan departamento.
     */
    public boolean requiereDepartamento() {
        return switch(this) {
            case DOCENTE_INVESTIGADOR, INVESTIGADOR_CONTRATADO, TECNICO_LABORATORIO -> true;
            case ESTUDIANTE, CONSERJE, GERENTE -> false;
        };
    }

    /**
     * REQ-B2, ¿Una persona puede tener varios roles de manera simultánea?
     * Una persona solo tiene un rol, excepto el de gerente, que es compatible con docente-investigador.
     */
    public boolean esCompatibleCon(Rol otroRol) {
        if (this == otroRol) return false; // Un rol no es compatible consigo mismo.
        return (this == GERENTE && otroRol == DOCENTE_INVESTIGADOR) || (this == DOCENTE_INVESTIGADOR && otroRol == GERENTE);
    }

    /**
     * ¿Puede la persona con este rol reserva el tipo de espacio dado?
     * Seguimos la Tabla de permisos establecida en la documentación del proyecto
     */
    public boolean puedeReservarTipoEspacio(CategoriaReserva categoriaReserva) {
        return switch(this) {
            case ESTUDIANTE -> categoriaReserva == CategoriaReserva.SALA_COMUN;
            // Para el laboratorio, estos debe coincidir con el departamente de la persona, pero esa lógica la implementaremos en el servicio de reserva, aquí solo comprobamos que el rol tiene permiso para reservar ese tipo de espacio.
            case DOCENTE_INVESTIGADOR, INVESTIGADOR_CONTRATADO -> categoriaReserva == CategoriaReserva.AULA || categoriaReserva == CategoriaReserva.SEMINARIO || categoriaReserva == CategoriaReserva.LABORATORIO;
            case CONSERJE ->  categoriaReserva == CategoriaReserva.AULA || categoriaReserva == CategoriaReserva.SEMINARIO || categoriaReserva == CategoriaReserva.LABORATORIO || categoriaReserva == CategoriaReserva.SALA_COMUN;
            // Solo lo podrá reserver en caso de que el departamento del técnico de laboratorio coincida con el del espacio, pero esa lógica la implementaremos en el servicio de reserva, aquí solo comprobamos que el rol tiene permiso para reservar ese tipo de espacio.
            case TECNICO_LABORATORIO -> categoriaReserva == CategoriaReserva.LABORATORIO;
            case GERENTE -> true; // El gerente puede reservar cualquier tipo de espacio.
        };
    }

    /**
     * Identificador legible del rol, para facilitar su uso en la UI y en los logs.
     */
    public String nombreUI() {
        return switch(this) {
            case ESTUDIANTE -> "Estudiante";
            case DOCENTE_INVESTIGADOR -> "Docente-Investigador";
            case INVESTIGADOR_CONTRATADO -> "Investigador Contratado";
            case CONSERJE -> "Conserje";
            case TECNICO_LABORATORIO -> "Técnico de Laboratorio";
            case GERENTE -> "Gerente";
        };
    }
}
