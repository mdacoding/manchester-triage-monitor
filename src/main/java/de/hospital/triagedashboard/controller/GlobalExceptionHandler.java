package de.hospital.triagedashboard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Zentrale Fehlerbehandlung für alle REST-Controller.
 * Wandelt Exceptions in RFC 9457 konforme ProblemDetail-Antworten um.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNoSuchElementException(NoSuchElementException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Ressource nicht gefunden");
        problemDetail.setType(URI.create("https://api.hospital.de/errors/not-found"));
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationException(MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validierungsfehler bei der Eingabe.");
        problemDetail.setTitle("Ungültige Anfrage");
        problemDetail.setType(URI.create("https://api.hospital.de/errors/validation"));

        // Sammle alle Validierungsfehler und füge sie als Properties hinzu
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Ungültig",
                        (existing, replacement) -> existing // bei mehreren Fehlern pro Feld den ersten behalten
                ));

        problemDetail.setProperty("errors", errors);
        return problemDetail;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentialsException(BadCredentialsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Benutzername oder Passwort ist ungültig.");
        problemDetail.setTitle("Anmeldung fehlgeschlagen");
        problemDetail.setType(URI.create("https://api.hospital.de/errors/unauthorized"));
        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Ein unerwarteter Fehler ist aufgetreten.");
        problemDetail.setTitle("Interner Serverfehler");
        problemDetail.setType(URI.create("https://api.hospital.de/errors/internal-server-error"));
        return problemDetail;
    }
}
