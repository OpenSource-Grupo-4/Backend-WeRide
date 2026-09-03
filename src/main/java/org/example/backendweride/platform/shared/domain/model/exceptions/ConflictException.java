package org.example.backendweride.platform.shared.domain.model.exceptions;

/**
 * Thrown when an operation conflicts with the current state of the resource
 * (e.g. duplicate unique constraint). Mapped to HTTP 409 by GlobalExceptionHandler.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
