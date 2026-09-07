package com.inventario.inventario_backend.exception;

import com.inventario.inventario_backend.enums.EstadoActivo;

public class TransicionEstadoInvalidaException extends RuntimeException {

    public TransicionEstadoInvalidaException(EstadoActivo estadoActual, EstadoActivo estadoNuevo) {
        super("No es posible cambiar el estado de " + estadoActual + " a " + estadoNuevo);
    }
}