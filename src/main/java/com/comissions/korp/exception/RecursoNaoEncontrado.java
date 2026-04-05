package com.comissions.korp.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecursoNaoEncontrado extends RuntimeException {
    public RecursoNaoEncontrado(String message) {
        super(message);
    }
}
