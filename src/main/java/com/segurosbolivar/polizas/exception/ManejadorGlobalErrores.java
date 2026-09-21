package com.segurosbolivar.polizas.exception;

import com.segurosbolivar.polizas.dto.RespuestaError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;

@RestControllerAdvice
public class ManejadorGlobalErrores {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> manejarNoEncontrado(RecursoNoEncontradoException ex,
                                                              HttpServletRequest request) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<RespuestaError> manejarReglaNegocio(ReglaNegocioException ex,
                                                              HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<RespuestaError> manejarRequestInvalido(Exception ex, HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, "La solicitud contiene parámetros o datos inválidos.", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarErrorInesperado(Exception ex, HttpServletRequest request) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado.", request);
    }

    private ResponseEntity<RespuestaError> respuesta(HttpStatus estado, String mensaje, HttpServletRequest request) {
        RespuestaError cuerpo = new RespuestaError(Instant.now(), estado.value(), estado.getReasonPhrase(),
                mensaje, request.getRequestURI());
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
