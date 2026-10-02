package com.fanny.exception;

/**
 * Se lanza cuando una regla de negocio no se cumple (ej: stock insuficiente,
 * categoria duplicada, email ya registrado). Responde HTTP 400.
 */
public class ValidacionNegocioException extends RuntimeException {

    public ValidacionNegocioException(String mensaje) {
        super(mensaje);
    }
}
