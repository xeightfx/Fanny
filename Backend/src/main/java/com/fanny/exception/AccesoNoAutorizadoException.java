package com.fanny.exception;

/** Se lanza cuando el usuario autenticado intenta tocar un recurso que no es suyo. Responde HTTP 403. */
public class AccesoNoAutorizadoException extends RuntimeException {

    public AccesoNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
