package com.fanny.exception;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fanny.dto.ApiError;

/**
 * Convierte cada excepcion en una respuesta JSON con el mismo formato (ApiError),
 * en lugar de que Spring devuelva una pagina de error HTML.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> noEncontrado(RecursoNoEncontradoException ex) {
        return cuerpo(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<ApiError> validacion(ValidacionNegocioException ex) {
        return cuerpo(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler({ AccesoNoAutorizadoException.class, AccessDeniedException.class })
    public ResponseEntity<ApiError> accesoDenegado(RuntimeException ex) {
        return cuerpo(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta accion", null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> credencialesInvalidas(BadCredentialsException ex) {
        return cuerpo(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos", null);
    }

    // Errores de @Valid en los DTO (@NotBlank, @Email, @Min, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> datosInvalidos(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
                errores.put(err.getField(), err.getDefaultMessage()));
        return cuerpo(HttpStatus.BAD_REQUEST, "Hay datos invalidos en la solicitud", errores);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> errorGeneral(Exception ex) {
        return cuerpo(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado: " + ex.getMessage(), null);
    }

    private ResponseEntity<ApiError> cuerpo(HttpStatus status, String mensaje, Map<String, String> errores) {
        ApiError error = new ApiError(OffsetDateTime.now(), status.value(), mensaje, errores);
        return ResponseEntity.status(status).body(error);
    }
}
