package com.comissions.korp.exception;

public class UsuarioJaExistente extends RuntimeException {
    public UsuarioJaExistente(String message) {
        super(message);
    }
}
