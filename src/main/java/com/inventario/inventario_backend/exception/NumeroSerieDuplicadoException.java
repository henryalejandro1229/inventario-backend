package com.inventario.inventario_backend.exception;

public class NumeroSerieDuplicadoException extends RuntimeException {

    public NumeroSerieDuplicadoException(String numeroSerie) {
        super("El número de serie ya está registrado: " + numeroSerie);
    }
}