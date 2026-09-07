package com.inventario.inventario_backend.exception;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarRecursoNoEncontrado(RecursoNoEncontradoException exception) {
        return responder(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(NumeroSerieDuplicadoException.class)
    public ResponseEntity<ApiError> manejarNumeroSerieDuplicado(NumeroSerieDuplicadoException exception) {
        return responder(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(TransicionEstadoInvalidaException.class)
    public ResponseEntity<ApiError> manejarTransicionInvalida(TransicionEstadoInvalidaException exception) {
        return responder(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> manejarArgumentoNoValido(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Los datos proporcionados no son válidos");

        return responder(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> manejarRestriccionNoValida(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations()
                .stream()
                .map(violation -> violation.getMessage())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("Los datos proporcionados no son válidos");

        return responder(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> manejarIntegridadDeDatos(DataIntegrityViolationException exception) {
        return responder(
                HttpStatus.CONFLICT,
                "La operación no puede completarse porque entra en conflicto con datos existentes"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarErrorInterno(Exception exception) {
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ha ocurrido un error interno en el servidor");
    }

    private ResponseEntity<ApiError> responder(HttpStatus status, String message) {
        ApiError error = new ApiError(LocalDateTime.now(), status.value(), message);
        return ResponseEntity.status(status).body(error);
    }
}