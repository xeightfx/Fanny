package com.fanny.exception;

/** Se lanza cuando se busca por id (u otro campo) algo que no existe. Responde HTTP 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
